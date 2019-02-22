/**
 * Swift SonarQube Plugin - Objective-C module - Enables analysis of Swift and Objective-C projects into SonarQube.
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
package com.backelite.sonarqube.objectivec.issues.oclint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.batch.sensor.issue.internal.DefaultIssueLocation;
import org.sonar.api.rule.RuleKey;
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

final class OCLintParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(OCLintParser.class);
    private static final String FILE = "file";
    private static final String FILENAME = "name";
    private static final String VIOLATION = "violation";
    private static final String LINE = "beginline";
    private static final String RULE = "rule";
    private final SensorContext context;
    private final DocumentBuilderFactory dbfactory;

    public OCLintParser(SensorContext context) {
        this.context = context;
        this.dbfactory = DocumentBuilderFactory.newInstance();
    }

    public void parseReport(final File xmlFile) {
        try {
            DocumentBuilder builder = dbfactory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            parseFiles(document.getElementsByTagName(FILE));
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

    private void parseFiles(NodeList nodeList) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                String filePath = element.getAttribute(FILENAME);
                NodeList nl = element.getElementsByTagName(VIOLATION);
                collectFileViolations(filePath,nl);
            }
        }
    }

    private void collectFileViolations(String filePath, NodeList nodeList) {
        File file = new File(filePath);
        FilePredicate fp = context.fileSystem().predicates().hasAbsolutePath(file.getAbsolutePath());
        if(!context.fileSystem().hasFiles(fp)){
            LOGGER.warn("file not included in sonar {}", filePath);
        }
        InputFile inputFile = context.fileSystem().inputFile(fp);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                NewIssueLocation dil = new DefaultIssueLocation()
                    .on(inputFile)
                    .at(inputFile.selectLine(Integer.valueOf(element.getAttribute(LINE))))
                    .message(element.getTextContent());
                context.newIssue()
                    .forRule(RuleKey.of(OCLintRulesDefinition.REPOSITORY_KEY, element.getAttribute(RULE)))
                    .at(dil)
                    .save();
            }
        }
    }
}
