/**
 * backelite-sonar-swift-plugin - Enables analysis of Swift projects into SonarQube.
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
package org.sonar.plugins.swift.generic;

import java.io.*;
import java.util.*;

import org.junit.Before;
import org.junit.Test;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.plugins.swift.coverage.SwiftCoberturaSensor;
import org.sonar.plugins.swift.generic.SwiftSensor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SwiftSensorTest {

	private static final String TEST_REPORT_PATTERN = "**/cobertura.xml";
	private static final String TEST_MATERIALS_DIR = "./src/test/resources/generic";
	private static final String TEST_MOD_NAME = "myMod";
	private static final File ROOT_REPORT = new File(TEST_MATERIALS_DIR + "/dir1/cobertura.xml");
	private static final File MODULE_REPORT = new File(TEST_MATERIALS_DIR + "/" + TEST_MOD_NAME + "/dir2/cobertura.xml");

	private FileSystem fileSystem;
	private Settings settings;
	private Project project;
	private SensorContext context;
	private File baseDir;
	private SwiftSensor sensorSpy;
	private List<File> parsedReports;

	@Before
	public void setUp() {
		fileSystem = mock(FileSystem.class);
		settings = mock(Settings.class);
		project = mock(Project.class);
		context = mock(SensorContext.class);
		baseDir = mock(File.class);
		sensorSpy = spy(new SwiftCoberturaSensor(fileSystem, settings));
		parsedReports = new LinkedList();

		when(fileSystem.baseDir()).thenReturn(baseDir);
		when(settings.getString(SwiftCoberturaSensor.REPORT_PATTERN_KEY)).thenReturn(TEST_REPORT_PATTERN);
		when(settings.getString(TEST_MOD_NAME + "." + SwiftCoberturaSensor.REPORT_PATTERN_KEY)).thenReturn(TEST_REPORT_PATTERN);
		doAnswer(invocation -> parsedReports.add((File) invocation.getArguments()[0]))
		.when(sensorSpy).parseReport(any(File.class), any(Project.class), any(SensorContext.class));
	}

	private void setupRootProject() {
		when(project.isRoot()).thenReturn(true);
		when(baseDir.getPath()).thenReturn(TEST_MATERIALS_DIR);
	}

	private void setupModularProject() {
		when(project.isRoot()).thenReturn(false);
		when(project.getName()).thenReturn(TEST_MOD_NAME);
		when(project.path()).thenReturn(TEST_MOD_NAME);
		when(baseDir.getPath()).thenReturn(TEST_MATERIALS_DIR + "/" + TEST_MOD_NAME);
	}

	@Test
	public void findReportsInRootWithReportsInRootUnset() {
		setupRootProject();
		sensorSpy.analyse(project, context);
		assertSameFiles(Arrays.asList(ROOT_REPORT, MODULE_REPORT), parsedReports);
	}

	@Test
	public void findReportsInRootWithReportsInRootFalse() {
		setupRootProject();
		when(settings.getString(SwiftCoberturaSensor.REPORTS_IN_ROOT_KEY)).thenReturn("false");
		sensorSpy.analyse(project, context);
		assertSameFiles(Arrays.asList(ROOT_REPORT, MODULE_REPORT), parsedReports);
	}

	@Test
	public void findReportsInRootWithReportsInRootTrue() {
		setupRootProject();
		when(settings.getString(SwiftCoberturaSensor.REPORTS_IN_ROOT_KEY)).thenReturn("true");
		sensorSpy.analyse(project, context);
		assertSameFiles(Arrays.asList(ROOT_REPORT, MODULE_REPORT), parsedReports);
	}

	@Test
	public void findReportsInModuleWithDefaultReportsInRootUnset() {
		setupModularProject();
		sensorSpy.analyse(project, context);
		assertSameFiles(Arrays.asList(MODULE_REPORT), parsedReports);
	}

	@Test
	public void findReportsInModuleWithDefaultReportsInRootFalse() {
		setupModularProject();
		when(settings.getString(SwiftCoberturaSensor.REPORTS_IN_ROOT_KEY)).thenReturn("false");
		sensorSpy.analyse(project, context);
		assertSameFiles(Arrays.asList(MODULE_REPORT), parsedReports);
	}

	@Test
	public void findReportsInModuleWithDefaultReportsInRootTrue() {
		setupModularProject();
		when(settings.getString(SwiftCoberturaSensor.REPORTS_IN_ROOT_KEY)).thenReturn("true");
		sensorSpy.analyse(project, context);
		assertSameFiles(Arrays.asList(ROOT_REPORT, MODULE_REPORT), parsedReports);
	}

	@Test
	public void findReportsInModuleWithModuleReportsInRootFalse() {
		setupModularProject();
		when(settings.getString(SwiftCoberturaSensor.REPORTS_IN_ROOT_KEY)).thenReturn("true");
		when(settings.getString(TEST_MOD_NAME + "." + SwiftCoberturaSensor.REPORTS_IN_ROOT_KEY)).thenReturn("false");
		sensorSpy.analyse(project, context);
		assertSameFiles(Arrays.asList(MODULE_REPORT), parsedReports);
	}

	@Test
	public void findReportsInModuleWithModuleReportsInRootTrue() {
		setupModularProject();
		when(settings.getString(SwiftCoberturaSensor.REPORTS_IN_ROOT_KEY)).thenReturn("false");
		when(settings.getString(TEST_MOD_NAME + "." + SwiftCoberturaSensor.REPORTS_IN_ROOT_KEY)).thenReturn("true");
		sensorSpy.analyse(project, context);
		assertSameFiles(Arrays.asList(ROOT_REPORT, MODULE_REPORT), parsedReports);
	}

	private void assertSameFiles(List<File> list1, List<File> list2) {
		assertEquals(getPaths(list1), getPaths(list2));
	}

	private Set<String> getPaths(List<File> files) {
		Set<String> paths = new HashSet();
		for (File file: files) {
			try {
				paths.add(file.getCanonicalPath());
			} catch (IOException e) {
				fail(e.toString());
			}
		}
		return paths;
	}
}
