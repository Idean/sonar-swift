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
package org.sonar.plugins.swift.complexity;

import org.slf4j.LoggerFactory;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.RangeDistributionBuilder;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LizardReportParser {

    private final Number[] FUNCTIONS_DISTRIB_BOTTOM_LIMITS = {1, 2, 4, 6, 8, 10, 12, 20, 30};
    private final Number[] FILES_DISTRIB_BOTTOM_LIMITS = {0, 5, 10, 20, 30, 60, 90};

    private static final String MEASURE = "measure";
    private static final String MEASURE_TYPE = "type";
    private static final String MEASURE_ITEM = "item";
    private static final String FILE_MEASURE = "file";
    private static final String FUNCTION_MEASURE = "Function";
    private static final String NAME = "name";
    private static final String VALUE = "value";
    private static final int CYCLOMATIC_COMPLEXITY_INDEX = 2;
    private static final int FUNCTIONS_INDEX = 3;

    public Map<String, List<Measure>> parseReport(final File xmlFile) {
        Map<String, List<Measure>> result = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            result = parseFile(document);
        } catch (final FileNotFoundException e){
            LoggerFactory.getLogger(getClass()).error("Lizard Report not found {}", xmlFile, e);
        } catch (final IOException e) {
            LoggerFactory.getLogger(getClass()).error("Error processing file named {}", xmlFile, e);
        } catch (final ParserConfigurationException e) {
            LoggerFactory.getLogger(getClass()).error("Error parsing file named {}", xmlFile, e);
        } catch (final SAXException e) {
            LoggerFactory.getLogger(getClass()).error("Error processing file named {}", xmlFile, e);
        }

        return result;
    }

    private Map<String, List<Measure>> parseFile(Document document) {
        final Map<String, List<Measure>> reportMeasures = new HashMap<String, List<Measure>>();
        final List<SwiftFunction> functions = new ArrayList<SwiftFunction>();

        NodeList nodeList = document.getElementsByTagName(MEASURE);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (element.getAttribute(MEASURE_TYPE).equalsIgnoreCase(FILE_MEASURE)) {
                    NodeList itemList = element.getElementsByTagName(MEASURE_ITEM);
                    addComplexityFileMeasures(itemList, reportMeasures);
                } else if(element.getAttribute(MEASURE_TYPE).equalsIgnoreCase(FUNCTION_MEASURE)) {
                    NodeList itemList = element.getElementsByTagName(MEASURE_ITEM);
                    collectFunctions(itemList, functions);
                }
            }
        }

        addComplexityFunctionMeasures(reportMeasures, functions);

        return reportMeasures;
    }

    private void addComplexityFileMeasures(NodeList itemList, Map<String, List<Measure>> reportMeasures){
        for (int i = 0; i < itemList.getLength(); i++) {
            Node item = itemList.item(i);

            if (item.getNodeType() == Node.ELEMENT_NODE) {
                Element itemElement = (Element) item;
                String fileName = itemElement.getAttribute(NAME);
                NodeList values = itemElement.getElementsByTagName(VALUE);
                int complexity = Integer.parseInt(values.item(CYCLOMATIC_COMPLEXITY_INDEX).getTextContent());
                double fileComplexity = Double.parseDouble(values.item(CYCLOMATIC_COMPLEXITY_INDEX).getTextContent());
                int numberOfFunctions =  Integer.parseInt(values.item(FUNCTIONS_INDEX).getTextContent());

                reportMeasures.put(fileName, buildMeasureList(complexity, fileComplexity, numberOfFunctions));
            }
        }
    }

    private List<Measure> buildMeasureList(int complexity, double fileComplexity, int numberOfFunctions){
        List<Measure> list = new ArrayList<Measure>();
        list.add(new Measure(CoreMetrics.COMPLEXITY).setIntValue(complexity));
        list.add(new Measure(CoreMetrics.FUNCTIONS).setIntValue(numberOfFunctions));
        list.add(new Measure(CoreMetrics.FILE_COMPLEXITY, fileComplexity));
        RangeDistributionBuilder complexityDistribution = new RangeDistributionBuilder(CoreMetrics.FILE_COMPLEXITY_DISTRIBUTION, FILES_DISTRIB_BOTTOM_LIMITS);
        complexityDistribution.add(fileComplexity);
        return list;
    }

    private void collectFunctions(NodeList itemList, List<SwiftFunction> functions) {
        for (int i = 0; i < itemList.getLength(); i++) {
            Node item = itemList.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                Element itemElement = (Element) item;
                String name = itemElement.getAttribute(NAME);
                String measure = itemElement.getElementsByTagName(VALUE).item(CYCLOMATIC_COMPLEXITY_INDEX).getTextContent();
                functions.add(new SwiftFunction(name, Integer.parseInt(measure)));
            }
        }
    }

    private void addComplexityFunctionMeasures(Map<String, List<Measure>> reportMeasures, List<SwiftFunction> functions){
        for (Map.Entry<String, List<Measure>> entry : reportMeasures.entrySet()) {

            RangeDistributionBuilder complexityDistribution = new RangeDistributionBuilder(CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION, FUNCTIONS_DISTRIB_BOTTOM_LIMITS);
            int count = 0;
            int complexityInFunctions = 0;

            for (SwiftFunction func : functions) {
                if (func.getName().contains(entry.getKey())) {
                    complexityDistribution.add(func.getCyclomaticComplexity());
                    count++;
                    complexityInFunctions += func.getCyclomaticComplexity();
                }
            }

            if (count != 0) {
                double complex = 0;
                for (Measure m : entry.getValue()){
                    if (m.getMetric().getKey().equalsIgnoreCase(CoreMetrics.FILE_COMPLEXITY.getKey())){
                        complex = m.getValue();
                        break;
                    }
                }

                double complexMean = complex/(double)count;
                entry.getValue().addAll(buildFuncionMeasuresList(complexMean, complexityInFunctions, complexityDistribution));
            }
        }
    }

    public List<Measure> buildFuncionMeasuresList(double complexMean, int complexityInFunctions, RangeDistributionBuilder builder){
        List<Measure> list = new ArrayList<Measure>();
        list.add(new Measure(CoreMetrics.FUNCTION_COMPLEXITY, complexMean));
        list.add(new Measure(CoreMetrics.COMPLEXITY_IN_FUNCTIONS).setIntValue(complexityInFunctions));
        return list;
    }

    private class SwiftFunction {
        private String name;
        private int cyclomaticComplexity;

        public SwiftFunction(String name, int cyclomaticComplexity) {
            this.name = name;
            this.cyclomaticComplexity = cyclomaticComplexity;
        }

        public String getName() {
            return name;
        }

        public int getCyclomaticComplexity() {
            return cyclomaticComplexity;
        }

    }
}
