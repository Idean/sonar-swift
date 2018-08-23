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
package org.sonar.plugins.swift.coverage;

import java.io.*;
import java.util.*;

import org.junit.Before;
import org.junit.Test;

import org.sonar.api.config.Settings;
import org.sonar.plugins.swift.lang.core.Swift;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ReportFilesFinderTest {

	private static final String TEST_REPORT_PATTERN = "**/cobertura.xml";
	private static final String TEST_MATERIALS_DIR = "./src/test/resources/coverage";
	private static final String TEST_MOD_NAME = "myMod";
	private static final File REPORT_PATH_1 = new File(TEST_MATERIALS_DIR + "/dir1/cobertura.xml");
	private static final File REPORT_PATH_2 = new File(TEST_MATERIALS_DIR + "/dir2/cobertura.xml");
	private static final File REPORT_PATH_3 = new File(TEST_MATERIALS_DIR + "/dir3/cobertura.xml");

	private Settings settings;
	private ReportFilesFinder reportFilesFinder;

	@Before
	public void setUp() {
		settings = mock(Settings.class);
		when(settings.getString(SwiftCoberturaSensor.REPORT_PATTERN_KEY)).thenReturn(TEST_REPORT_PATTERN);
		when(settings.getString(TEST_MOD_NAME + "." + SwiftCoberturaSensor.REPORT_PATTERN_KEY)).thenReturn(TEST_REPORT_PATTERN);

		reportFilesFinder = new ReportFilesFinder(settings, SwiftCoberturaSensor.REPORT_PATTERN_KEY, 
			SwiftCoberturaSensor.DEFAULT_REPORT_PATTERN, SwiftCoberturaSensor.REPORT_DIRECTORY_KEY);
	}

	@Test
	public void findsFoldersInRootWithNoReportsDirectory() {
		assertSameFiles(Arrays.asList(REPORT_PATH_1, REPORT_PATH_2, REPORT_PATH_3), reportFilesFinder.reportsIn(TEST_MATERIALS_DIR));
	}

	@Test
	public void findsFoldersInRootWithReportsDirectory() {
		when(settings.getString(SwiftCoberturaSensor.REPORT_DIRECTORY_KEY)).thenReturn("/dir1");
		assertSameFiles(Arrays.asList(REPORT_PATH_1), reportFilesFinder.reportsIn(TEST_MATERIALS_DIR));
	}

	@Test
	public void findsFoldersInModuleWithNoReportsDirectory() {
		assertSameFiles(Arrays.asList(REPORT_PATH_1), reportFilesFinder.reportsIn(TEST_MOD_NAME, TEST_MATERIALS_DIR, TEST_MATERIALS_DIR + "/dir1"));
	}

	@Test
	public void findsFoldersInModuleWithDefaultReportsDirectory() {
		when(settings.getString(SwiftCoberturaSensor.REPORT_DIRECTORY_KEY)).thenReturn("/dir2");
		assertSameFiles(Arrays.asList(REPORT_PATH_2), reportFilesFinder.reportsIn(TEST_MOD_NAME, TEST_MATERIALS_DIR, TEST_MATERIALS_DIR + "/dir1"));
	}

	@Test
	public void findsFoldersInModuleWithModuleReportsDirectory() {
		when(settings.getString(SwiftCoberturaSensor.REPORT_DIRECTORY_KEY)).thenReturn("/dir2");
		when(settings.getString(TEST_MOD_NAME + "." + SwiftCoberturaSensor.REPORT_DIRECTORY_KEY)).thenReturn("/dir3");
		assertSameFiles(Arrays.asList(REPORT_PATH_3), reportFilesFinder.reportsIn(TEST_MOD_NAME, TEST_MATERIALS_DIR, TEST_MATERIALS_DIR + "/dir1"));
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