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
package org.sonar.plugins.swift.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.plugins.swift.lang.core.Swift;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public abstract class SwiftSensor implements Sensor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwiftSensor.class);
    public final FileSystem fileSystem;

    private final Settings settings;
    private final String reportPatternKey;
    private final String reportPatternDefault;
    private final String reportsInRootKey;
    private final boolean multipleReports;

    public SwiftSensor(FileSystem fileSystem, Settings settings, String reportPatternKey, 
        String reportPatternDefault, String reportsInRootKey, boolean multipleReports) {
        this.fileSystem = fileSystem;
        this.settings = settings;
        this.reportPatternKey = reportPatternKey;
        this.reportPatternDefault = reportPatternDefault;
        this.reportsInRootKey = reportsInRootKey;
        this.multipleReports = multipleReports;
    }

    public boolean shouldExecuteOnProject(Project project) {
        return fileSystem.languages().contains(Swift.KEY);
    }

    public void analyse(Project project, SensorContext context) {
        String reportDirectory = getReportDirectory(project);
        String reportPattern = getReportPattern(project);
        List<File> reports;
        if (multipleReports) {
            reports = Util.findReports(reportDirectory, reportPattern);
        }
        else {
            StringBuilder filePathBuilder = new StringBuilder(reportDirectory).append("/").append(reportPattern);
            reports = Arrays.asList(new File(filePathBuilder.toString()));
        }
        for (final File report : reports) {
            LOGGER.info("Processing report {}", report);
            parseReport(report, project, context);
        }
    }

    public abstract void parseReport(File report, Project project, SensorContext context);

    public String getRootDirectory(Project project) {
        final String baseDir = fileSystem.baseDir().getPath();
        if (project.isRoot()) {
            return baseDir;
        }
        return baseDir.substring(0, baseDir.length() - project.path().length());
    }

    private String getReportDirectory(Project project) {
        if (getSettingsValue(project, reportsInRootKey, "false").equals("true")) {
            return getRootDirectory(project);
        }
        return fileSystem.baseDir().getPath();
    }

    private String getReportPattern(Project project) {
        return getSettingsValue(project, reportPatternKey, reportPatternDefault);
    }

    private String getSettingsValue(Project project, String key, String defaultValue) {
        String value;
        // modular case
        if (!project.isRoot()) {
            value = settings.getString(project.getName() + "." + key);
            if (value != null) {
                return value;
            }
        }
        // root case
        value = settings.getString(key);
        return value != null ? value : defaultValue;
    }
}
