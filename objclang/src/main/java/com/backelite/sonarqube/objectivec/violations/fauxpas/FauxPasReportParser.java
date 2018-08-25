/**
 * Objective-C Language - Enables analysis of Swift and Objective-C projects into SonarQube.
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
package com.backelite.sonarqube.objectivec.violations.fauxpas;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class FauxPasReportParser {

    private final Project project;
    private final SensorContext context;
    private final ResourcePerspectives resourcePerspectives;
    private final FileSystem fileSystem;

    private static final Logger LOGGER = LoggerFactory.getLogger(FauxPasReportParser.class);

    public FauxPasReportParser(final Project p, final SensorContext c, final ResourcePerspectives resourcePerspectives, final FileSystem fileSystem) {
        project = p;
        context = c;
        this.resourcePerspectives = resourcePerspectives;
        this.fileSystem = fileSystem;
    }

    public void parseReport(File reportFile) {

        try {
            // Read and parse report
            FileReader fr = new FileReader(reportFile);
            Object reportObj = JSONValue.parse(fr);
            IOUtils.closeQuietly(fr);

            // Record violations
            if (reportObj != null) {

                JSONObject reportJson = (JSONObject)reportObj;
                JSONArray diagnosticsJson = (JSONArray)reportJson.get("diagnostics");

                for (Object obj : diagnosticsJson) {
                    recordIssue((JSONObject) obj);

                }
            }

        } catch (FileNotFoundException e) {
            LOGGER.error("Failed to parse FauxPas report file", e);
        }
    }

    private void recordIssue(final JSONObject diagnosticJson) {

        String filePath = (String)diagnosticJson.get("file");

        if (filePath != null) {


            InputFile inputFile = fileSystem.inputFile(fileSystem.predicates().hasAbsolutePath(filePath));
            Issuable issuable = resourcePerspectives.as(Issuable.class, inputFile);

            if (issuable != null && inputFile != null) {

                JSONObject extent = (JSONObject)diagnosticJson.get("extent");
                JSONObject start = (JSONObject)extent.get("start");

                String info = (String)diagnosticJson.get("info");
                if (info == null) {
                    info = (String)diagnosticJson.get("ruleName");
                }

                // Prevent line num 0 case
                int lineNum = Integer.parseInt(start.get("line").toString());
                if (lineNum == 0) {
                    lineNum++;
                }

                Issue issue = issuable.newIssueBuilder()
                        .ruleKey(RuleKey.of(FauxPasRulesDefinition.REPOSITORY_KEY, (String) diagnosticJson.get("ruleShortName")))
                        .line(lineNum)
                        .message(info)
                        .build();

                issuable.addIssue(issue);


            }

        }

    }

}
