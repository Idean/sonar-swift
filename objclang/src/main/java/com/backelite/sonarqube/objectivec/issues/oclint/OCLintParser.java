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

import com.backelite.sonarqube.commons.surefire.StaxParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.sensor.SensorContext;

import javax.xml.stream.XMLStreamException;
import java.io.File;

final class OCLintParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(OCLintParser.class);
    private final SensorContext context;

    public OCLintParser(SensorContext context) {
        this.context = context;
    }

    public void parseReport(final File file) {
        try {
            StaxParser parser = new StaxParser(new OCLintXMLStreamHandler(context));
            parser.parse(file);
        } catch (final XMLStreamException e) {
            LOGGER.error( "Error while parsing XML stream.", e);
        }

    }
}
