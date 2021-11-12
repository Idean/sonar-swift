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
package com.backelite.sonarqube.swift.issues.tailor;

import com.backelite.sonarqube.swift.lang.core.Swift;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.squidbridge.rules.SqaleXmlLoader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * Created by tzwickl on 22/11/2016.
 */

public class TailorRulesDefinition implements RulesDefinition {
    private static final Logger LOGGER = LoggerFactory.getLogger(TailorRulesDefinition.class);
    public static final String REPOSITORY_KEY = "Tailor";
    public static final String REPOSITORY_NAME = REPOSITORY_KEY;
    private static final String RULES_FILE = "/org/sonar/plugins/tailor/rules.json";

    @Override
    public void define(final Context context) {
        NewRepository repository = context.createRepository(REPOSITORY_KEY, Swift.KEY).setName(REPOSITORY_NAME);

        try(Reader reader = new InputStreamReader(getClass().getResourceAsStream(RULES_FILE), Charset.forName("UTF-8"))){
            JSONArray slRules = (JSONArray)JSONValue.parse(reader);
            if(slRules != null){
                for (Object obj : slRules) {
                    JSONObject slRule = (JSONObject) obj;
                    repository.createRule((String) slRule.get("key"))
                        .setName((String) slRule.get("name"))
                        .setSeverity((String) slRule.get("severity"))
                        .setHtmlDescription(slRule.get("description") +
                            " (<a href=" + slRule.get("styleguide") + ">" + slRule.get("styleguide") + "</a>)");
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load tailor rules", e);
        }
        repository.done();
    }
}
