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

import com.backelite.sonarqube.commons.TestFileFinders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.test.MutableTestPlan;
import org.sonar.api.test.TestCase;
import org.sonar.squidbridge.api.AnalysisException;

import javax.annotation.CheckForNull;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SurefireReportParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(SurefireReportParser.class);
    private static final String TESTSUITE = "testsuite";
    private static final String TESTCASE = "testcase";

    protected final SensorContext context;
    private final DocumentBuilderFactory dbfactory;
    private final UnitTestIndex index;
    private final ResourcePerspectives perspectives;
    private final FileSystem fileSystem;

    protected SurefireReportParser(FileSystem fileSystem, ResourcePerspectives perspectives, SensorContext context) {
        this.fileSystem = fileSystem;
        this.context = context;
        this.perspectives = perspectives;
        this.dbfactory = DocumentBuilderFactory.newInstance();
        this.index = new UnitTestIndex();
    }

    public void collect(File reportsDir) {
        List<File> xmlFiles = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(reportsDir.toPath(), "{TEST}*.{xml}")) {
            for (Path p: stream) {
                LOGGER.info("Processing Surefire report {}", p.getFileName());
                xmlFiles.add(p.toFile());
            }
        } catch (IOException e) {
            LOGGER.error( "Error while finding test files.", e);
        }

        if (!xmlFiles.isEmpty()) {
            parseFiles(xmlFiles);
        }
    }

    private void parseFiles(List<File> reports) {
        UnitTestIndex index = new UnitTestIndex();
        parseFiles(reports, index);
        save(index, context);
    }

    private static void parseFiles(List<File> reports, UnitTestIndex index) {
        StaxParser parser = new StaxParser(index);
        for (File report : reports) {
            try {
                parser.parse(report);
            } catch (XMLStreamException e) {
                throw new AnalysisException("Fail to parse the Surefire report: " + report, e);
            }
        }
    }

    private void save(UnitTestIndex index, SensorContext context) {
        long negativeTimeTestNumber = 0;
        Map<InputFile, UnitTestClassReport> indexByInputFile = mapToInputFile(index.getIndexByClassname());
        for (Map.Entry<InputFile, UnitTestClassReport> entry : indexByInputFile.entrySet()) {
            UnitTestClassReport report = entry.getValue();
            if (report.getTests() > 0) {
                negativeTimeTestNumber += report.getNegativeTimeTestNumber();
                save(report, entry.getKey(), context);
            }
        }
        if (negativeTimeTestNumber > 0) {
            LOGGER.warn("There is {} test(s) reported with negative time by surefire, total duration may not be accurate.", negativeTimeTestNumber);
        }
    }

    private Map<InputFile, UnitTestClassReport> mapToInputFile(Map<String, UnitTestClassReport> indexByClassname) {
        Map<InputFile, UnitTestClassReport> result = new HashMap<>();
        indexByClassname.forEach((className, index) -> {
            InputFile resource = getUnitTestResource(className, index);
            if (resource != null) {
                UnitTestClassReport report = result.computeIfAbsent(resource, r -> new UnitTestClassReport());
                // in case of repeated/parameterized tests (JUnit 5.x) we may end up with tests having the same name
                index.getResults().forEach(report::add);
            } else {
                LOGGER.debug("Resource not found: {}", className);
            }
        });
        return result;
    }

    private void save(UnitTestClassReport report, InputFile inputFile, SensorContext context) {
        int testsCount = report.getTests() - report.getSkipped();
        saveMeasure(context, inputFile, CoreMetrics.SKIPPED_TESTS, report.getSkipped());
        saveMeasure(context, inputFile, CoreMetrics.TESTS, testsCount);
        saveMeasure(context, inputFile, CoreMetrics.TEST_ERRORS, report.getErrors());
        saveMeasure(context, inputFile, CoreMetrics.TEST_FAILURES, report.getFailures());
        saveMeasure(context, inputFile, CoreMetrics.TEST_EXECUTION_TIME, report.getDurationMilliseconds());
        saveResults(inputFile, report);
    }

    protected void saveResults(InputFile testFile, UnitTestClassReport report) {
        for (UnitTestResult unitTestResult : report.getResults()) {
            MutableTestPlan testPlan = perspectives.as(MutableTestPlan.class, testFile);
            if (testPlan != null) {
                testPlan.addTestCase(unitTestResult.getName())
                        .setDurationInMs(Math.max(unitTestResult.getDurationMilliseconds(), 0))
                        .setStatus(TestCase.Status.of(unitTestResult.getStatus()))
                        .setMessage(unitTestResult.getMessage())
                        .setStackTrace(unitTestResult.getStackTrace());
            }
        }
    }

    @CheckForNull
    private InputFile getUnitTestResource(String className, UnitTestClassReport unitTestClassReport) {
        return TestFileFinders.getInstance().getUnitTestResource(fileSystem, className);
    }

    private static <T extends Serializable> void saveMeasure(SensorContext context, InputFile inputFile, Metric<T> metric, T value) {
        context.<T>newMeasure().forMetric(metric).on(inputFile).withValue(value).save();
    }

}
