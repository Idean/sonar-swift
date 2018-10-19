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

import com.backelite.sonarqube.commons.MeasureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;

import java.io.File;
import java.util.List;
import java.util.Map;

class LizardMeasurePersistor {
    private static final Logger LOGGER = LoggerFactory.getLogger(LizardMeasurePersistor.class);

    private SensorContext sensorContext;
    private FileSystem fileSystem;

    LizardMeasurePersistor(final SensorContext c, FileSystem fileSystem) {
        this.sensorContext = c;
        this.fileSystem = fileSystem;
    }

    void saveMeasures(final Map<String, List<LizardMeasure>> measures) {

        if (measures == null) {
            return;
        }

        for (Map.Entry<String, List<LizardMeasure>> entry : measures.entrySet()) {
            File file = new File(fileSystem.baseDir(), entry.getKey());
            InputFile inputFile = fileSystem.inputFile(fileSystem.predicates().hasAbsolutePath(file.getAbsolutePath()));

            if (inputFile == null) {
                LOGGER.warn("File not included in sonar {}", entry.getKey());
            } else {
                LOGGER.debug("Saving results for Resource: {}", entry.getKey());
                for (LizardMeasure measure : entry.getValue()) {
                    try {
                        MeasureUtil.saveMeasure(sensorContext, inputFile, measure.metric(), measure.value());

                    } catch (Exception e) {
                        LOGGER.error(" Exception -> {} -> {}", entry.getKey(), measure.metric().key());
                    }
                }
            }

        }
    }


}
