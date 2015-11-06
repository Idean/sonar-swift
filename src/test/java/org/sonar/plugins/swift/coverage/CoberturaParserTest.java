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

import org.apache.tools.ant.filters.StringInputStream;
import org.junit.Test;
import org.sonar.api.measures.CoverageMeasuresBuilder;

import java.io.File;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public final class CoberturaParserTest {

	private final String VALID_REPORT_FILE_PATH = "FILEPATH";
	private final String VALID_REPORT = "<?xml version=\"1.0\" ?><!DOCTYPE coverage SYSTEM \'http://cobertura.sourceforge.net/xml/coverage-03.dtd\'><coverage branch-rate=\"0.0\" line-rate=\"0.0679012345679\" timestamp=\"1373572927\" version=\"gcovr 2.5-prerelease (r2876)\"><sources><source>.</source></sources><packages><package branch-rate=\"0.0\" complexity=\"0.0\" line-rate=\"0.0679012345679\" name=\"REPORTNAME\"><classes><class branch-rate=\"0.0\" complexity=\"0.0\" filename=\"FILEPATH\" line-rate=\"0.0679012345679\" name=\"FILENAME\"><lines></lines></class></classes></package></packages></coverage>";

	@Test
	public void parseReportShouldReturnAnEmptyMapWhenTheReportIsInvalid() {

		final CoberturaParser coberturaParser = new CoberturaParser();
		final Map<String, CoverageMeasuresBuilder> measures = coberturaParser.parseReport(new StringInputStream(""));

		assertTrue(measures.isEmpty());
	}

	@Test
	public void parseReportShouldReturnAnEmptyMapWhenTheFileIsInvalid() {

		final CoberturaParser coberturaParser = new CoberturaParser();
		final Map<String, CoverageMeasuresBuilder> measures = coberturaParser.parseReport(new File(""));

		assertTrue(measures.isEmpty());
	}

	@Test
	public void parseReportShouldReturnAMapOfFileToMeasuresWhenTheReportIsValid() {

		final CoberturaParser coberturaParser = new CoberturaParser();
		final Map<String, CoverageMeasuresBuilder> measures = coberturaParser.parseReport(new StringInputStream(VALID_REPORT));

		assertNotNull(measures.get(VALID_REPORT_FILE_PATH));
	}

}
