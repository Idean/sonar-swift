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
import com.backelite.sonarqube.objectivec.lang.core.ObjectiveC;
import com.backelite.sonarqube.swift.lang.core.Swift;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;

import java.io.File;

public class LizardSensor implements Sensor {
    private static final Logger LOGGER = LoggerFactory.getLogger(LizardSensor.class);
    public static final String REPORT_PATH_KEY = Constants.PROPERTY_PREFIX + ".lizard.report";
    public static final String DEFAULT_REPORT_PATH = "sonar-reports/lizard-report.xml";

    private final SensorContext context;

    public LizardSensor(final SensorContext context) {
        this.context = context;
    }

    private String reportPath() {
        return context.config()
            .get(REPORT_PATH_KEY)
            .orElse(DEFAULT_REPORT_PATH);
    }

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor
            .name("Lizard")
            .onlyOnLanguages(Swift.KEY, ObjectiveC.KEY)
            .onlyOnFileType(InputFile.Type.MAIN);
    }

    @Override
    public void execute(SensorContext context) {
        String reportFileName = context.fileSystem().baseDir().getPath() + "/"+ reportPath();
        LOGGER.info("Processing complexity report: {}",reportFileName);

        LizardReportParser parser = new LizardReportParser(context);
        parser.parseReport(new File(reportFileName));
    }
}
