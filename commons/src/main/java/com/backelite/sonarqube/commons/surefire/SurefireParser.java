/**
 * commons - Enables analysis of Swift and Objective-C projects into SonarQube.
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
package com.backelite.sonarqube.commons.surefire;

import com.backelite.sonarqube.commons.MeasureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.test.MutableTestPlan;
import org.sonar.api.test.TestCase;
import org.sonar.api.utils.StaxParser;

import javax.annotation.Nullable;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Map;

/**
 * Created by gillesgrousset on 28/08/2018.
 */
public class SurefireParser {

    protected static final Logger LOGGER = LoggerFactory.getLogger(SurefireParser.class);

    protected final FileSystem fileSystem;
    protected final SensorContext context;
    protected final ResourcePerspectives perspectives;

    protected SurefireParser(FileSystem fileSystem, ResourcePerspectives perspectives, SensorContext context) {
        this.fileSystem = fileSystem;
        this.perspectives = perspectives;
        this.context = context;
    }

    private void parseFiles(File[] reports, UnitTestIndex index) {
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

    public void collect(File reportsDir) {


        File[] xmlFiles = getReports(reportsDir);

        if (xmlFiles.length > 0) {
            parseFiles(xmlFiles);
        }
    }

    private File[] getReports(File dir) {

        if (dir == null || !dir.isDirectory() || !dir.exists()) {
            return new File[0];
        }

        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                // .junit is for fastlane support
                return (name.startsWith("TEST") && name.endsWith(".xml")) || (name.endsWith(".junit"));
            }
        });
    }


    private void parseFiles(File[] reports) {
        UnitTestIndex index = new UnitTestIndex();
        parseFiles(reports, index);
        save(index);
    }

    private void save(UnitTestIndex index) {


        long negativeTimeTestNumber = 0;
        int testsCount = 0;
        int testsSkipped = 0;
        int testsErrors = 0;
        int testsFailures = 0;
        long testsTime = 0;

        for (Map.Entry<String, UnitTestClassReport> entry : index.getIndexByClassname().entrySet()) {

            UnitTestClassReport report = entry.getValue();

            testsCount += report.getTests() - report.getSkipped();
            testsSkipped += report.getSkipped();
            testsErrors += report.getErrors();
            testsFailures += report.getFailures();

            if (report.getTests() > 0) {

                negativeTimeTestNumber += report.getNegativeTimeTestNumber();
                InputFile inputFile = getUnitTestResource(entry.getKey());
                if (inputFile != null) {
                    saveResults(inputFile, report);
                } else {
                    LOGGER.warn("Resource not found: {}", entry.getKey());
                }

            }

        }


        if (negativeTimeTestNumber > 0) {
            LOGGER.warn("There is {} test(s) reported with negative time by data, total duration may not be accurate.", negativeTimeTestNumber);
        }

        if (testsCount > 0) {
            InputComponent module = context.module();
            MeasureUtil.saveMeasure(context, module, CoreMetrics.TESTS, testsCount);
            MeasureUtil.saveMeasure(context, module, CoreMetrics.SKIPPED_TESTS, testsSkipped);
            MeasureUtil.saveMeasure(context, module, CoreMetrics.TEST_ERRORS, testsErrors);
            MeasureUtil.saveMeasure(context, module, CoreMetrics.TEST_FAILURES, testsFailures);
            MeasureUtil.saveMeasure(context, module, CoreMetrics.TEST_EXECUTION_TIME, testsTime);
        }
    }


    protected void saveResults(InputFile testFile, UnitTestClassReport report) {
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

    @Nullable
    public  InputFile getUnitTestResource(String classname) {
        return TestFileFinders.getInstance().getUnitTestResource(fileSystem, classname);
    }


}
