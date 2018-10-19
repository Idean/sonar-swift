/**
 * Swift SonarQube Plugin - Swift module - Enables analysis of Swift and Objective-C projects into SonarQube.
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
package com.backelite.sonarqube.swift.issues.swiftlint;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SwiftLintReportParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwiftLintReportParser.class);

    private final SensorContext context;
    private final FileSystem fileSystem;

    public SwiftLintReportParser(final SensorContext context, final FileSystem fileSystem) {
        this.context = context;
        this.fileSystem = fileSystem;
    }

    public void parseReport(File reportFile) {
        try {
            // Read and parse report
            FileReader fr = new FileReader(reportFile);

            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                recordIssue(line);

            }
            IOUtils.closeQuietly(br);
            IOUtils.closeQuietly(fr);

        } catch (IOException e) {
            LOGGER.error("Failed to parse SwiftLint report file", e);
        }
    }

    private void recordIssue(final String line) {
        LOGGER.debug("record issue {}", line);

        Pattern pattern = Pattern.compile("(.*.swift):(\\w+):?(\\w+)?: (warning|error): (.*) \\((\\w+)");
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            String filePath = matcher.group(1);
            int lineNum = Integer.parseInt(matcher.group(2));
            String message = matcher.group(5);
            String ruleId = matcher.group(6);

            InputFile inputFile = fileSystem.inputFile(fileSystem.predicates().hasAbsolutePath(filePath));

            if (inputFile == null) {
                LOGGER.warn("file not included in sonar {}", filePath);
                continue;
            }

            NewIssue newIssue = context.newIssue();

            NewIssueLocation primaryLocation = newIssue.newLocation()
                    .message(message)
                    .on(inputFile)
                    .at(inputFile.selectLine(lineNum));

            newIssue
                    .forRule(RuleKey.of(SwiftLintRulesDefinition.REPOSITORY_KEY, ruleId))
                    .at(primaryLocation);

            newIssue.save();

        }
    }
}
