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
package com.backelite.sonarqube.objectivec.issues.oclint;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.server.rule.RulesDefinition;
import com.backelite.sonarqube.objectivec.lang.core.ObjectiveC;
import org.sonar.squidbridge.rules.SqaleXmlLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gillesgrousset on 18/02/2016.
 */
public class OCLintRulesDefinition implements RulesDefinition {

    private static final Logger LOGGER = LoggerFactory.getLogger(OCLintRulesDefinition.class);

    public static final String REPOSITORY_KEY = "OCLint";
    public static final String REPOSITORY_NAME = REPOSITORY_KEY;

    private static final String RULES_FILE = "/org/sonar/plugins/oclint/rules.txt";

    @Override
    public void define(Context context) {

        NewRepository repository = context
                .createRepository(REPOSITORY_KEY, ObjectiveC.KEY)
                .setName(REPOSITORY_NAME);

        try {
            loadRules(repository);
        } catch (IOException e) {
            LOGGER.error("Failed to load OCLint rules", e);
        }

        SqaleXmlLoader.load(repository, "/com/sonar/sqale/oclint-model.xml");

        repository.done();

    }

    private void loadRules(NewRepository repository) throws IOException {

        Reader reader = new BufferedReader(new InputStreamReader(getClass()
                .getResourceAsStream(RULES_FILE), CharEncoding.UTF_8));

        final List<String> listLines = IOUtils.readLines(reader);

        String previousLine = null;
        Map<String, String> rule = new HashMap<String, String>();
        boolean inDescription = false;
        for (String line : listLines) {

            if (isLineIgnored(line)) {
                inDescription = false;

            } else if (line.matches("[\\-]{4,}.*")) {
                LOGGER.debug("Rule found : {}", previousLine);

                // Remove the rule name from the description of the previous
                // rule
                if (rule.get("description") != null) {
                    String description = rule.get("description").toString();
                    final int index = description.lastIndexOf(previousLine);
                    if (index > 0) {
                        rule.put("description", description.substring(0, index));
                    }

                }

                rule.clear();

                rule.put("name", StringUtils.capitalize(previousLine));
                rule.put("key", previousLine);


            } else if (line.matches("Summary:.*")) {
                inDescription = true;
                rule.put("description", line.substring(line.indexOf(':') + 1));
            } else if (line.matches("Category:.*")) {
                inDescription = true;

                // Create rule when last filed found
                RulesDefinition.NewRule newRule = repository.createRule(rule.get("key").toString());
                newRule.setName(rule.get("name").toString());
                newRule.setSeverity(rule.get("severity").toString());
                newRule.setHtmlDescription(rule.get("description").toString());

            } else if (line.matches("Severity:.*")) {
                inDescription = false;
                final String severity = line.substring("Severity: ".length());
                rule.put("severity", OCLintRuleSeverity.valueOfInt(Integer.valueOf(severity)).name());
            } else {
                if (inDescription) {
                    line = ruleDescriptionLink(line);
                    String description = (String)rule.get("description");
                    rule.put("description", description + "<br>" + line);
                }
            }

            previousLine = line;
        }

    }

    private boolean isLineIgnored(String line) {
        return line.matches("\\=.*") || line.matches("Priority:.*");
    }

    private String ruleDescriptionLink(final String line) {
        String result = line;
        final int indexOfLink = line.indexOf("http://");
        if (0 <= indexOfLink) {
            final String link = line.substring(indexOfLink);
            final StringBuilder htmlText = new StringBuilder("<a href=\"");
            htmlText.append(link);
            htmlText.append("\" target=\"_blank\">");
            htmlText.append(link);
            htmlText.append("</a>");
            result = htmlText.toString();
        }
        return result;
    }
}
