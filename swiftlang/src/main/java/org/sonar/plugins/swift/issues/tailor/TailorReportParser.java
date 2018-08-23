/**
 * Swift Language - Enables analysis of Swift projects into SonarQube.
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
package org.sonar.plugins.swift.issues.tailor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;

/**
 * Created by tzwickl on 22/11/2016.
 */

public class TailorReportParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(TailorReportParser.class);

	private final Project project;
	private final SensorContext context;
	private final ResourcePerspectives resourcePerspectives;
	private final FileSystem fileSystem;

	public TailorReportParser(final Project project, final SensorContext context,
			final ResourcePerspectives resourcePerspectives, final FileSystem fileSystem) {
		this.project = project;
		this.context = context;
		this.resourcePerspectives = resourcePerspectives;
		this.fileSystem = fileSystem;
	}

	public void parseReport(final File reportFile) {
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

		} catch (FileNotFoundException e) {
			LOGGER.error("Failed to parse SwiftLint report file", e);
		} catch (IOException e) {
			LOGGER.error("Failed to parse SwiftLint report file", e);
		}
	}

	private void recordIssue(final String line) {
		LOGGER.debug("record issue {}", line);

		Pattern pattern = Pattern.compile("(.*.swift):(\\w+):(\\w+): (.*): \\[(.*)\\] (.*)");
		Matcher matcher = pattern.matcher(line);
		while (matcher.find()) {
			String filePath = matcher.group(1);
			int lineNum = Integer.parseInt(matcher.group(2));
			String ruleId = matcher.group(5);
			String message = matcher.group(6);

			InputFile inputFile = this.fileSystem.inputFile(this.fileSystem.predicates().hasAbsolutePath(filePath));

			if (inputFile == null) {
				LOGGER.warn("file not included in sonar {}", filePath);
				continue;
			}

			Issuable issuable = this.resourcePerspectives.as(Issuable.class, inputFile);

			if (issuable != null) {
				Issue issue = issuable.newIssueBuilder()
						.ruleKey(RuleKey.of(TailorRulesDefinition.REPOSITORY_KEY, ruleId)).line(lineNum)
						.message(message).build();

				try {
					issuable.addIssue(issue);
				} catch (Exception e) {
					// Unable to add issue : probably because does not exist in the repository
					LOGGER.warn(e.getMessage());
				}
			}
		}
	}
}
