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

import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoverageMeasuresBuilder;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.resources.Resource;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public final class CoberturaMeasuresPersistorTest {

	@Test
	public void shouldNotPersistMeasuresForUnknownFiles() {

		final Project project = new Project("Test");

		final SensorContext context = mock(SensorContext.class);
        final ProjectFileSystem fileSystem = mock(ProjectFileSystem.class);
		final Map<String, CoverageMeasuresBuilder> measures = new HashMap<String, CoverageMeasuresBuilder>();
		measures.put("DummyResource", CoverageMeasuresBuilder.create());


        when(fileSystem.getBasedir()).thenReturn(new File("."));

		project.setFileSystem(fileSystem);

		final CoverageMeasuresPersistor testedPersistor = new CoverageMeasuresPersistor(project, context);
		testedPersistor.saveMeasures(measures);

		verify(context, never()).saveMeasure(any(Resource.class), any(Measure.class));
	}

	@Test
	public void shouldPersistMeasuresForKnownFiles() {

		final Project project = new Project("Test");
		final org.sonar.api.resources.File dummyFile = new org.sonar.api.resources.File("dummy/test");
		final SensorContext context = mock(SensorContext.class);
		final ProjectFileSystem fileSystem = mock(ProjectFileSystem.class);
        final List<File> sourceDirs = new ArrayList<File>();
		final Map<String, CoverageMeasuresBuilder> measures = new HashMap<String, CoverageMeasuresBuilder>();
		final CoverageMeasuresBuilder measureBuilder = CoverageMeasuresBuilder.create();

		sourceDirs.add(new File("/dummy"));
		measures.put("/dummy/test", measureBuilder);
		measureBuilder.setHits(99, 99);
		measureBuilder.setConditions(99, 99, 1);

		when(fileSystem.getSourceDirs()).thenReturn(sourceDirs);
		when(context.getResource(any(Resource.class))).thenReturn(dummyFile);
        when(fileSystem.getBasedir()).thenReturn(new File("."));

		project.setFileSystem(fileSystem);

		final CoverageMeasuresPersistor testedPersistor = new CoverageMeasuresPersistor(project, context);
		testedPersistor.saveMeasures(measures);

		for (final Measure measure : measureBuilder.createMeasures()) {
			verify(context, times(1)).saveMeasure(eq(org.sonar.api.resources.File.fromIOFile(new File(project.getFileSystem().getBasedir(), "dummy/test"), project)), eq(measure));
		}
	}

}
