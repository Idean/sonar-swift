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


import com.backelite.sonarqube.commons.Constants;
import com.backelite.sonarqube.objectivec.lang.core.ObjectiveC;
import com.backelite.sonarqube.swift.lang.core.Swift;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.scan.filesystem.PathResolver;

import java.io.File;
import java.util.List;

public class CoberturaSensor implements Sensor {

    public static final String REPORT_PATTERN_KEY = Constants.PROPERTY_PREFIX + ".coverage.reportPattern";
    public static final String DEFAULT_REPORT_PATTERN = "sonar-reports/coverage*.xml";
    public static final String REPORT_DIRECTORY_KEY = Constants.PROPERTY_PREFIX + ".coverage.reportDirectory";
    private static final Logger LOGGER = LoggerFactory.getLogger(CoberturaSensor.class);
    private final ReportFilesFinder reportFilesFinder;

    private final Settings settings;
    private final FileSystem fileSystem;
    private final PathResolver pathResolver;
    private Project project;

    public CoberturaSensor(final FileSystem fileSystem, final PathResolver pathResolver, final Settings settings) {

        this.settings = settings;
        this.fileSystem = fileSystem;
        this.pathResolver = pathResolver;

        reportFilesFinder = new ReportFilesFinder(settings, REPORT_PATTERN_KEY, DEFAULT_REPORT_PATTERN, REPORT_DIRECTORY_KEY);
    }

    @Override
    public boolean shouldExecuteOnProject(final Project project) {

        this.project = project;

        return fileSystem.languages().contains(Swift.KEY) || fileSystem.languages().contains(ObjectiveC.KEY);
    }

    @Override
    public void analyse(final Project project, final SensorContext context) {

        final String projectBaseDir = fileSystem.baseDir().getPath();
        LOGGER.info("Analyzing directory: {}", projectBaseDir);

        List<File> reports;
        if (project.isRoot()) {
            reports = reportFilesFinder.reportsIn(projectBaseDir);
        } else {
            final String module = project.getName();
            final String rootDir = getRootDirectory(project);
            reports = reportFilesFinder.reportsIn(module, rootDir, projectBaseDir);
        }

        for (final File report : reports) {
            LOGGER.info("Processing coverage report {}", report);
            CoberturaReportParser.parseReport(report, fileSystem, project, context);
        }
    }

    private String getRootDirectory(Project project) {
        final String projectBaseDir = fileSystem.baseDir().getPath();
        if (project.isRoot()) {
            return projectBaseDir;
        } else {
            final String modulePath = project.path();
            return projectBaseDir.substring(0, projectBaseDir.length() - modulePath.length());
        }
    }

}
