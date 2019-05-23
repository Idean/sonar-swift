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
package com.backelite.sonarqube.commons.surefire;

import com.backelite.sonarqube.commons.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;

import java.io.File;

public class SurefireSensor implements Sensor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SurefireSensor.class);
    public static final String REPORT_PATH_KEY = Constants.PROPERTY_PREFIX + ".surefire.junit.reportsPath";
    public static final String DEFAULT_REPORT_PATH = "sonar-reports/";

    private final SensorContext context;

    public SurefireSensor(SensorContext context) {
        this.context = context;
    }

    protected String reportPath() {
        return context.config()
            .get(REPORT_PATH_KEY)
            .orElse(DEFAULT_REPORT_PATH);
    }

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor
            .name("Surefire")
            .onlyOnLanguages("swift","objc")
            .onlyOnFileType(InputFile.Type.MAIN);
    }

    @Override
    public void execute(SensorContext context) {
        SurefireReportParser surefireParser = new SurefireReportParser(context);
        String reportFileName = context.fileSystem().baseDir().getAbsolutePath()  + File.separator +  reportPath();
        File reportsDir = new File(reportFileName);

        if (!reportsDir.isDirectory()) {
            LOGGER.warn("JUnit report directory not found at {}", reportsDir);
            return;
        }
        for (File file : reportsDir.listFiles((file,name) -> (name.startsWith("TEST") && name.endsWith(".xml")) || name.endsWith(".junit"))){
            LOGGER.info("Processing Surefire report {}", file.getName());
            surefireParser.parseReport(file);
        }
        surefireParser.save();
    }
}
