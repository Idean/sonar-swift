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
package com.backelite.sonarqube.swift.complexity;

import com.backelite.sonarqube.commons.Constants;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.Measure;

import java.io.File;
import java.util.List;
import java.util.Map;

public class LizardSensor implements Sensor {

    public static final String REPORT_PATH_KEY = Constants.PROPERTY_PREFIX + ".lizard.report";
    public static final String DEFAULT_REPORT_PATH = "sonar-reports/lizard-report.xml";

    private final Settings conf;
    private final FileSystem fileSystem;

    public LizardSensor(final FileSystem moduleFileSystem, final Settings config) {
        this.conf = config;
        this.fileSystem = moduleFileSystem;
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

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor
                .name("Lizard")
                .onlyOnFileType(InputFile.Type.MAIN);
    }

    @Override
    public void execute(SensorContext context) {

        final String projectBaseDir = fileSystem.baseDir().getPath();
        Map<String, List<Measure>> measures = parseReportsIn(projectBaseDir, new LizardReportParser());
        LoggerFactory.getLogger(getClass()).info("Saving results of complexity analysis");
        new LizardMeasurePersistor(context, fileSystem).saveMeasures(measures);
    }
}
