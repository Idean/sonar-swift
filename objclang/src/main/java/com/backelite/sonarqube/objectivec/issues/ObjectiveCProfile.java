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
package com.backelite.sonarqube.objectivec.issues;

import com.backelite.sonarqube.objectivec.issues.fauxpas.FauxPasProfile;
import com.backelite.sonarqube.objectivec.issues.fauxpas.FauxPasProfileImporter;
import com.backelite.sonarqube.objectivec.issues.oclint.OCLintProfile;
import com.backelite.sonarqube.objectivec.issues.oclint.OCLintProfileImporter;
import com.backelite.sonarqube.objectivec.lang.core.ObjectiveC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.api.utils.ValidationMessages;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class ObjectiveCProfile implements BuiltInQualityProfilesDefinition {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectiveCProfile.class);
    private final OCLintProfileImporter ocLintProfileImporter;
    private final FauxPasProfileImporter fauxPasProfileImporter;

    public ObjectiveCProfile(final OCLintProfileImporter ocLintProfileImporter, final FauxPasProfileImporter fauxPasProfileImporter) {
        this.ocLintProfileImporter = ocLintProfileImporter;
        this.fauxPasProfileImporter = fauxPasProfileImporter;
    }

    @Override
    public void define(Context context) {
        LOGGER.info("Creating Objective-C Profile");

        NewBuiltInQualityProfile nbiqp = context.createBuiltInQualityProfile("Objective-C", ObjectiveC.KEY);
        nbiqp.setDefault(true);

        try(Reader config = new InputStreamReader(getClass().getResourceAsStream(OCLintProfile.PROFILE_PATH))) {
            RulesProfile ocLintRulesProfile = ocLintProfileImporter.importProfile(config, ValidationMessages.create());
            for (ActiveRule rule : ocLintRulesProfile.getActiveRules()) {
                nbiqp.activateRule(rule.getRepositoryKey(), rule.getRuleKey());
            }
        } catch (IOException ex){
            LOGGER.error("Error Creating Objective-C Profile",ex);
        }

        try(Reader config = new InputStreamReader(getClass().getResourceAsStream(FauxPasProfile.PROFILE_PATH))){
            RulesProfile fauxPasRulesProfile = fauxPasProfileImporter.importProfile(config, ValidationMessages.create());
            for (ActiveRule rule : fauxPasRulesProfile.getActiveRules()) {
                nbiqp.activateRule(rule.getRepositoryKey(),rule.getRuleKey());
            }
        } catch (IOException ex){
            LOGGER.error("Error Creating Objective-C Profile",ex);
        }
        nbiqp.done();
    }
}
