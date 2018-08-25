/**
 * Swift Language - Enables analysis of Swift and Objective-C projects into SonarQube.
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
package org.sonar.plugins.swift.issues;

import java.io.InputStreamReader;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.plugins.swift.issues.swiftlint.SwiftLintProfile;
import org.sonar.plugins.swift.issues.swiftlint.SwiftLintProfileImporter;
import org.sonar.plugins.swift.issues.tailor.TailorProfileImporter;
import org.sonar.plugins.swift.lang.core.Swift;

import com.google.common.io.Closeables;

public class SwiftProfile extends ProfileDefinition {

	private static final Logger LOGGER = LoggerFactory.getLogger(SwiftProfile.class);

	private final SwiftLintProfileImporter swiftLintProfileImporter;
	private final TailorProfileImporter tailorProfileImporter;

	public SwiftProfile(final SwiftLintProfileImporter swiftLintProfileImporter,
			final TailorProfileImporter tailorProfileImporter) {
		this.swiftLintProfileImporter = swiftLintProfileImporter;
		this.tailorProfileImporter = tailorProfileImporter;
	}

	@Override
	public RulesProfile createProfile(final ValidationMessages messages) {

		LOGGER.info("Creating Swift Profile");

		Reader config = null;
		final RulesProfile profile = RulesProfile.create("Swift", Swift.KEY);
		profile.setDefaultProfile(true);

		try {
			// Add swift lint rules
			config = new InputStreamReader(getClass().getResourceAsStream(SwiftLintProfile.PROFILE_PATH));
			RulesProfile ocLintRulesProfile = this.swiftLintProfileImporter.importProfile(config, messages);
			for (ActiveRule rule : ocLintRulesProfile.getActiveRules()) {
				profile.addActiveRule(rule);
			}


			return profile;
		} finally {

			Closeables.closeQuietly(config);
		}
	}
}
