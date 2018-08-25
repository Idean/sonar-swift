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
package com.backelite.sonarqube.objectivec.coverage;


import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.measures.CoverageMeasuresBuilder;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.ParsingUtils;
import org.sonar.api.utils.StaxParser;
import org.sonar.api.utils.XmlParserException;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.text.ParseException;
import java.util.Locale;
import java.util.Map;

final class CoberturaReportParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoberturaReportParser.class);

    private final FileSystem fileSystem;
    private final Project project;
    private final SensorContext context;

    private CoberturaReportParser(FileSystem fileSystem, Project project, SensorContext context) {
        this.fileSystem = fileSystem;
        this.project = project;
        this.context = context;
    }

    /**
     * Parse a Cobertura xml report and create measures accordingly
     */
    public static void parseReport(File xmlFile, FileSystem fileSystem, Project project, SensorContext context) {
        new CoberturaReportParser(fileSystem, project, context).parse(xmlFile);
    }

    private void parse(File xmlFile) {
        try {
            StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {

                @Override
                public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
                    rootCursor.advance();
                    collectPackageMeasures(rootCursor.descendantElementCursor("package"));
                }
            });
            parser.parse(xmlFile);
        } catch (XMLStreamException e) {
            throw new XmlParserException(e);
        }
    }

    private void collectPackageMeasures(SMInputCursor pack) throws XMLStreamException {
        while (pack.getNext() != null) {
            Map<String, CoverageMeasuresBuilder> builderByFilename = Maps.newHashMap();
            collectFileMeasures(pack.descendantElementCursor("class"), builderByFilename);
            for (Map.Entry<String, CoverageMeasuresBuilder> entry : builderByFilename.entrySet()) {
                String filePath = entry.getKey();
                File file = new File(fileSystem.baseDir(), filePath);
                InputFile inputFile = fileSystem.inputFile(fileSystem.predicates().hasAbsolutePath(file.getAbsolutePath()));

                if (inputFile == null) {
                    LOGGER.warn("file not included in sonar {}", filePath);
                    continue;
                }

                Resource resource = context.getResource(inputFile);
                if (resourceExists(resource)) {
                    for (Measure measure : entry.getValue().createMeasures()) {
                        context.saveMeasure(resource, measure);
                    }
                }
            }
        }
    }

    private boolean resourceExists(Resource file) {
        return context.getResource(file) != null;
    }

    private static void collectFileMeasures(SMInputCursor clazz,
                                            Map<String, CoverageMeasuresBuilder> builderByFilename) throws XMLStreamException {
        while (clazz.getNext() != null) {
            String fileName = clazz.getAttrValue("filename");
            CoverageMeasuresBuilder builder = builderByFilename.get(fileName);
            if (builder == null) {
                builder = CoverageMeasuresBuilder.create();
                builderByFilename.put(fileName, builder);
            }
            collectFileData(clazz, builder);
        }
    }

    private static void collectFileData(SMInputCursor clazz,
                                        CoverageMeasuresBuilder builder) throws XMLStreamException {
        SMInputCursor line = clazz.childElementCursor("lines").advance().childElementCursor("line");
        while (line.getNext() != null) {
            int lineId = Integer.parseInt(line.getAttrValue("number"));
            try {
                builder.setHits(lineId, (int) ParsingUtils.parseNumber(line.getAttrValue("hits"), Locale.ENGLISH));
            } catch (ParseException e) {
                throw new XmlParserException(e);
            }

            String isBranch = line.getAttrValue("branch");
            String text = line.getAttrValue("condition-coverage");
            if (StringUtils.equals(isBranch, "true") && StringUtils.isNotBlank(text)) {
                String[] conditions = StringUtils.split(StringUtils.substringBetween(text, "(", ")"), "/");
                builder.setConditions(lineId, Integer.parseInt(conditions[1]), Integer.parseInt(conditions[0]));
            }
        }
    }
}
