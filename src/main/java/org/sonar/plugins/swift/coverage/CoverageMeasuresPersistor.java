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
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoverageMeasuresBuilder;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;

import java.io.File;
import java.util.Map;

final class CoverageMeasuresPersistor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoverageMeasuresPersistor.class);

    private final Project project;
    private final SensorContext context;

    public CoverageMeasuresPersistor(final Project p, final SensorContext c) {

        project = p;
        context = c;
    }

    public void saveMeasures(final Map<String, CoverageMeasuresBuilder> coverageMeasures) {

        for (final Map.Entry<String, CoverageMeasuresBuilder> entry : coverageMeasures.entrySet()) {
            saveMeasuresForFile(entry.getValue(), entry.getKey());
        }
    }

    private void saveMeasuresForFile(final CoverageMeasuresBuilder measureBuilder, final String filePath) {

        LoggerFactory.getLogger(getClass()).debug("Saving measures for {}", filePath);
        final org.sonar.api.resources.File swiftFile = org.sonar.api.resources.File.fromIOFile(new File(project.getFileSystem().getBasedir(), filePath), project);

        if (fileExists(context, swiftFile)) {
            LOGGER.debug("File {} was found in the project.", filePath);
            saveMeasures(measureBuilder, swiftFile);
        }
    }

    private void saveMeasures(final CoverageMeasuresBuilder measureBuilder, final org.sonar.api.resources.File swiftFile) {

        for (final Measure measure : measureBuilder.createMeasures()) {
            LOGGER.debug("Measure {}", measure.getMetric().getName());
            context.saveMeasure(swiftFile, measure);
        }
    }

    private boolean fileExists(final SensorContext context, final org.sonar.api.resources.File file) {

        return context.getResource(file) != null;
    }
}
