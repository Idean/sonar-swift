/**
 * Swift SonarQube Plugin - Swift module - Enables analysis of Swift and Objective-C projects into SonarQube.
 * Copyright © 2015 Backelite (${email})
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.backelite.sonarqube.swift.issues.swiftlint;

import com.backelite.sonarqube.swift.lang.core.Swift;
import com.google.common.io.Closeables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.utils.ValidationMessages;

import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Created by gillesgrousset on 03/11/2015.
 */

public class SwiftLintProfile extends ProfileDefinition {

    public static final String PROFILE_PATH = "/org/sonar/plugins/swiftlint/profile-swiftlint.xml";
    private static final Logger LOGGER = LoggerFactory.getLogger(SwiftLintProfile.class);

    private final SwiftLintProfileImporter profileImporter;

    public SwiftLintProfile(final SwiftLintProfileImporter importer) {
        profileImporter = importer;
    }

    @Override
    public RulesProfile createProfile(ValidationMessages messages) {
        LOGGER.info("Creating SwiftLint Profile");
        Reader config = null;

        try {
            config = new InputStreamReader(getClass().getResourceAsStream(
                    PROFILE_PATH));
            final RulesProfile profile = profileImporter.importProfile(config, messages);
            profile.setName(SwiftLintRulesDefinition.REPOSITORY_KEY);
            profile.setLanguage(Swift.KEY);

            return profile;
        } finally {
            Closeables.closeQuietly(config);
        }
    }
}
