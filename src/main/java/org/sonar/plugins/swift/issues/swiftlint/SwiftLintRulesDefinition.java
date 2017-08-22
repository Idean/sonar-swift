/*
 * SonarQube Swift Plugin
 * Copyright (C) 2015 Backelite
 * sonarqube@googlegroups.com
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
package org.sonar.plugins.swift.issues.swiftlint;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharEncoding;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.swift.lang.core.Swift;
import org.sonar.squidbridge.rules.SqaleXmlLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Created by gillesgrousset on 27/11/2015.
 */
public class SwiftLintRulesDefinition implements RulesDefinition {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwiftLintRulesDefinition.class);

    public static final String REPOSITORY_KEY = "SwiftLint";
    public static final String REPOSITORY_NAME = REPOSITORY_KEY;

    private static final String RULES_FILE = "/org/sonar/plugins/swiftlint/rules.json";

    @Override
    public void define(Context context) {

        NewRepository repository = context
                .createRepository(REPOSITORY_KEY, Swift.KEY)
                .setName(REPOSITORY_NAME);

        try {
            loadRules(repository);
        } catch (IOException e) {
            LOGGER.error("Failed to load SwiftLint rules", e);
        }

        SqaleXmlLoader.load(repository, "/com/sonar/sqale/swiftlint-model.xml");

        repository.done();

    }

    private void loadRules(NewRepository repository) throws IOException {

        Reader reader = new BufferedReader(new InputStreamReader(getClass()
                .getResourceAsStream(RULES_FILE), CharEncoding.UTF_8));

        String jsonString = IOUtils.toString(reader);

        Object rulesObj = JSONValue.parse(jsonString);

        if (rulesObj != null) {
            JSONArray slRules = (JSONArray)rulesObj;
            for (Object obj : slRules) {
                JSONObject slRule = (JSONObject)obj;

                RulesDefinition.NewRule rule = repository.createRule((String)slRule.get("key"));
                rule.setName((String) slRule.get("name"));
                rule.setSeverity((String)slRule.get("severity"));
                rule.setHtmlDescription((String) slRule.get("description"));

            }
        }
    }
}
