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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.coverage.NewCoverage;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.text.ParseException;

import static java.util.Locale.ENGLISH;
import static org.sonar.api.utils.ParsingUtils.parseNumber;

public final class CoberturaReportParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoberturaReportParser.class);
    private static final String PACKAGE = "package";
    private static final String CLASS = "class";
    private static final String FILE = "filename";
    private static final String LINES = "lines";
    private static final String LINE = "line";
    private static final String NUMBER = "number";
    private static final String HITS = "hits";
    private static final String BRANCH = "branch";
    private static final String COVERAGE = "condition-coverage";
    private final SensorContext context;

    public CoberturaReportParser(SensorContext context) {
        this.context = context;
    }

    public void parseReport(final File xmlFile) {
        final XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
        try {
            SMInputFactory inputFactory = new SMInputFactory(xmlFactory);
            SMHierarchicCursor rootCursor = inputFactory.rootElementCursor(xmlFile);
            while (rootCursor.getNext() != null) {
                collectPackageMeasures(rootCursor.descendantElementCursor(PACKAGE));
            }
            rootCursor.getStreamReader().closeCompletely();
        } catch (XMLStreamException e) {
            throw new IllegalStateException("XML is not valid", e);
        }
    }

    private void collectPackageMeasures(SMInputCursor pack) throws XMLStreamException {
        while (pack.getNext() != null) {
            collectClassMeasures(pack.descendantElementCursor(CLASS));
        }
    }

    private void collectClassMeasures(SMInputCursor classCursor) throws XMLStreamException {
        while (classCursor.getNext() != null) {
            String fileName = classCursor.getAttrValue(FILE);
            collectFileData(classCursor, fileName);
        }
    }

    private void collectFileData(SMInputCursor fileCursor, String filename) throws XMLStreamException {
        filename = sanitizeFilename(filename);
        InputFile resource = getFile(filename);

        NewCoverage coverage = null;
        boolean lineAdded = false;
        if (resource != null) {
            coverage = context.newCoverage();
            coverage.onFile(resource);
        }

        SMInputCursor line = fileCursor.childElementCursor(LINES).advance().childElementCursor(LINE);
        while (line.getNext() != null) {
            int lineId = Integer.parseInt(line.getAttrValue(NUMBER));
            try {
                if (coverage != null) {
                    coverage.lineHits(lineId, (int) parseNumber(line.getAttrValue(HITS), ENGLISH));
                    lineAdded = true;
                }
            } catch (ParseException e) {
                throw new XMLStreamException(e);
            }

            String isBranch = line.getAttrValue(BRANCH);
            String text = line.getAttrValue(COVERAGE);
            if ("true".equalsIgnoreCase(isBranch) && StringUtils.isNotBlank(text)) {
                String[] conditions = StringUtils.split(StringUtils.substringBetween(text, "(", ")"), "/");
                if (coverage != null) {
                    coverage.conditions(lineId, Integer.parseInt(conditions[1]), Integer.parseInt(conditions[0]));
                    lineAdded = true;
                }
            }
        }
        if (coverage != null) {
            // If there was no lines covered or uncovered (e.g. everything is ignored), but the file exists then Sonar would report the file as uncovered
            // so adding a fake one to line number 1
            if (!lineAdded) {
                coverage.lineHits(1, 1);
            }
            coverage.save();
        }
    }

    private InputFile getFile(String name) {
        FilePredicate fpName = context.fileSystem().predicates().hasFilename(name);
        FilePredicate fpPath = context.fileSystem().predicates().hasAbsolutePath(context.fileSystem().baseDir().getAbsolutePath());
        InputFile file = context.fileSystem().inputFile(context.fileSystem().predicates().and(fpPath, fpName));
        return file != null && file.isFile() ? file : null;
    }

    private static String sanitizeFilename(String s) {
        String fileName = FilenameUtils.removeExtension(s);
        fileName = fileName.replace('/', '.').replace('\\', '.');
        return fileName;
    }

}

