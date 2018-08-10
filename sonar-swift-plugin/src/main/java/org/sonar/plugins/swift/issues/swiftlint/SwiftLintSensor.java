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
package org.sonar.plugins.swift.issues.swiftlint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.plugins.swift.SwiftPlugin;
import org.sonar.plugins.swift.generic.SwiftSensor;

import java.io.File;

public class SwiftLintSensor extends SwiftSensor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwiftLintSensor.class);

    public static final String REPORT_PATH_KEY = SwiftPlugin.PROPERTY_PREFIX + ".swiftlint.report";
    public static final String DEFAULT_REPORT_PATH = "sonar-reports/*swiftlint.txt";
    public static final String REPORTS_IN_ROOT_KEY = SwiftPlugin.PROPERTY_PREFIX + ".swiftlint.reportsInRoot";

    private final ResourcePerspectives resourcePerspectives;

    public SwiftLintSensor(FileSystem fileSystem, Settings settings, ResourcePerspectives resourcePerspectives) {
        super(fileSystem, settings, REPORT_PATH_KEY, DEFAULT_REPORT_PATH, REPORTS_IN_ROOT_KEY, false);
        this.resourcePerspectives = resourcePerspectives;
    }

    public void parseReport(File report, Project project, SensorContext context) {
        SwiftLintReportParser.parseReport(report, fileSystem, resourcePerspectives);
    }
}
