/*
 * SonarQube Swift Plugin
 * Copyright (C) 2015 Backelite
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.swift.coverage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.measures.CoverageMeasuresBuilder;
import org.sonar.api.utils.StaxParser;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

final class CoberturaParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoberturaParser.class);

    public Map<String, CoverageMeasuresBuilder> parseReport(final File xmlFile) {

        Map<String, CoverageMeasuresBuilder> result = null;

        try {
            final InputStream reportStream = new FileInputStream(xmlFile);
            result = parseReport(reportStream);
            reportStream.close();
        } catch (final IOException e) {
            LOGGER.error("Error processing file named {}", xmlFile, e);
            result = new HashMap<String, CoverageMeasuresBuilder>();
        }

        return result;
    }

    public Map<String, CoverageMeasuresBuilder> parseReport(final InputStream xmlFile) {

        final Map<String, CoverageMeasuresBuilder> measuresForReport = new HashMap<String, CoverageMeasuresBuilder>();

        try {

            final StaxParser parser = new StaxParser(new CoberturaXMLStreamHandler(measuresForReport));
            parser.parse(xmlFile);
        } catch (final XMLStreamException e) {

            LOGGER.error("Error while parsing XML stream.", e);
        }
        return measuresForReport;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
