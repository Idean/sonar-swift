/**
 * backelite-sonar-swift-plugin - Enables analysis of Swift projects into SonarQube.
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
package org.sonar.plugins.swift.issues.swiftlint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.profiles.ProfileImporter;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.profiles.XMLProfileParser;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.swift.lang.core.Swift;

import java.io.Reader;

public class SwiftLintProfileImporter extends ProfileImporter {

    private static final String UNABLE_TO_LOAD_DEFAULT_PROFILE = "Unable to load default SwiftLint profile";
    private static final Logger LOGGER = LoggerFactory.getLogger(SwiftLintProfileImporter.class);

    private final XMLProfileParser profileParser;

    public SwiftLintProfileImporter(final XMLProfileParser xmlProfileParser) {
        super(SwiftLintRulesDefinition.REPOSITORY_KEY, SwiftLintRulesDefinition.REPOSITORY_KEY);
        setSupportedLanguages(Swift.KEY);
        profileParser = xmlProfileParser;
    }

    @Override
    public RulesProfile importProfile(Reader reader, ValidationMessages messages) {

        final RulesProfile profile = profileParser.parse(reader, messages);

        if (null == profile) {
            messages.addErrorText(UNABLE_TO_LOAD_DEFAULT_PROFILE);
            LOGGER.error(UNABLE_TO_LOAD_DEFAULT_PROFILE);
        }

        return profile;
    }
}
