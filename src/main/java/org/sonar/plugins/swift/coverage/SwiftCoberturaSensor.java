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
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.scan.filesystem.PathResolver;
import org.sonar.plugins.swift.SwiftPlugin;
import org.sonar.plugins.swift.lang.core.Swift;

import java.io.File;


public final class SwiftCoberturaSensor implements Sensor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwiftCoberturaSensor.class);

    public static final String REPORT_PATTERN_KEY = SwiftPlugin.PROPERTY_PREFIX + ".coverage.reportPattern";
    public static final String DEFAULT_REPORT_PATTERN = "sonar-reports/coverage-swift*.xml";

    private final ReportFilesFinder reportFilesFinder;

    private final Settings settings;
    private final FileSystem fileSystem;
    private final PathResolver pathResolver;
    private Project project;

    public SwiftCoberturaSensor(final FileSystem fileSystem, final PathResolver pathResolver, final Settings settings) {

        this.settings = settings;
        this.fileSystem = fileSystem;
        this.pathResolver = pathResolver;

        reportFilesFinder = new ReportFilesFinder(settings, REPORT_PATTERN_KEY, DEFAULT_REPORT_PATTERN);
    }

    public boolean shouldExecuteOnProject(final Project project) {

        this.project = project;

        return project.isRoot() && fileSystem.languages().contains(Swift.KEY);
    }

    public void analyse(final Project project, final SensorContext context) {


        final String projectBaseDir = fileSystem.baseDir().getPath();

        for (final File report : reportFilesFinder.reportsIn(projectBaseDir)) {
            LOGGER.info("Processing coverage report {}", report);
            CoberturaReportParser.parseReport(report, fileSystem, project, context);
        }
    }
}
