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

import com.backelite.sonarqube.objectivec.core.ObjectiveC;
import com.google.common.io.Closeables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.utils.ValidationMessages;

import java.io.InputStreamReader;
import java.io.Reader;

public class FauxPasProfile extends ProfileDefinition {

    public static final String PROFILE_PATH = "/org/sonar/plugins/fauxpas/profile-fauxpas.xml";
    private static final Logger LOGGER = LoggerFactory.getLogger(FauxPasProfile.class);

    private final FauxPasProfileImporter profileImporter;

    public FauxPasProfile(final FauxPasProfileImporter importer) {
        profileImporter = importer;
    }

    @Override
    public RulesProfile createProfile(ValidationMessages messages) {
        LOGGER.info("Creating FauxPas Profile");
        Reader config = null;

        try {
            config = new InputStreamReader(getClass().getResourceAsStream(
                    PROFILE_PATH));
            final RulesProfile profile = profileImporter.importProfile(config, messages);
            profile.setName(FauxPasRulesDefinition.REPOSITORY_KEY);
            profile.setLanguage(ObjectiveC.KEY);

            return profile;
        } finally {
            Closeables.closeQuietly(config);
        }
    }
}
