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

import org.apache.commons.lang.StringUtils;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.measures.CoverageMeasuresBuilder;
import org.sonar.api.utils.StaxParser;

import javax.xml.stream.XMLStreamException;
import java.util.Map;

class CoberturaXMLStreamHandler implements StaxParser.XmlStreamHandler {

    private final Map<String, CoverageMeasuresBuilder> measuresForReport;

    public CoberturaXMLStreamHandler(final Map<String, CoverageMeasuresBuilder> data) {

        measuresForReport = data;
    }

    public void stream(final SMHierarchicCursor rootCursor) throws XMLStreamException {

        rootCursor.advance();
        collectPackageMeasures(rootCursor.descendantElementCursor("package"));
    }

    private void collectPackageMeasures(final SMInputCursor pack) throws XMLStreamException {

        while (pack.getNext() != null) {
            collectFileMeasures(pack.descendantElementCursor("class"));
        }
    }

    private void collectFileMeasures(final SMInputCursor clazz) throws XMLStreamException {

        while (clazz.getNext() != null) {
            collectFileData(clazz);
        }
    }

    private void collectFileData(final SMInputCursor clazz) throws XMLStreamException {

        final CoverageMeasuresBuilder builder = builderFor(clazz);
        final SMInputCursor line = clazz.childElementCursor("lines").advance().childElementCursor("line");

        while (null != line.getNext()) {
            recordCoverageFor(line, builder);
        }
    }

    private void recordCoverageFor(final SMInputCursor line, final CoverageMeasuresBuilder builder) throws XMLStreamException {

        final int lineId = Integer.parseInt(line.getAttrValue("number"));
        final int noHits = (int) Math.min(Double.parseDouble(line.getAttrValue("hits")), Integer.MAX_VALUE);
        final String isBranch = line.getAttrValue("branch");
        final String conditionText = line.getAttrValue("condition-coverage");

        builder.setHits(lineId, noHits);

        if (StringUtils.equals(isBranch, "true") && StringUtils.isNotBlank(conditionText)) {

            final String[] conditions = StringUtils.split(StringUtils.substringBetween(conditionText, "(", ")"), "/");
            builder.setConditions(lineId, Integer.parseInt(conditions[1]), Integer.parseInt(conditions[0]));
        }
    }

    private CoverageMeasuresBuilder builderFor(final SMInputCursor clazz) throws XMLStreamException {

        final String fileName = clazz.getAttrValue("filename");
        CoverageMeasuresBuilder builder = measuresForReport.get(fileName);

        if (builder == null) {
            builder = CoverageMeasuresBuilder.create();
            measuresForReport.put(fileName, builder);
        }

        return builder;
    }
}