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
import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputComponent;
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
                    InputFile inputFile = getFile(name);
                    if (inputFile != null) {
                        addComplexityFileMeasures(inputFile, values);
                    }
                } else if (FUNCTION_MEASURE.equalsIgnoreCase(type)) {
                    addComplexityFunctionMeasures(new SwiftFunction(0,name), values);
                }
            }
        }
    }

    private InputFile getFile(String fileName){
        FilePredicate fp = context.fileSystem().predicates().hasRelativePath(fileName);
        if(!context.fileSystem().hasFiles(fp)){
            LOGGER.warn("file not included in sonar {}", fileName);
            return null;
        }
        return context.fileSystem().inputFile(fp);
    }

    static class SwiftFunction extends DefaultInputComponent implements InputComponent {
        private String name;
        private String key;
        private String file;
        private int lineNumber;
        SwiftFunction(int scannerId, String name) {
            super(scannerId);
            String[] vals = name.split(" at ");
            if (vals.length >= 2) {
                this.name = vals[0].replaceAll("\\W","");

                if (vals[1].contains(":")) {
                    String[] sp = vals[1].split(":");
                    this.file = sp[0].substring(0,sp[0].lastIndexOf("."));
                    this.lineNumber = Integer.parseInt(sp[1]);
                } else {
                    this.file = vals[1];
                    this.lineNumber = 0;
                }

                this.key = String.format("%s.%s:%d", this.file, this.name, this.lineNumber);
            } else {
                this.key = name;
            }
        }
        @Override
        public String key() {
            return key;
        }
        @Override
        public boolean isFile() {
            return false;
        }
    }

    private void addComplexityFileMeasures(InputFile component, NodeList values) {
        LOGGER.debug("File measures for {}",component.toString());
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

    private void addComplexityFunctionMeasures(InputComponent component, NodeList values) {
        LOGGER.debug("Function measures for {}",component.key());
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
}
