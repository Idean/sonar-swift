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
package com.backelite.sonarqube.swift.coverage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.coverage.NewCoverage;
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

public final class CoberturaReportParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoberturaReportParser.class);
    private static final String PACKAGES = "packages";
    private static final String CLASSES = "class";
    private static final String FILE = "filename";
    private static final String LINE = "line";
    private static final String NUMBER = "number";
    private static final String HITS = "hits";
    private static final String BRANCH = "branch";
    private static final String COVERAGE = "condition-coverage";
    private final SensorContext context;
    private final DocumentBuilderFactory dbfactory;

    public CoberturaReportParser(SensorContext context) {
        this.context = context;
        this.dbfactory = DocumentBuilderFactory.newInstance();
    }
    public testSonar(){

    }
    public void parseReport(final File xmlFile) {
        try {
            DocumentBuilder builder = dbfactory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            collectPackageMeasures(document.getElementsByTagName(PACKAGES));
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

    public void collectPackageMeasures(NodeList nodeList) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                NodeList nl = element.getElementsByTagName(CLASSES);
                collectClassMeasures(nl);
            }
        }
    }

    private void collectClassMeasures(NodeList nodeList) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String filePath = element.getAttribute(FILE);
                NodeList nl = element.getElementsByTagName(LINE);
                collectFileData(filePath, nl);
            }
        }
    }

    private void collectFileData(String filePath, NodeList nodeList) {
        InputFile resource = getFile(filePath);
        LOGGER.info("Collect file data: {}",resource.toString());
        if (resource != null) {
            boolean lineAdded = false;
            NewCoverage coverage = context.newCoverage();
            coverage.onFile(resource);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    int lineId = Integer.parseInt(element.getAttribute(NUMBER));
                    coverage.lineHits(lineId, Integer.parseInt(element.getAttribute(HITS)));
                    lineAdded = true;

                    String isBranch = element.getAttribute(BRANCH);
                    String text = element.getAttribute(COVERAGE);
                    if ("true".equalsIgnoreCase(isBranch) && text != null && !text.isEmpty())
                        addCoverageConditions(coverage,lineId,text);
                }
            }
            // If there was no lines covered or uncovered (e.g. everything is ignored), but the file exists then Sonar would report the file as uncovered
            // so adding a fake one to line number 1
            if (!lineAdded) {
                coverage.lineHits(1, 1);
            }
            coverage.save();
        }
    }

    private void addCoverageConditions(NewCoverage coverage, int line, String text) {
        int start = text.indexOf("(");
        int end = text.indexOf(")");
        if(start != -1 && end != -1){
            String[] conditions = text.substring(start + 1, end).split("/");
            coverage.conditions(line, Integer.parseInt(conditions[1]), Integer.parseInt(conditions[0]));
        }
    }

    private InputFile getFile(String filePath) {
        FilePredicate fp = context.fileSystem().predicates().hasPath(filePath);
        if(context.fileSystem().hasFiles(fp))
            return context.fileSystem().inputFile(fp);
        LOGGER.warn("Can't find file {}",filePath);
        return null;
    }

}

