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
import org.sonar.api.utils.StaxParser;

import javax.xml.stream.XMLStreamException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public final class CoberturaXMLStreamHandlerTest {

	private final String EMPTY_REPORT = "<?xml version=\"1.0\" ?><!DOCTYPE coverage SYSTEM \'http://cobertura.sourceforge.net/xml/coverage-03.dtd\'><coverage branch-rate=\"0.0\" line-rate=\"0.0679012345679\" timestamp=\"1373572927\" version=\"gcovr 2.5-prerelease (r2876)\"><sources><source>.</source></sources><packages><package branch-rate=\"0.0\" complexity=\"0.0\" line-rate=\"0.0679012345679\" name=\"REPORTNAME\"><classes></classes></package></packages></coverage>";
	private final String VALID_REPORT = "<?xml version=\"1.0\" ?><!DOCTYPE coverage SYSTEM \'http://cobertura.sourceforge.net/xml/coverage-03.dtd\'><coverage branch-rate=\"0.0\" line-rate=\"0.0679012345679\" timestamp=\"1373572927\" version=\"gcovr 2.5-prerelease (r2876)\"><sources><source>.</source></sources><packages><package branch-rate=\"0.0\" complexity=\"0.0\" line-rate=\"0.0679012345679\" name=\"REPORTNAME\"><classes><class branch-rate=\"0.0\" complexity=\"0.0\" filename=\"FILEPATH\" line-rate=\"0.0679012345679\" name=\"FILENAME\"><lines><line branch=\"false\" hits=\"0\" number=\"25\"/><line branch=\"false\" hits=\"1\" number=\"29\"/><line branch=\"true\" condition-coverage=\"100% (1/2)\" hits=\"10\" number=\"35\"/></lines></class></classes></package></packages></coverage>";
	private final String FILE_PATH = "FILEPATH";
	private final int NO_HIT_LINE = 25;
	private final int NO_BRANCH_LINE = 29;
	private final int BRANCH_LINE = 35;

	@Test
	public void streamLeavesTheMapEmptyWhenNoLinesAreFound() throws XMLStreamException {

		final Map<String, CoverageMeasuresBuilder> parseResults = new HashMap<String, CoverageMeasuresBuilder>();
		final StaxParser parser = new StaxParser(new CoberturaXMLStreamHandler(parseResults));

		parser.parse(new StringInputStream(EMPTY_REPORT));

		assertTrue(parseResults.isEmpty());
	}

	@Test
	public void streamAddsACoverageMeasureBuilderForClassesInTheReport() throws XMLStreamException {

		final Map<String, CoverageMeasuresBuilder> parseResults = new HashMap<String, CoverageMeasuresBuilder>();
		final StaxParser parser = new StaxParser(new CoberturaXMLStreamHandler(parseResults));

		parser.parse(new StringInputStream(VALID_REPORT));

		assertNotNull(parseResults.get(FILE_PATH));
	}

	@Test
	public void streamRecords0HitsForLinesWithNoHits() throws XMLStreamException {

		final Map<String, CoverageMeasuresBuilder> parseResults = new HashMap<String, CoverageMeasuresBuilder>();
		final StaxParser parser = new StaxParser(new CoberturaXMLStreamHandler(parseResults));

		parser.parse(new StringInputStream(VALID_REPORT));

		assertEquals(Integer.valueOf(0), parseResults.get(FILE_PATH).getHitsByLine().get(NO_HIT_LINE));
	}

	@Test
	public void streamRecordsHitsForLinesWithNoBranch() throws XMLStreamException {

		final Map<String, CoverageMeasuresBuilder> parseResults = new HashMap<String, CoverageMeasuresBuilder>();
		final StaxParser parser = new StaxParser(new CoberturaXMLStreamHandler(parseResults));

		parser.parse(new StringInputStream(VALID_REPORT));

		assertEquals(Integer.valueOf(1), parseResults.get(FILE_PATH).getHitsByLine().get(NO_BRANCH_LINE));
	}

	@Test
	public void streamRecordsHitsForLinesWithBranch() throws XMLStreamException {

		final Map<String, CoverageMeasuresBuilder> parseResults = new HashMap<String, CoverageMeasuresBuilder>();
		final StaxParser parser = new StaxParser(new CoberturaXMLStreamHandler(parseResults));

		parser.parse(new StringInputStream(VALID_REPORT));

		assertEquals(Integer.valueOf(10), parseResults.get(FILE_PATH).getHitsByLine().get(BRANCH_LINE));
	}

	@Test
	public void streamRecordsConditionsForLinesWithBranch() throws XMLStreamException {

		final Map<String, CoverageMeasuresBuilder> parseResults = new HashMap<String, CoverageMeasuresBuilder>();
		final StaxParser parser = new StaxParser(new CoberturaXMLStreamHandler(parseResults));

		parser.parse(new StringInputStream(VALID_REPORT));

		assertEquals(Integer.valueOf(2), parseResults.get(FILE_PATH).getConditionsByLine().get(BRANCH_LINE));
	}

	@Test
	public void streamRecordsConditionsHitsForLinesWithBranch() throws XMLStreamException {

		final Map<String, CoverageMeasuresBuilder> parseResults = new HashMap<String, CoverageMeasuresBuilder>();
		final StaxParser parser = new StaxParser(new CoberturaXMLStreamHandler(parseResults));

		parser.parse(new StringInputStream(VALID_REPORT));

		assertEquals(Integer.valueOf(1), parseResults.get(FILE_PATH).getCoveredConditionsByLine().get(BRANCH_LINE));
	}

}
