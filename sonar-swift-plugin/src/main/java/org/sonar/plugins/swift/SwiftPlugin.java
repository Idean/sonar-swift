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
package org.sonar.plugins.swift;

import java.util.List;

import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.SonarPlugin;
import org.sonar.plugins.swift.colorizer.SwiftCodeColorizerFormat;
import org.sonar.plugins.swift.complexity.LizardSensor;
import org.sonar.plugins.swift.coverage.SwiftCoberturaSensor;
import org.sonar.plugins.swift.cpd.SwiftCpdMapping;
import org.sonar.plugins.swift.issues.SwiftProfile;
import org.sonar.plugins.swift.issues.swiftlint.SwiftLintProfile;
import org.sonar.plugins.swift.issues.swiftlint.SwiftLintProfileImporter;
import org.sonar.plugins.swift.issues.swiftlint.SwiftLintRulesDefinition;
import org.sonar.plugins.swift.issues.swiftlint.SwiftLintSensor;
import org.sonar.plugins.swift.issues.tailor.TailorProfile;
import org.sonar.plugins.swift.issues.tailor.TailorProfileImporter;
import org.sonar.plugins.swift.issues.tailor.TailorRulesDefinition;
import org.sonar.plugins.swift.issues.tailor.TailorSensor;
import org.sonar.plugins.swift.lang.core.Swift;
import org.sonar.plugins.swift.tests.SwiftSurefireSensor;

import com.google.common.collect.ImmutableList;

@Properties({
        @Property(
                key = SwiftCoberturaSensor.REPORT_PATTERN_KEY,
                defaultValue = SwiftCoberturaSensor.DEFAULT_REPORT_PATTERN,
                name = "Path to unit test coverage report(s)",
                description = "Relative to projects' root. Ant patterns are accepted",
                global = false,
                project = true),
        @Property(
                key = SwiftLintSensor.REPORT_PATH_KEY,
                defaultValue = SwiftLintSensor.DEFAULT_REPORT_PATH,
                name = "Path to SwiftLint report",
                description = "Relative to projects' root.",
                global = false,
                project = true),
        @Property(
                key = TailorSensor.REPORT_PATH_KEY,
                defaultValue = TailorSensor.DEFAULT_REPORT_PATH,
                name = "Path to Tailor report",
                description = "Relative to projects' root.",
                global = false,
                project = true),
        @Property(
                key = LizardSensor.REPORT_PATH_KEY,
                defaultValue = LizardSensor.DEFAULT_REPORT_PATH,
                name = "Path to lizard report",
                description = "Relative to projects' root.",
                global = false,
                project = true)
})
public class SwiftPlugin extends SonarPlugin {

    // Global Swift constants
    public static final String FALSE = "false";

    public static final String FILE_SUFFIXES_KEY = "sonar.swift.file.suffixes";
    public static final String FILE_SUFFIXES_DEFVALUE = "swift";

    public static final String PROPERTY_PREFIX = "sonar.swift";

    public static final String TEST_FRAMEWORK_KEY = PROPERTY_PREFIX + ".testframework";
    public static final String TEST_FRAMEWORK_DEFAULT = "ghunit";

    @Override
    public List getExtensions() {
        return ImmutableList.of(
                // language support
                Swift.class,
                SwiftProfile.class,

                // SwiftLint rules
                SwiftLintSensor.class,
                SwiftLintRulesDefinition.class,

                // SwiftLint guality profile
                SwiftLintProfile.class,
                SwiftLintProfileImporter.class,
                
                // Tailor rules
                TailorSensor.class,
                TailorRulesDefinition.class,
                
                // Tailor quality profile
                TailorProfile.class,
                TailorProfileImporter.class,

                // duplications search
                SwiftCpdMapping.class,

                // code
                SwiftSquidSensor.class,

                // tests
                SwiftSurefireSensor.class,
                SwiftCoberturaSensor.class,

                // complexity
                LizardSensor.class);
    }
}
