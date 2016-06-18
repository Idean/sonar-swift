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
import org.mockito.Mockito;
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
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Every.everyItem;
import static org.junit.Assert.assertTrue;

public class SwiftSurefireParserTest {

    private SensorContext context;
    private Settings config = Mockito.mock(Settings.class);
    private static Project project;

    /* Helpers */

    private DefaultFileSystem createFileSystem(File baseDir) {
        DefaultFileSystem fileSystem = new DefaultFileSystem();
        fileSystem.setBaseDir(baseDir);
        ProjectFileSystem projectFileSystem = Mockito.mock(ProjectFileSystem.class);
        Mockito.when(projectFileSystem.getBasedir()).thenReturn(baseDir);
        Mockito.when(project.getFileSystem()).thenReturn(projectFileSystem);
        return fileSystem;
    }

    private DefaultInputFile createTestFile(File baseDir, String path) {
        DefaultInputFile file = new DefaultInputFile(baseDir.getPath() + "/" + path);
        file.setType(InputFile.Type.TEST);
        file.setBasedir(baseDir);
        file.setAbsolutePath(baseDir.getAbsolutePath() + "/" + path);
        return file;
    }

    private static List<String> pathsFromResources(List<Resource> resources) {
        List<String> paths = new ArrayList<String>();
        for (Resource resource : resources) { paths.add(resource.getPath()); }
        return paths;
    }

    @BeforeClass public static void setupClass() {
        project = Mockito.mock(Project.class);
    }

    @Before public void setupTest() {
        context = Mockito.mock(SensorContext.class);
    }

    /**
     * This method tests collecting the correct test files from the report, even when they are in subfolders
     */
    @Test
    public void testCollectReportsInSubFolder() {

        // Setup fileSystem
        String baseDirPath = "src/test/resources/tests-project-with-subfolder/";
        File baseDir = new File(baseDirPath);
        DefaultFileSystem fileSystem = createFileSystem(baseDir);
        fileSystem.add(createTestFile(baseDir, "FixtureProjTests/Folder/FolderTests.swift"));

        // Setup parser
        SwiftSurefireParser parser = new SwiftSurefireParser(project, fileSystem, config, context);

        // Execute
        parser.collect(new File(baseDirPath + "reports/"));

        // Verify
        ArgumentCaptor<Resource> resourceArg = ArgumentCaptor.forClass(Resource.class);
        Mockito.verify(context, Mockito.atLeastOnce()).saveMeasure(resourceArg.capture(), Mockito.any(Metric.class), Mockito.anyDouble());
        List<String> paths = SwiftSurefireParserTest.pathsFromResources(resourceArg.getAllValues());
        assertThat(paths, everyItem(is(equalTo("FixtureProjTests/Folder/FolderTests.swift"))));
    }

    /**
     * This method tests retrieving and parsing a *.junit file
     */
    @Test
    public void testCollectReports() {

        // Setup fileSystem
        String baseDirPath = "src/test/resources/tests-simple-project/";
        File baseDir = new File(baseDirPath);
        DefaultFileSystem fileSystem = createFileSystem(baseDir);
        fileSystem.add(createTestFile(baseDir, "FixtureProjTests/FixtureProjTests.swift"));

        // Setup parser
        SwiftSurefireParser parser = new SwiftSurefireParser(project, fileSystem, config, context);

        // Execute
        parser.collect(new File(baseDirPath + "reports/"));

        // Verify
        ArgumentCaptor<Resource> resourceArg = ArgumentCaptor.forClass(Resource.class);
        Mockito.verify(context, Mockito.atLeastOnce()).saveMeasure(resourceArg.capture(), Mockito.any(Metric.class), Mockito.anyDouble());
        List<String> paths = SwiftSurefireParserTest.pathsFromResources(resourceArg.getAllValues());
        assertThat(paths, everyItem(is(equalTo("FixtureProjTests/FixtureProjTests.swift"))));
    }

    /**
     * This method tests collecting the correct test files from the report, even when they are in subfolders
     */
    @Test
    public void testCollectReportsInMixedFolder() {

        // Setup fileSystem
        String baseDirPath = "src/test/resources/tests-project-with-mixed-folder/";
        File baseDir = new File(baseDirPath);
        DefaultFileSystem fileSystem = createFileSystem(baseDir);
        fileSystem.add(createTestFile(baseDir, "FixtureProjTests/RootTests.swift"));
        fileSystem.add(createTestFile(baseDir, "FixtureProjTests/Folder/FolderTests.swift"));

        // Setup parser
        SwiftSurefireParser parser = new SwiftSurefireParser(project, fileSystem, config, context);

        // Execute
        parser.collect(new File(baseDirPath + "reports/"));

        // Verify
        ArgumentCaptor<Resource> resourceArg = ArgumentCaptor.forClass(Resource.class);
        Mockito.verify(context, Mockito.atLeastOnce()).saveMeasure(resourceArg.capture(), Mockito.any(Metric.class), Mockito.anyDouble());
        List<String> paths = SwiftSurefireParserTest.pathsFromResources(resourceArg.getAllValues());
        assertThat(paths, everyItem(is(anyOf(
                equalTo("FixtureProjTests/Folder/FolderTests.swift"),
                equalTo("FixtureProjTests/RootTests.swift")
        ))));
        assertTrue(paths.contains("FixtureProjTests/Folder/FolderTests.swift"));
        assertTrue(paths.contains("FixtureProjTests/RootTests.swift"));
    }
}
