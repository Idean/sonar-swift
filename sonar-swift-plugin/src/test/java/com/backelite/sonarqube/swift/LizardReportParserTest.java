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
package com.backelite.sonarqube.swift;

import com.backelite.sonarqube.swift.complexity.LizardReportParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.batch.fs.*;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.measure.NewMeasure;

import java.io.File;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LizardReportParserTest {

    @Mock
    SensorContext sensorContext;

    @Mock
    FileSystem fileSystem;

    @Mock
    FilePredicates filePredicates;

    @Mock
    FilePredicate filePredicate;

    @Mock
    InputFile inputFile;

    @Mock
    NewMeasure<Integer> newMeasure;

    @Captor
    ArgumentCaptor<String> hasRelativePathCaptor;

    @Captor
    ArgumentCaptor<InputComponent> onCaptor;

    @Captor
    ArgumentCaptor<Metric<Integer>> forMetricCaptor;

    @Captor
    ArgumentCaptor<Integer> withValueCaptor;


    @Test
    public void parseSimpleFile() {

        LizardReportParser parser = new LizardReportParser(sensorContext);
        File xmlFile = new File("src/test/resources/lizard-report.xml");

        when(sensorContext.<Integer>newMeasure()).thenReturn(newMeasure);
        when(newMeasure.on(onCaptor.capture())).thenReturn(newMeasure);
        when(newMeasure.forMetric(forMetricCaptor.capture())).thenReturn(newMeasure);
        when(newMeasure.withValue(withValueCaptor.capture())).thenReturn(newMeasure);

        when(sensorContext.fileSystem()).thenReturn(fileSystem);
        when(fileSystem.predicates()).thenReturn(filePredicates);
        when(filePredicates.hasRelativePath(hasRelativePathCaptor.capture())).thenReturn(filePredicate);
        when(fileSystem.hasFiles(filePredicate)).thenReturn(true);
        when(fileSystem.inputFile(filePredicate)).thenReturn(inputFile);

        parser.parseReport(xmlFile);

        assertEquals(5, onCaptor.getAllValues().size());
        assertEquals(5, forMetricCaptor.getAllValues().size());

        assertEquals(Arrays.asList(1, 4, 8, 5, 46), withValueCaptor.getAllValues());
        assertEquals(Arrays.asList("./Folder With Space/File With Space.swift"), hasRelativePathCaptor.getAllValues());
    }

}
