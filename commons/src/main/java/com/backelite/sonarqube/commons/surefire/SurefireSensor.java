/**
 * commons - Enables analysis of Swift and Objective-C projects into SonarQube.
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
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.scan.filesystem.PathResolver;

import java.io.File;

/**
 * Created by gillesgrousset on 28/08/2018.
 */
public class SurefireSensor implements Sensor {

    public static final String REPORTS_PATH_KEY = Constants.PROPERTY_PREFIX + ".surefire.junit.reportsPath";
    public static final String DEFAULT_REPORTS_PATH = "sonar-reports/";

    private static final Logger LOGGER = LoggerFactory.getLogger(SurefireSensor.class);
    private final FileSystem fileSystem;
    private final PathResolver pathResolver;
    private final ResourcePerspectives resourcePerspectives;
    private final Settings settings;

    public SurefireSensor(FileSystem fileSystem, PathResolver pathResolver, ResourcePerspectives resourcePerspectives, Settings settings) {
        this.fileSystem = fileSystem;
        this.pathResolver = pathResolver;
        this.resourcePerspectives = resourcePerspectives;
        this.settings = settings;
    }

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor
                .name("Surefire")
                .onlyOnFileType(InputFile.Type.MAIN);
    }

    @Override
    public void execute(SensorContext context) {

        String path = this.reportPath();
        File reportsDir = pathResolver.relativeFile(fileSystem.baseDir(), path);

        LOGGER.info("Processing test reports in {}", reportsDir);

        if (!reportsDir.isDirectory()) {
            LOGGER.warn("JUnit report directory not found at {}", reportsDir);
            return;
        }

        collect(context, reportsDir);
    }

    protected  void collect(SensorContext context, File reportsDir) {
        LOGGER.info("parsing {}", reportsDir);
        new SurefireParser(fileSystem, resourcePerspectives, context).collect(reportsDir);
    }

    protected String reportPath() {
        String reportPath = settings.getString(REPORTS_PATH_KEY);
        if (reportPath == null) {
            reportPath = DEFAULT_REPORTS_PATH;
        }
        return reportPath;
    }
}
