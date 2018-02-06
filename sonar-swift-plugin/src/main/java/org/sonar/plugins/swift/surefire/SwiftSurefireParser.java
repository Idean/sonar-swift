/**
 * backelite-sonar-swift-plugin - Enables analysis of Swift projects into SonarQube.
 * Copyright © 2015 Backelite (${email})
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sonar.plugins.swift.surefire;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Resource;
import org.sonar.api.test.MutableTestPlan;
import org.sonar.api.test.TestCase;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.api.utils.StaxParser;
import org.sonar.plugins.swift.surefire.data.SurefireStaxHandler;
import org.sonar.plugins.swift.surefire.data.UnitTestClassReport;
import org.sonar.plugins.swift.surefire.data.UnitTestIndex;
import org.sonar.plugins.swift.surefire.data.UnitTestResult;

import javax.annotation.Nullable;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.Map;


public final class SwiftSurefireParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwiftSurefireParser.class);

    private final FileSystem fileSystem;
    private final SensorContext context;
    private final ResourcePerspectives perspectives;

    public SwiftSurefireParser(FileSystem fileSystem, ResourcePerspectives perspectives, SensorContext context) {
        this.fileSystem = fileSystem;
        this.perspectives = perspectives;
        this.context = context;
    }

    public void collect(File reportsDir) {


        File[] xmlFiles = getReports(reportsDir);

        if (xmlFiles.length == 0) {
            insertZeroWhenNoReports();
        } else {
            parseFiles(xmlFiles);
        }
    }

    private File[] getReports(File dir) {

        if (dir == null || !dir.isDirectory() || !dir.exists()) {
            return new File[0];
        }

        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                // .junit is for Fastlane support
                return (name.startsWith("TEST") && name.endsWith(".xml")) || (name.endsWith(".junit"));
            }
        });
    }

    private void insertZeroWhenNoReports() {
        context.saveMeasure(CoreMetrics.TESTS, 0.0);
    }

    private void parseFiles(File[] reports) {
        UnitTestIndex index = new UnitTestIndex();
        parseFiles(reports, index);
        save(index);
    }

    private static void parseFiles(File[] reports, UnitTestIndex index) {
        SurefireStaxHandler staxParser = new SurefireStaxHandler(index);
        StaxParser parser = new StaxParser(staxParser, false);
        for (File report : reports) {
            try {
                parser.parse(report);
            } catch (XMLStreamException e) {
                throw new IllegalStateException("Fail to parse the Surefire report: " + report, e);
            }
        }
    }

    private void save(UnitTestIndex index) {
        long negativeTimeTestNumber = 0;

        for (Map.Entry<String, UnitTestClassReport> entry : index.getIndexByClassname().entrySet()) {
            UnitTestClassReport report = entry.getValue();
            if (report.getTests() > 0) {
                negativeTimeTestNumber += report.getNegativeTimeTestNumber();
                Resource resource = getUnitTestResource(entry.getKey());
                if (resource != null) {
                    save(report, resource);
                } else {
                    LOGGER.warn("Resource not found: {}", entry.getKey());
                }
            }
        }
        if (negativeTimeTestNumber > 0) {
            LOGGER.warn("There is {} test(s) reported with negative time by data, total duration may not be accurate.", negativeTimeTestNumber);
        }
    }

    private void save(UnitTestClassReport report, Resource resource) {
        double testsCount = report.getTests() - report.getSkipped();
        saveMeasure(resource, CoreMetrics.SKIPPED_TESTS, report.getSkipped());
        saveMeasure(resource, CoreMetrics.TESTS, testsCount);
        saveMeasure(resource, CoreMetrics.TEST_ERRORS, report.getErrors());
        saveMeasure(resource, CoreMetrics.TEST_FAILURES, report.getFailures());
        saveMeasure(resource, CoreMetrics.TEST_EXECUTION_TIME, report.getDurationMilliseconds());
        double passedTests = testsCount - report.getErrors() - report.getFailures();
        if (testsCount > 0) {
            double percentage = passedTests * 100d / testsCount;
            saveMeasure(resource, CoreMetrics.TEST_SUCCESS_DENSITY, ParsingUtils.scaleValue(percentage));
        }
        saveResults(resource, report);
    }

    protected void saveResults(Resource testFile, UnitTestClassReport report) {
        for (UnitTestResult unitTestResult : report.getResults()) {
            MutableTestPlan testPlan = perspectives.as(MutableTestPlan.class, testFile);
            if (testPlan != null) {
                testPlan.addTestCase(unitTestResult.getName())
                        .setDurationInMs(Math.max(unitTestResult.getDurationMilliseconds(), 0))
                        .setStatus(TestCase.Status.of(unitTestResult.getStatus()))
                        .setMessage(unitTestResult.getMessage())
                        .setType(TestCase.TYPE_UNIT)
                        .setStackTrace(unitTestResult.getStackTrace());
            }
        }
    }

    @Nullable  public Resource getUnitTestResource(String classname) {
        String fileName = classname.replace('.', '/') + ".swift";
	String wildcardFileName = classname.replace(".", "/**/") + ".swift";

        InputFile inputFile = fileSystem.inputFile(fileSystem.predicates().hasPath(fileName));

        /*
         * Most xcodebuild JUnit parsers don't include the path to the class in the class field, so search for it if it
         * wasn't found in the root.
         */
        if (inputFile == null) {
            List<InputFile> files = ImmutableList.copyOf(fileSystem.inputFiles(fileSystem.predicates().and(
                    fileSystem.predicates().hasType(InputFile.Type.TEST),
                    fileSystem.predicates().matchesPathPattern("**/" + wildcardFileName))));

            if (files.isEmpty()) {
                LOGGER.info("Unable to locate test source file {}", wildcardFileName);
            } else {
                /*
                 * Lazily get the first file, since we wouldn't be able to determine the correct one from just the
                 * test class name in the event that there are multiple matches.
                 */
                inputFile = files.get(0);
            }
        }

        return inputFile == null ? null : context.getResource(inputFile);
    }

    private void saveMeasure(Resource resource, Metric metric, double value) {
        if (!Double.isNaN(value)) {
            context.saveMeasure(resource, metric, value);
        }
    }
}
