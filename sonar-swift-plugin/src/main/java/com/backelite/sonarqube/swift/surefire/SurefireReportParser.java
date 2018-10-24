/**
 * backelite-sonar-swift-plugin - Enables analysis of Swift and Objective-C projects into SonarQube.
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
package com.backelite.sonarqube.swift.surefire;

import com.backelite.sonarqube.commons.MeasureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SurefireReportParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(SurefireReportParser.class);
    private static final String TESTSUITE = "testsuite";
    private static final String TESTCASE = "testcase";

    protected final SensorContext context;
    private final DocumentBuilderFactory dbfactory;
    private final UnitTestIndex index;

    protected SurefireReportParser(SensorContext context) {
        this.context = context;
        this.dbfactory = DocumentBuilderFactory.newInstance();
        this.index = new UnitTestIndex();
    }

    public void parseReport(final File xmlFile) {
        try {
            DocumentBuilder builder = dbfactory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            collectTestSuites(document.getElementsByTagName(TESTSUITE));
        } catch (final FileNotFoundException e) {
            LOGGER.error("Cobertura Report not found {}", xmlFile, e);
        } catch (final IOException e) {
            LOGGER.error("Error processing file named {}", xmlFile, e);
        } catch (final ParserConfigurationException e) {
            LOGGER.error("Error in parser config {}", e);
        } catch (final SAXException e) {
            LOGGER.error("Error processing file named {}", xmlFile, e);
        }
    }

    private void collectTestSuites(NodeList nodeList) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String testSuiteClassName = element.getAttribute("name");
                if(testSuiteClassName != null && !testSuiteClassName.contains("$")){
                    NodeList nl = element.getElementsByTagName(TESTCASE);
                    collectTestCases(testSuiteClassName, nl);
                }
            }
        }
    }

    private void collectTestCases(String testSuiteName, NodeList nodeList) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                String className = element.getAttribute("classname");
                if(className != null && className.endsWith(")") && className.indexOf("(") >= 0){
                    className = className.substring(0, className.indexOf("("));
                }
                if(className == null || className.trim().isEmpty())
                    className = testSuiteName;

                String testName = element.getAttribute("name");
                if(className != null && className.contains("$"))
                    testName = className.substring(className.indexOf("$")) + "/" + testName;

                UnitTestClassReport classReport = index.index(className);
                UnitTestResult detail = new UnitTestResult();
                detail.setName(testName);

                String status = UnitTestResult.STATUS_OK;
                Long duration = parseDuration(element.getAttribute("time"));
                if(element.hasChildNodes()){
                    Element item = (Element) element.getChildNodes().item(0);
                    if ("skipped".equals(item.getLocalName())) {
                        status = UnitTestResult.STATUS_SKIPPED;
                        duration = 0L;

                    } else if ("failure".equals(item.getLocalName())) {
                        status = UnitTestResult.STATUS_FAILURE;
                        detail.setMessage(item.getAttribute("message"));
                        detail.setStackTrace(item.getTextContent());

                    } else if ("error".equals(item.getLocalName())) {
                        status = UnitTestResult.STATUS_ERROR;
                        detail.setMessage(item.getAttribute("message"));
                        detail.setStackTrace(item.getTextContent());
                    }
                }
                detail.setDurationMilliseconds(duration);
                detail.setStatus(status);
                classReport.add(detail);
            }
        }
    }

    private Long parseDuration(String value) {
        Long duration = null;
        if(value != null && !value.isEmpty()){
            Double time = Double.parseDouble(value);
            duration = (long)(time * 1000);
        }
        return duration;
    }

    public void save() {
        long negativeTimeTestNumber = 0;
        int testsCount = 0;
        int testsSkipped = 0;
        int testsErrors = 0;
        int testsFailures = 0;
        long testsTime = 0;

        for (UnitTestClassReport report : index.getIndexByClassname().values()) {
            testsCount += report.getTests() - report.getSkipped();
            testsSkipped += report.getSkipped();
            testsErrors += report.getErrors();
            testsFailures += report.getFailures();

            if (report.getTests() > 0) {
                negativeTimeTestNumber += report.getNegativeTimeTestNumber();
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
}
