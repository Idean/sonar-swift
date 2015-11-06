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
package org.sonar.plugins.swift.violations.swiftlint;

import org.junit.Test;
import org.sonar.api.rules.Rule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by gillesgrousset on 12/02/15.
 */
public class SwiftLintRuleParserTest {

    private static final String VALID_JSON_RULES = "[{\"category\":\"SwiftLint\",\"key\":\"function_body_length\",\"name\":\"Function Body Length Rule\",\"description\":\"Enforce maximum function length\",\"severity\":\"MAJOR\"}]";

    @Test
    public void parseShouldReturnAnEmptyListIfNoValidRulesFile() throws IOException {
        SwiftLintRuleParser parser = new SwiftLintRuleParser();
        List<Rule> rules = parser.parse(new BufferedReader(new StringReader("")));

        assertTrue(rules.isEmpty());
    }

    @Test
    public void parseShouldReturnAListOfRulesIfValideFile() throws IOException {

        SwiftLintRuleParser parser = new SwiftLintRuleParser();
        List<Rule> rules = parser.parse(new BufferedReader(new StringReader(VALID_JSON_RULES)));

        assertEquals(1, rules.size());
    }
}
