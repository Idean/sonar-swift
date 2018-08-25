/**
 * Objective-C Language - Enables analysis of Swift projects into SonarQube.
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
package org.sonar.plugins.objectivec.complexity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * This class is used to save the measures created by the lizardReportParser in the sonar database
 *
 * @author Andres Gil Herrera
 * @since 28/05/15.
 */
public class LizardMeasurePersistor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LizardMeasurePersistor.class);

    private final Project project;
    private final SensorContext sensorContext;
    private final FileSystem fileSystem;

    public LizardMeasurePersistor(final Project project, final SensorContext sensorContext, final FileSystem fileSystem) {
        this.project = project;
        this.sensorContext = sensorContext;
        this.fileSystem = fileSystem;
    }

    /**
     *
     * @param measures Map containing as key the name of the file and as value a list containing the measures for that file
     */
    public void saveMeasures(final Map<String, List<Measure>> measures) {

        if (measures == null) {
            return;
        }

        for (Map.Entry<String, List<Measure>> entry : measures.entrySet()) {
            File file = new File(fileSystem.baseDir(), entry.getKey());
            InputFile inputFile = fileSystem.inputFile(fileSystem.predicates().hasAbsolutePath(file.getAbsolutePath()));

            if (inputFile == null) {
                LOGGER.warn("file not included in sonar {}", entry.getKey());
                continue;
            }

            Resource resource = sensorContext.getResource(inputFile);

            if (resource != null) {
                for (Measure measure : entry.getValue()) {
                    try {
                        LOGGER.debug("Save measure {} for file {}", measure.getMetric().getName(), file);
                        sensorContext.saveMeasure(resource, measure);
                    } catch (Exception e) {
                        LOGGER.error(" Exception -> {} -> {}", entry.getKey(), measure.getMetric().getName(), e);
                    }
                }
            }
        }
    }

}