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
import java.util.ArrayList;
import java.util.List;

public class LizardReportParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(LizardReportParser.class);
    private static final String MEASURE = "measure";
    private static final String MEASURE_TYPE = "type";
    private static final String MEASURE_ITEM = "item";
    private static final String FILE_MEASURE = "file";
    private static final String FUNCTION_MEASURE = "Function";
    private static final String NAME = "name";
    private static final String VALUE = "value";
    private static final int CYCLOMATIC_COMPLEXITY_INDEX = 2;
    private static final int FUNCTIONS_INDEX = 3;
    private final SensorContext context;

    public LizardReportParser(final SensorContext context) {
        this.context = context;
    }

    public void parseReport(final File xmlFile) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            parseFile(document);
        } catch (final FileNotFoundException e) {
            LoggerFactory.getLogger(getClass()).error("Lizard Report not found {}", xmlFile, e);
        } catch (final IOException e) {
            LoggerFactory.getLogger(getClass()).error("Error processing file named {}", xmlFile, e);
        } catch (final ParserConfigurationException e) {
            LoggerFactory.getLogger(getClass()).error("Error parsing file named {}", xmlFile, e);
        } catch (final SAXException e) {
            LoggerFactory.getLogger(getClass()).error("Error processing file named {}", xmlFile, e);
        }
    }

    private void parseFile(Document document) {
        NodeList nodeList = document.getElementsByTagName(MEASURE);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                NodeList nl = element.getElementsByTagName(MEASURE_ITEM);
                parseMeasure(element.getAttribute(MEASURE_TYPE), nl);
            }
        }
    }

    private void parseMeasure(String type, NodeList itemList) {
        List<SwiftFunction> functions = new ArrayList<>();
        for (int i = 0; i < itemList.getLength(); i++) {
            Node item = itemList.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                Element itemElement = (Element) item;
                String name = itemElement.getAttribute(NAME);

                NodeList values = itemElement.getElementsByTagName(VALUE);
                if (FILE_MEASURE.equalsIgnoreCase(type)) {
                    addComplexityFileMeasures(name,values);
                } else if (FUNCTION_MEASURE.equalsIgnoreCase(type)) {
                    String measure = values.item(CYCLOMATIC_COMPLEXITY_INDEX).getTextContent();
                    functions.add(new SwiftFunction(name, Integer.parseInt(measure)));
                }
            }
        }
        if (!functions.isEmpty()) {
            addComplexityFunctionMeasures(functions);
        }
    }

    private void addComplexityFileMeasures(String fileName, NodeList values) {
        File file = new File(fileName);
        FilePredicate fp = context.fileSystem().predicates().hasAbsolutePath(file.getAbsolutePath());
        if(!context.fileSystem().hasFiles(fp)){
            LOGGER.warn("file not included in sonar {}", fileName);
            return;
        }
        InputFile component = context.fileSystem().inputFile(fp);
        int complexity = Integer.parseInt(values.item(CYCLOMATIC_COMPLEXITY_INDEX).getTextContent());
        context.<Integer>newMeasure()
            .on(component)
            .forMetric(CoreMetrics.COMPLEXITY)
            .withValue(complexity)
            .save();

        int numberOfFunctions = Integer.parseInt(values.item(FUNCTIONS_INDEX).getTextContent());
        context.<Integer>newMeasure()
            .on(component)
            .forMetric(CoreMetrics.FUNCTIONS)
            .withValue(numberOfFunctions)
            .save();

        double fileComplexity = Double.parseDouble(values.item(CYCLOMATIC_COMPLEXITY_INDEX).getTextContent());
        context.<Double>newMeasure()
            .on(component)
            .forMetric(CoreMetrics.FILE_COMPLEXITY)
            .withValue(fileComplexity)
            .save();
    }

    private void addComplexityFunctionMeasures(List<SwiftFunction> functions) {
        int count = 0;
        int complexityInFunctions = 0;

        for (SwiftFunction func : functions) {
            count++;
            complexityInFunctions += func.getCyclomaticComplexity();

            context.<Integer>newMeasure()
                .on(func)
                .forMetric(CoreMetrics.COMPLEXITY_IN_FUNCTIONS)
                .withValue(complexityInFunctions)
                .save();
        }

        if (count != 0) {
            context.<Double>newMeasure()
                .forMetric(CoreMetrics.FUNCTION_COMPLEXITY)
                .withValue(((double)complexityInFunctions)/count)
                .save();
        }
    }

    private static class SwiftFunction implements InputModule {
        private String key;
        private int cyclomaticComplexity;

        public SwiftFunction(String functionName, int cyclomaticComplexity) {
            this.key = functionName;
            this.cyclomaticComplexity = cyclomaticComplexity;
        }

        public int getCyclomaticComplexity() {
            return cyclomaticComplexity;
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
}
