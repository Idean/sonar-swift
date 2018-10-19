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


import com.backelite.sonarqube.commons.StaxParser;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.codehaus.staxmate.in.SMInputCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.coverage.NewCoverage;
import org.sonar.api.utils.ParsingUtils;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.text.ParseException;
import java.util.Locale;
import java.util.Map;

final class CoberturaReportParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoberturaReportParser.class);

    private final SensorContext context;
    private final FileSystem fileSystem;

    CoberturaReportParser(final SensorContext context, final FileSystem fileSystem) {
        this.context = context;
        this.fileSystem = fileSystem;
    }

    /**
     * Parse a Cobertura xml report and create measures accordingly
     */
    void parseReport(File xmlFile) {
        parse(xmlFile);
    }

    private void collectFileMeasures(SMInputCursor clazz,
                                            Map<String, NewCoverage> builderByFilename) throws XMLStreamException {
        while (clazz.getNext() != null) {
            String fileName = clazz.getAttrValue("filename");

            NewCoverage builder = builderByFilename.get(fileName);
            if (builder == null) {

                String filePath = getAdjustedPathIfProjectIsModule(fileName);
                if (filePath == null) {
                    LOGGER.warn("File not found {}", fileName);
                    continue;
                }
                File file = new File(fileSystem.baseDir(), filePath);
                InputFile inputFile = fileSystem.inputFile(fileSystem.predicates().hasAbsolutePath(file.getAbsolutePath()));

                if (inputFile == null) {
                    LOGGER.warn("File not included in sonar {}", fileName);
                    continue;
                } else {
                    builder = context.newCoverage().onFile(inputFile);
                }
                
                builderByFilename.put(fileName, builder);
            }
            collectFileData(clazz, builder);
        }
    }

    private void collectFileData(SMInputCursor clazz,
                                 NewCoverage builder) throws XMLStreamException {
        SMInputCursor line = clazz.childElementCursor("lines").advance().childElementCursor("line");
        while (line.getNext() != null) {
            int lineId = Integer.parseInt(line.getAttrValue("number"));
            try {
                builder.lineHits(lineId, (int) ParsingUtils.parseNumber(line.getAttrValue("hits"), Locale.ENGLISH));
            } catch (ParseException e) {
                throw new XMLStreamException(e);
            }

            String isBranch = line.getAttrValue("branch");
            String text = line.getAttrValue("condition-coverage");
            if (StringUtils.equals(isBranch, "true") && StringUtils.isNotBlank(text)) {
                String[] conditions = StringUtils.split(StringUtils.substringBetween(text, "(", ")"), "/");
                builder.conditions(lineId, Integer.parseInt(conditions[1]), Integer.parseInt(conditions[0]));
            }
        }
    }

    private void parse(File xmlFile) {
        try {
            StaxParser parser = new StaxParser(rootCursor -> {
                rootCursor.advance();
                collectPackageMeasures(rootCursor.descendantElementCursor("package"));
            });
            parser.parse(xmlFile);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    private void collectPackageMeasures(SMInputCursor pack) throws XMLStreamException {
        while (pack.getNext() != null) {
            Map<String, NewCoverage> builderByFilename = Maps.newHashMap();
            collectFileMeasures(pack.descendantElementCursor("class"), builderByFilename);
            for (Map.Entry<String, NewCoverage> entry : builderByFilename.entrySet()) {
                entry.getValue().save();
                LOGGER.info("Successfully collected measures for file {}", entry.getKey());
            }
        }
    }


    private String getAdjustedPathIfProjectIsModule(String filePath) {
        return filePath;
    }
}
