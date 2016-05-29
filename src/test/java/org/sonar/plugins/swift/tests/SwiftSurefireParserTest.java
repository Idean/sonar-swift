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
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.resources.Resource;
import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class SwiftSurefireParserTest {

    private SensorContext context;
    private Settings config = mock(Settings.class);
    private static Project project;

    private DefaultFileSystem createFileSystem(File baseDir) {
        DefaultFileSystem fileSystem = new DefaultFileSystem();
        fileSystem.setBaseDir(baseDir);
        ProjectFileSystem projectFileSystem = mock(ProjectFileSystem.class);
        when(projectFileSystem.getBasedir()).thenReturn(baseDir);
        when(project.getFileSystem()).thenReturn(projectFileSystem);
        return fileSystem;
    }

    private DefaultInputFile createTestFile(File baseDir, String path) {
        DefaultInputFile file = new DefaultInputFile(baseDir.getPath() + "/" + path);
        file.setType(InputFile.Type.TEST);
        file.setBasedir(baseDir);
        file.setAbsolutePath(baseDir.getAbsolutePath() + "/" + path);
        return file;
    }

    @BeforeClass public static void setupClass() {
        project = mock(Project.class);
    }

    @Before public void setupTest() {
        context = mock(SensorContext.class);
    }

    /**
     * This method tests collecting the correct test files from the report, even when they are in subfolders
     */
    @Test
    public void testRetrieveTestInSubFolder() {

        // Setup fileSystem
        String baseDirPath = "src/test/resources/tests-project-with-subfolder/";
        File baseDir = new File(baseDirPath);
        DefaultFileSystem fileSystem = createFileSystem(baseDir);
        fileSystem.add(createTestFile(baseDir, "FixtureProjTests/FixtureProjTests.swift"));
        fileSystem.add(createTestFile(baseDir, "FixtureProjTests/Folder/FolderTests.swift"));

        // Setup parser
        SwiftSurefireParser parser = new SwiftSurefireParser(project, fileSystem, config, context);
        ArgumentCaptor<Resource> resourceArg = ArgumentCaptor.forClass(Resource.class);
        ArgumentCaptor<Metric> metricArg = ArgumentCaptor.forClass(Metric.class);
        ArgumentCaptor<Double> valueArg = ArgumentCaptor.forClass(Double.class);

        // Execute
        parser.collect(new File(baseDirPath + "reports/"));

        // Verify
        verify(context, times(6)).saveMeasure(resourceArg.capture(), metricArg.capture(), valueArg.capture());
        assertEquals(resourceArg.getValue().getPath(), "FixtureProjTests/Folder/FolderTests.swift");
    }

    /**
     * This method tests retrieving and parsing a *.junit file
     */
    @Test
    public void testGetReportsFromJUnitFiles() {

        // Setup fileSystem
        String baseDirPath = "src/test/resources/tests-simple-project/";
        File baseDir = new File(baseDirPath);
        DefaultFileSystem fileSystem = createFileSystem(baseDir);
        fileSystem.add(createTestFile(baseDir, "FixtureProjTests/FixtureProjTests.swift"));

        // Setup parser
        SwiftSurefireParser parser = new SwiftSurefireParser(project, fileSystem, config, context);
        ArgumentCaptor<Resource> resourceArg = ArgumentCaptor.forClass(Resource.class);
        ArgumentCaptor<Metric> metricArg = ArgumentCaptor.forClass(Metric.class);
        ArgumentCaptor<Double> valueArg = ArgumentCaptor.forClass(Double.class);

        // Execute
        parser.collect(new File(baseDirPath + "reports/"));

        // Verify
        verify(context, times(6)).saveMeasure(resourceArg.capture(), metricArg.capture(), valueArg.capture());
        assertEquals(resourceArg.getValue().getPath(), "FixtureProjTests/FixtureProjTests.swift");
    }
}
