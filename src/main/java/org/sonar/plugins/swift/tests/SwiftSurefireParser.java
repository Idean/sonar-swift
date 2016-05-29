/*
 * SonarQube Swift Plugin
 * Copyright (C) 2015 Backelite
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.swift.tests;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.api.utils.StaxParser;
import org.sonar.api.utils.WildcardPattern;
import org.sonar.api.utils.XmlParserException;
import org.sonar.plugins.surefire.TestCaseDetails;
import org.sonar.plugins.surefire.TestSuiteParser;
import org.sonar.plugins.surefire.TestSuiteReport;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class SwiftSurefireParser {

    private static final Logger LOG = LoggerFactory.getLogger(SwiftSurefireParser.class);

    private static final String INCLUDE_KEY = "sonar.junit.include";
    private static final String DEFAULT_INCLUDE = "*.junit";

    private final Project project;
    private final FileSystem fileSystem;
    private final SensorContext context;
    private final Settings settings;

    SwiftSurefireParser(Project project, FileSystem fileSystem, final Settings config, SensorContext context) {
        this.settings = config;
        this.project = project;
        this.fileSystem = fileSystem;
        this.context = context;
    }

    void collect(File reportsDir) {

        File[] xmlFiles = getReports(reportsDir);

        if (xmlFiles.length == 0) {
            insertZeroWhenNoReports();
        } else {
            parseFiles(xmlFiles);
        }
    }

    private String inclusionPattern() {
        String inclusionPattern = settings.getString(INCLUDE_KEY);
        if (inclusionPattern == null) {
            return DEFAULT_INCLUDE;
        }
        return inclusionPattern;
    }

    private File[] getReports(File dir) {

        if (dir == null || !dir.isDirectory() || !dir.exists()) {
            return new File[0];
        }

        final WildcardPattern matcher = WildcardPattern.create(inclusionPattern());

        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return matcher.match(name);
            }
        });
    }

    private void insertZeroWhenNoReports() {
        context.saveMeasure(CoreMetrics.TESTS, 0.0);
    }

    private void parseFiles(File[] reports) {

        Set<TestSuiteReport> analyzedReports = new HashSet<TestSuiteReport>();

        try {

            for (File report : reports) {
                TestSuiteParser parserHandler = new TestSuiteParser();
                StaxParser parser = new StaxParser(parserHandler, false);
                parser.parse(report);

                for (TestSuiteReport fileReport : parserHandler.getParsedReports()) {


                    if ( !fileReport.isValid() || analyzedReports.contains(fileReport)) {
                        continue;
                    }

                    if (fileReport.getTests() > 0) {
                        double testsCount = fileReport.getTests() - fileReport.getSkipped();
                        saveClassMeasure(fileReport, CoreMetrics.SKIPPED_TESTS, fileReport.getSkipped());
                        saveClassMeasure(fileReport, CoreMetrics.TESTS, testsCount);
                        saveClassMeasure(fileReport, CoreMetrics.TEST_ERRORS, fileReport.getErrors());
                        saveClassMeasure(fileReport, CoreMetrics.TEST_FAILURES, fileReport.getFailures());
                        saveClassMeasure(fileReport, CoreMetrics.TEST_EXECUTION_TIME, fileReport.getTimeMS());
                        double passedTests = testsCount - fileReport.getErrors() - fileReport.getFailures();
                        if (testsCount > 0) {
                            double percentage = passedTests * 100d / testsCount;
                            saveClassMeasure(fileReport, CoreMetrics.TEST_SUCCESS_DENSITY, ParsingUtils.scaleValue(percentage));
                        }
                        saveTestsDetails(fileReport);
                        analyzedReports.add(fileReport);
                    }
                }
            }

        } catch (Exception e) {
            throw new XmlParserException("Cannot parse surefire reports", e);
        }
    }

    private void saveTestsDetails(TestSuiteReport fileReport) throws TransformerException {

        StringBuilder testCaseDetails = new StringBuilder(256);
        testCaseDetails.append("<tests-details>");
        List<TestCaseDetails> details = fileReport.getDetails();
        for (TestCaseDetails detail : details) {
            testCaseDetails.append("<testcase status=\"").append(detail.getStatus())
                    .append("\" time=\"").append(detail.getTimeMS())
                    .append("\" name=\"").append(detail.getName()).append("\"");
            boolean isError = detail.getStatus().equals(TestCaseDetails.STATUS_ERROR);
            if (isError || detail.getStatus().equals(TestCaseDetails.STATUS_FAILURE)) {
                testCaseDetails.append(">")
                        .append(isError ? "<error message=\"" : "<failure message=\"")
                        .append(StringEscapeUtils.escapeXml(detail.getErrorMessage())).append("\">")
                        .append("<![CDATA[").append(StringEscapeUtils.escapeXml(detail.getStackTrace())).append("]]>")
                        .append(isError ? "</error>" : "</failure>").append("</testcase>");
            } else {
                testCaseDetails.append("/>");
            }
        }
        testCaseDetails.append("</tests-details>");
        context.saveMeasure(getUnitTestResource(fileReport.getClassKey()), new Measure(CoreMetrics.TEST_DATA, testCaseDetails.toString()));
    }

    private void saveClassMeasure(TestSuiteReport fileReport, Metric metric, double value) {
        if ( !Double.isNaN(value)) {
            context.saveMeasure(getUnitTestResource(fileReport.getClassKey()), metric, value);
        }
    }

    private Resource getUnitTestResource(String classname) {
        String fileName = classname.replace('.', '/') + ".swift";
        String wildcardFileName = classname.replace(".", "/**/") + ".swift";

        File file = new File(fileName);
        if (!file.isAbsolute()) {
            file = new File(fileSystem.baseDir(), fileName);
        }

        /*
         * Most xcodebuild JUnit parsers don't include the path to the class in the class field, so search for it if it
         * wasn't found in the root.
         */
        if (!file.isFile() || !file.exists()) {
            List<File> files = ImmutableList.copyOf(fileSystem.files(fileSystem.predicates().and(
                    fileSystem.predicates().hasType(InputFile.Type.TEST),
                    fileSystem.predicates().matchesPathPattern("**/" + wildcardFileName))));

            if (files.isEmpty()) {
                LOG.info("Unable to locate test source file {}", wildcardFileName);
            } else {
                /*
                 * Lazily get the first file, since we wouldn't be able to determine the correct one from just the
                 * test class name in the event that there are multiple matches.
                 */
                file = files.get(0);
            }
        }

        org.sonar.api.resources.File sonarFile = org.sonar.api.resources.File.fromIOFile(file, project);
        sonarFile.setQualifier(Qualifiers.UNIT_TEST_FILE);
        return sonarFile;
    }
}
