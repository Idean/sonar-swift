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
package org.sonar.plugins.swift.coverage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.plugins.swift.SwiftPlugin;
import org.sonar.plugins.swift.generic.SwiftSensor;

import java.io.File;

public class SwiftCoberturaSensor extends SwiftSensor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwiftCoberturaSensor.class);

    public static final String REPORT_PATTERN_KEY = SwiftPlugin.PROPERTY_PREFIX + ".coverage.reportPattern";
    public static final String DEFAULT_REPORT_PATTERN = "sonar-reports/coverage-swift*.xml";
    public static final String REPORTS_IN_ROOT_KEY = SwiftPlugin.PROPERTY_PREFIX + ".coverage.reportsInRoot";

    public SwiftCoberturaSensor(FileSystem fileSystem, Settings settings) {
        super(fileSystem, settings, REPORT_PATTERN_KEY, DEFAULT_REPORT_PATTERN, REPORTS_IN_ROOT_KEY, true);
    }

    public void parseReport(File report, Project project, SensorContext context) {
    	CoberturaReportParser.parseReport(report, fileSystem, context, getRootDirectory(project));
    }
}
