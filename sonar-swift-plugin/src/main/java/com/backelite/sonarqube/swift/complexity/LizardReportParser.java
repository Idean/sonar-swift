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
package com.backelite.sonarqube.swift.complexity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputModule;
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

public class LizardReportParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(LizardReportParser.class);
    private static final String MEASURE = "measure";
    private static final String MEASURE_TYPE = "type";
    private static final String MEASURE_LABELS = "label";
    private static final String MEASURE_ITEM = "item";
    private static final String FILE_MEASURE = "file";
    private static final String FUNCTION_MEASURE = "function";
    private static final String NAME = "name";
    private static final String VALUE = "value";
    private static final String LINE_COUNT_LABEL = "NCSS";
    private static final String CYCLOMATIC_COMPLEXITY_LABEL = "CCN";
    private static final String FUNCTION_COUNT_LABEL = "Functions";
    private final SensorContext context;
    private final DocumentBuilderFactory dbfactory;
    private int lineCountIndex;
    private int cyclomaticComplexityIndex;
    private int functionCountIndex;

    public LizardReportParser(final SensorContext context) {
        this.context = context;
        this.dbfactory = DocumentBuilderFactory.newInstance();
    }

    public void parseReport(final File xmlFile) {
        try {
            DocumentBuilder builder = dbfactory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            parseFile(document);
        } catch (final FileNotFoundException e) {
            LOGGER.error("Lizard Report not found {}", xmlFile, e);
        } catch (final IOException e) {
            LOGGER.error("Error processing file named {}", xmlFile, e);
        } catch (final ParserConfigurationException e) {
            LOGGER.error("Error parsing file named {}", xmlFile, e);
        } catch (final SAXException e) {
            LOGGER.error("Error processing file named {}", xmlFile, e);
        }
    }

    private void parseFile(Document document) {
        NodeList nodeList = document.getElementsByTagName(MEASURE);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                updateIndexes(element.getElementsByTagName(MEASURE_LABELS));

                parseMeasure(element.getAttribute(MEASURE_TYPE), element.getElementsByTagName(MEASURE_ITEM));
            }
        }
    }

    private void updateIndexes(NodeList nodeList) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String label = element.getTextContent();
                if(LINE_COUNT_LABEL.equalsIgnoreCase(label))
                    lineCountIndex = i;
                else if(CYCLOMATIC_COMPLEXITY_LABEL.equalsIgnoreCase(label))
                    cyclomaticComplexityIndex = i;
                else if(FUNCTION_COUNT_LABEL.equalsIgnoreCase(label))
                    functionCountIndex = i;
            }
        }
    }

    private void parseMeasure(String type, NodeList itemList) {
        for (int i = 0; i < itemList.getLength(); i++) {
            Node item = itemList.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                Element itemElement = (Element) item;
                String name = itemElement.getAttribute(NAME);

                NodeList values = itemElement.getElementsByTagName(VALUE);
                if (FILE_MEASURE.equalsIgnoreCase(type)) {
                    addComplexityFileMeasures(name,values);
                } else if (FUNCTION_MEASURE.equalsIgnoreCase(type)) {
                    addComplexityFunctionMeasures(new SwiftFunction(name),values);
                }
            }
        }
    }

    private void addComplexityFileMeasures(String fileName, NodeList values) {
        FilePredicate fp = context.fileSystem().predicates().hasRelativePath(fileName);
        if(!context.fileSystem().hasFiles(fp)){
            LOGGER.warn("file not included in sonar {}", fileName);
            return;
        }
        InputFile component = context.fileSystem().inputFile(fp);
        int complexity = Integer.parseInt(values.item(cyclomaticComplexityIndex).getTextContent());
        context.<Integer>newMeasure()
            .on(component)
            .forMetric(CoreMetrics.COMPLEXITY)
            .withValue(complexity)
            .save();

        int numberOfFunctions = Integer.parseInt(values.item(functionCountIndex).getTextContent());
        context.<Integer>newMeasure()
            .on(component)
            .forMetric(CoreMetrics.FUNCTIONS)
            .withValue(numberOfFunctions)
            .save();

        int numberOfLines = Integer.parseInt(values.item(lineCountIndex).getTextContent());
        context.<Integer>newMeasure()
            .on(component)
            .forMetric(CoreMetrics.LINES)
            .withValue(numberOfLines)
            .save();
    }

    private void addComplexityFunctionMeasures(SwiftFunction component, NodeList values) {
        int complexity = Integer.parseInt(values.item(cyclomaticComplexityIndex).getTextContent());
        context.<Integer>newMeasure()
            .on(component)
            .forMetric(CoreMetrics.COMPLEXITY)
            .withValue(complexity)
            .save();

        int numberOfLines = Integer.parseInt(values.item(lineCountIndex).getTextContent());
        context.<Integer>newMeasure()
            .on(component)
            .forMetric(CoreMetrics.LINES)
            .withValue(numberOfLines)
            .save();
    }

    private static class SwiftFunction implements InputModule {
        private String name;
        private String key;
        private String file;
        private int lineNumber;

        public SwiftFunction(String name) {
            String[] vals = name.split(" ");
            if(vals.length >= 3){
                this.name = vals[0].substring(0,vals[0].indexOf("("));
                this.file = vals[2].substring(0,vals[2].lastIndexOf(":"));
                this.lineNumber = Integer.parseInt(vals[2].substring(vals[2].lastIndexOf(":")+1));
                this.key = file.substring(0,file.lastIndexOf('.')+1)+name;
            }else{
                this.key = name;
            }
        }

        @Override
        public String key() {
            return key;
        }

        public String getName(){
            return name;
        }

        public String getFile(){
            return file;
        }

        public int getLineNumber(){
            return lineNumber;
        }

        @Override
        public boolean isFile() {
            return false;
        }
    }
}
