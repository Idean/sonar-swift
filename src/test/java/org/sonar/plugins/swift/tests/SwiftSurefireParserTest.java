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

package org.sonar.plugins.swift.tests;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.swift.lang.core.Swift;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class SwiftSurefireParserTest {

    private SensorContext context;
    private SwiftSurefireParser parser;
    private static Project project;
    private static FileSystem fileSystem;

    @BeforeClass public static void setupClass() {
        File baseDir = new File("src/test/resources/");
        fileSystem = mock(FileSystem.class);
        when(fileSystem.baseDir()).thenReturn(baseDir);
        project = mock(Project.class);
        ProjectFileSystem projectFileSystem = mock(ProjectFileSystem.class);
        when(projectFileSystem.getBasedir()).thenReturn(baseDir);
        when(project.getFileSystem()).thenReturn(projectFileSystem);
    }

    @Before public void setupTest() {
        context = mock(SensorContext.class);
        Settings config = mock(Settings.class);
        parser = new SwiftSurefireParser(project, fileSystem, config, context);
    }

    /**
     * This method tests retrieving and parsing a *.junit file
     */
    @Test
    public void testGetReportsFromJUnitFiles() {

        // Setup
        File reportsFolder = new File("src/test/resources/reports/");
        ArgumentCaptor<Resource> resourceArg = ArgumentCaptor.forClass(Resource.class);
        ArgumentCaptor<Metric> metricArg = ArgumentCaptor.forClass(Metric.class);
        ArgumentCaptor<Double> valueArg = ArgumentCaptor.forClass(Double.class);

        // Execute
        parser.collect(reportsFolder);

        // Verify
        verify(context, times(6)).saveMeasure(resourceArg.capture(), metricArg.capture(), valueArg.capture());
        assertEquals(resourceArg.getValue().getPath(), "FixtureProjTests/FixtureProjTests.swift");
    }
}
