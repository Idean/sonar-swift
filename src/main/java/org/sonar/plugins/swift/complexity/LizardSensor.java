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

package org.sonar.plugins.swift.complexity;

import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.plugins.swift.SwiftPlugin;
import org.sonar.plugins.swift.lang.core.Swift;

import java.io.File;
import java.util.List;
import java.util.Map;

public class LizardSensor implements Sensor {

    public static final String REPORT_PATH_KEY = SwiftPlugin.PROPERTY_PREFIX
            + ".lizard.report";
    public static final String DEFAULT_REPORT_PATH = "sonar-reports/lizard-report.xml";

    private final Settings conf;
    private final FileSystem fileSystem;

    public LizardSensor(final FileSystem moduleFileSystem, final Settings config) {
        this.conf = config;
        this.fileSystem = moduleFileSystem;
    }

    @Override
    public boolean shouldExecuteOnProject(Project project) {
        return project.isRoot() && fileSystem.languages().contains(Swift.KEY);
    }

    @Override
    public void analyse(Project project, SensorContext sensorContext) {

        final String projectBaseDir = fileSystem.baseDir().getPath();
        Map<String, List<Measure>> measures = parseReportsIn(projectBaseDir, new LizardReportParser());
        LoggerFactory.getLogger(getClass()).info("Saving results of complexity analysis");
        new LizardMeasurePersistor(project, sensorContext, fileSystem).saveMeasures(measures);
    }

    private Map<String, List<Measure>> parseReportsIn(final String baseDir, LizardReportParser parser) {
        final StringBuilder reportFileName = new StringBuilder(baseDir);
        reportFileName.append("/").append(reportPath());
        LoggerFactory.getLogger(getClass()).info("Processing complexity report ");
        return parser.parseReport(new File(reportFileName.toString()));
    }

    private String reportPath() {
        String reportPath = conf.getString(REPORT_PATH_KEY);
        if (reportPath == null) {
            reportPath = DEFAULT_REPORT_PATH;
        }
        return reportPath;
    }
}
