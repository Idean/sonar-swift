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

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.Violation;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SwiftLintReportParser {

    private final Project project;
    private final SensorContext context;

    private static final Logger LOGGER = LoggerFactory.getLogger(SwiftLintReportParser.class);

    public SwiftLintReportParser(final Project p, final SensorContext c) {
        project = p;
        context = c;
    }

    public Collection<Violation> parseReport(File reportFile) {


        final Collection<Violation> violations = new ArrayList<Violation>();

        try {
            // Read and parse report
            FileReader fr = new FileReader(reportFile);

            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                recordViolation(line, violations);

            }
            IOUtils.closeQuietly(br);
            IOUtils.closeQuietly(fr);

        } catch (FileNotFoundException e) {
            LOGGER.error("Failed to parse SwiftLint report file", e);
        } catch (IOException e) {
            LOGGER.error("Failed to parse SwiftLint report file", e);
        }


        return violations;
    }

    private void recordViolation(final String line, Collection<Violation> violations) {


        Pattern pattern = Pattern.compile("(.*.swift):(\\w+):?(\\w+)?: (warning|error): (.*) \\((\\w+)");
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            String filePath =  matcher.group(1);
            int lineNum = Integer.parseInt(matcher.group(2));
            String message = matcher.group(5);
            String ruleId = matcher.group(6);

            org.sonar.api.resources.File resource = org.sonar.api.resources.File.fromIOFile(new File(filePath), project);

            final Rule rule = Rule.create();
            final Violation violation = Violation.create(rule, resource);

            rule.setRepositoryKey(SwiftLintRuleRepository.REPOSITORY_KEY);
            rule.setKey(ruleId);

            violation.setMessage(message);

            violation.setLineId(lineNum);

            violations.add(violation);


        }

    }

}
