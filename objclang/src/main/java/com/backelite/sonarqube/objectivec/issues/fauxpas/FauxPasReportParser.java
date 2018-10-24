/**
 * Swift SonarQube Plugin - Objective-C module - Enables analysis of Swift and Objective-C projects into SonarQube.
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
package com.backelite.sonarqube.objectivec.issues.fauxpas;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.rule.RuleKey;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FauxPasReportParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(FauxPasReportParser.class);

    private final SensorContext context;

    public FauxPasReportParser(final SensorContext context) {
        this.context = context;
    }

    public void parseReport(File reportFile) {

        try(FileReader fr = new FileReader(reportFile)){
            // Read and parse report
            Object reportObj = JSONValue.parse(fr);

            // Record issues
            if (reportObj != null) {
                JSONObject reportJson = (JSONObject) reportObj;
                JSONArray diagnosticsJson = (JSONArray) reportJson.get("diagnostics");

                for (Object obj : diagnosticsJson) {
                    recordIssue((JSONObject) obj);
                }
            }

        } catch (FileNotFoundException e) {
            LOGGER.error("Failed to parse FauxPas report file", e);
        } catch (IOException e) {
            LOGGER.error("Failed to parse FauxPas report file", e);
        }
    }

    private void recordIssue(final JSONObject diagnosticJson) {
        String filePath = (String) diagnosticJson.get("file");
        if (filePath != null) {
            FilePredicate fp = context.fileSystem().predicates().hasAbsolutePath(filePath);

            if (!context.fileSystem().hasFiles(fp)) {
                LOGGER.warn("file not included in sonar {}", filePath);
                return;
            }

            JSONObject extent = (JSONObject) diagnosticJson.get("extent");
            JSONObject start = (JSONObject) extent.get("start");

            String info = (String) diagnosticJson.get("info");
            if (info == null) {
                info = (String) diagnosticJson.get("ruleName");
            }

            // Prevent line num 0 case
            int lineNum = Integer.parseInt(start.get("line").toString());
            if (lineNum == 0) {
                lineNum++;
            }

            InputFile inputFile = context.fileSystem().inputFile(fp);
            NewIssue issue = context.newIssue();

            NewIssueLocation issueLocation = issue.newLocation()
                    .message(info)
                    .on(inputFile)
                    .at(inputFile.selectLine(lineNum));

            issue
                    .forRule(RuleKey.of(FauxPasRulesDefinition.REPOSITORY_KEY, (String) diagnosticJson.get("ruleShortName")))
                    .at(issueLocation);

            issue.save();

        }
    }

}
