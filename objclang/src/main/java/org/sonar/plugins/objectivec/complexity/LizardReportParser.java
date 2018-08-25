/**
 * Objective-C Language - Enables analysis of Swift projects into SonarQube.
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
package org.sonar.plugins.objectivec.complexity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.measures.*;
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

/**
 * This class parses xml Reports form the tool Lizard in order to extract this measures:
 *      COMPLEXITY, FUNCTIONS, FUNCTION_COMPLEXITY, FUNCTION_COMPLEXITY_DISTRIBUTION,
 *      FILE_COMPLEXITY, FUNCTION_COMPLEXITY_DISTRIBUTION, COMPLEXITY_IN_FUNCTIONS
 *
 * @author Andres Gil Herrera
 * @since 28/05/15
 */
public class LizardReportParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(LizardReportParser.class);

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

    /**
     *
     * @param xmlFile lizard xml report
     * @return Map containing as key the name of the file and as value a list containing the measures for that file
     */
    public Map<String, List<Measure>> parseReport(final File xmlFile) {
        Map<String, List<Measure>> result = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            result = parseFile(document);
        } catch (final FileNotFoundException e){
            LOGGER.error("Lizard Report not found {}", xmlFile, e);
        } catch (final IOException e) {
            LOGGER.error("Error processing file named {}", xmlFile, e);
        } catch (final ParserConfigurationException e) {
            LOGGER.error("Error parsing file named {}", xmlFile, e);
        } catch (final SAXException e) {
            LOGGER.error("Error processing file named {}", xmlFile, e);
        }

        return result;
    }

    /**
     *
     * @param document Document object representing the lizard report
     * @return Map containing as key the name of the file and as value a list containing the measures for that file
     */
    private Map<String, List<Measure>> parseFile(Document document) {
        final Map<String, List<Measure>> reportMeasures = new HashMap<String, List<Measure>>();
        final List<ObjCFunction> functions = new ArrayList<ObjCFunction>();

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

    /**
     * This method extracts the values for COMPLEXITY, FUNCTIONS, FILE_COMPLEXITY
     *
     * @param itemList list of all items from a <measure type=file>
     * @param reportMeasures map to save the measures for each file
     */
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

    /**
     *
     * @param complexity overall complexity of the file
     * @param fileComplexity file complexity
     * @param numberOfFunctions number of functions in the file
     * @return returns a list of tree measures COMPLEXITY, FUNCTIONS, FILE_COMPLEXITY with the values specified
     */
    private List<Measure> buildMeasureList(int complexity, double fileComplexity, int numberOfFunctions){
        List<Measure> list = new ArrayList<Measure>();
        list.add(new Measure(CoreMetrics.COMPLEXITY).setIntValue(complexity));
        list.add(new Measure(CoreMetrics.FUNCTIONS).setIntValue(numberOfFunctions));
        list.add(new Measure(CoreMetrics.FILE_COMPLEXITY, fileComplexity));
        RangeDistributionBuilder complexityDistribution = new RangeDistributionBuilder(CoreMetrics.FILE_COMPLEXITY_DISTRIBUTION, FILES_DISTRIB_BOTTOM_LIMITS);
        complexityDistribution.add(fileComplexity);
        return list;
    }

    /**
     *
     * @param itemList NodeList of all items in a <measure type=function> tag
     * @param functions list to save the functions in the NodeList as ObjCFunction objects.
     */
    private void collectFunctions(NodeList itemList, List<ObjCFunction> functions) {
        for (int i = 0; i < itemList.getLength(); i++) {
            Node item = itemList.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                Element itemElement = (Element) item;
                String name = itemElement.getAttribute(NAME);
                String measure = itemElement.getElementsByTagName(VALUE).item(CYCLOMATIC_COMPLEXITY_INDEX).getTextContent();
                functions.add(new ObjCFunction(name, Integer.parseInt(measure)));
            }
        }
    }

    /**
     *
     * @param reportMeasures map to save the measures for the different files
     * @param functions list of ObjCFunction to extract the information needed to create
     *                  FUNCTION_COMPLEXITY_DISTRIBUTION, FUNCTION_COMPLEXITY, COMPLEXITY_IN_FUNCTIONS
     */
    private void addComplexityFunctionMeasures(Map<String, List<Measure>> reportMeasures, List<ObjCFunction> functions){
        for (Map.Entry<String, List<Measure>> entry : reportMeasures.entrySet()) {

            RangeDistributionBuilder complexityDistribution = new RangeDistributionBuilder(CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION, FUNCTIONS_DISTRIB_BOTTOM_LIMITS);
            int count = 0;
            int complexityInFunctions = 0;

            for (ObjCFunction func : functions) {
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
                entry.getValue().addAll(buildFunctionMeasuresList(complexMean, complexityInFunctions, complexityDistribution));
            }
        }
    }

    /**
     *
     * @param complexMean average complexity per function in a file
     * @param complexityInFunctions Entire complexity in functions
     * @param builder Builder ready to build FUNCTION_COMPLEXITY_DISTRIBUTION
     * @return list of Measures containing FUNCTION_COMPLEXITY_DISTRIBUTION, FUNCTION_COMPLEXITY and COMPLEXITY_IN_FUNCTIONS
     */
    public List<Measure> buildFunctionMeasuresList(double complexMean, int complexityInFunctions, RangeDistributionBuilder builder){
        List<Measure> list = new ArrayList<Measure>();
        list.add(new Measure(CoreMetrics.FUNCTION_COMPLEXITY, complexMean));
        list.add(new Measure(CoreMetrics.COMPLEXITY_IN_FUNCTIONS).setIntValue(complexityInFunctions));
        return list;
    }

    /**
     * helper class to process the information the functions contained in a Lizard report
     */
    private class ObjCFunction {
        private String name;
        private int cyclomaticComplexity;

        public ObjCFunction(String name, int cyclomaticComplexity) {
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