/*
 * SonarQube Swift Plugin
 * Copyright (C) 2015 Backelite
 * dev@sonar.codehaus.org
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
package org.sonar.plugins.swift.violations;

import com.google.common.io.Closeables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.swift.lang.core.Swift;
import org.sonar.plugins.swift.violations.swiftlint.SwiftLintProfile;
import org.sonar.plugins.swift.violations.swiftlint.SwiftLintProfileImporter;

import java.io.InputStreamReader;
import java.io.Reader;

public class SwiftProfile extends ProfileDefinition {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwiftProfile.class);

    private final SwiftLintProfileImporter swiftLintProfileImporter;

    public SwiftProfile(final SwiftLintProfileImporter swiftLintProfileImporter) {
        this.swiftLintProfileImporter = swiftLintProfileImporter;
    }


    @Override
    public RulesProfile createProfile(ValidationMessages messages) {

        LOGGER.info("Creating Swift Profile");

        Reader config = null;
        final RulesProfile profile = RulesProfile.create("Swift", Swift.KEY);
        profile.setDefaultProfile(true);

        try {
            config = new InputStreamReader(getClass().getResourceAsStream(SwiftLintProfile.PROFILE_PATH));
            RulesProfile ocLintRulesProfile = swiftLintProfileImporter.importProfile(config, messages);
            for (ActiveRule rule : ocLintRulesProfile.getActiveRules()) {
                profile.addActiveRule(rule);
            }

            return profile;
        } finally {

            Closeables.closeQuietly(config);
        }
    }
}
