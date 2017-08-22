/*
 * SonarQube Swift Plugin
 * Copyright (C) 2015 Backelite
 * sonarqube@googlegroups.com
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

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.api.utils.StaxParser;
import org.sonar.api.utils.XmlParserException;
import org.sonar.plugins.surefire.TestCaseDetails;
import org.sonar.plugins.surefire.TestSuiteParser;
import org.sonar.plugins.surefire.TestSuiteReport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class SwiftSurefireParser {

    private static final Logger LOG = LoggerFactory.getLogger(SwiftSurefireParser.class);

    private final Project project;
    private final FileSystem fileSystem;
    private final  ResourcePerspectives resourcePerspectives;
    private final SensorContext context;

    public SwiftSurefireParser(Project project, FileSystem fileSystem, ResourcePerspectives resourcePerspectives, SensorContext context) {
        this.project = project;
        this.fileSystem = fileSystem;
        this.resourcePerspectives = resourcePerspectives;
        this.context = context;
    }

    public void collect(File reportsDir) {

        File[] xmlFiles = getReports(reportsDir);

        if (xmlFiles.length == 0) {
            insertZeroWhenNoReports(project, context);
        } else {
            parseFiles(context, xmlFiles);
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

    private void insertZeroWhenNoReports(Project pom, SensorContext context) {

        context.saveMeasure(CoreMetrics.TESTS, 0.0);
    }

    private void parseFiles(SensorContext context, File[] reports) {

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
                        String testClass = fileReport.getClassKey();
                        Resource resource = getUnitTestResource(testClass);

                        if(resource == null) {
                            LOG.warn("file for test class {} not found", testClass);
                            continue;
                        }

                        saveClassMeasure(resource, CoreMetrics.SKIPPED_TESTS, fileReport.getSkipped());
                        saveClassMeasure(resource, CoreMetrics.TESTS, testsCount);
                        saveClassMeasure(resource, CoreMetrics.TEST_ERRORS, fileReport.getErrors());
                        saveClassMeasure(resource, CoreMetrics.TEST_FAILURES, fileReport.getFailures());
                        saveClassMeasure(resource, CoreMetrics.TEST_EXECUTION_TIME, fileReport.getTimeMS());
                        double passedTests = testsCount - fileReport.getErrors() - fileReport.getFailures();
                        if (testsCount > 0) {
                            double percentage = passedTests * 100d / testsCount;
                            saveClassMeasure(resource, CoreMetrics.TEST_SUCCESS_DENSITY, ParsingUtils.scaleValue(percentage));
                        }
                        saveTestsDetails(resource, fileReport);
                        analyzedReports.add(fileReport);
                    }
                }
            }

        } catch (Exception e) {
            throw new XmlParserException("Cannot parse surefire reports", e);
        }
    }

    private void saveTestsDetails(@Nonnull Resource resource, @Nonnull TestSuiteReport fileReport) throws TransformerException {

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
    }

    private void saveClassMeasure(@Nonnull Resource resource, Metric metric, double value) {

        if (Double.isNaN(value)) {
            return;
        }
        context.saveMeasure(resource, metric, value);
    }

    private @Nullable Resource getUnitTestResource(String classname) {

        String[] classNameParts = classname.split("\\.");
        String classNameWithoutModule = classNameParts.length > 1 ? classNameParts[1] : classNameParts[0];
        String fileName = classNameWithoutModule + ".swift";

        InputFile inputFile = fileSystem.inputFile(fileSystem.predicates().matchesPathPattern("**/" + fileName));
        if (inputFile == null) {
            return null;
        }

        Resource resource = context.getResource(inputFile);

        if(resource instanceof org.sonar.api.resources.File) {
            org.sonar.api.resources.File sonarFile = (org.sonar.api.resources.File) resource;
            sonarFile.setQualifier(Qualifiers.UNIT_TEST_FILE);
        }

        return resource;
    }
}
