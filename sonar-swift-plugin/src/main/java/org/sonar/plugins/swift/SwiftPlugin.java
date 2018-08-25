/**
 * backelite-sonar-swift-plugin - Enables analysis of Swift and Objective-C projects into SonarQube.
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

import com.google.common.collect.ImmutableList;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.SonarPlugin;
import org.sonar.plugins.objectivec.ObjectiveCSquidSensor;
import org.sonar.plugins.objectivec.core.ObjectiveC;
import org.sonar.plugins.objectivec.coverage.CoberturaSensor;
import org.sonar.plugins.objectivec.cpd.ObjectiveCCpdMapping;
import org.sonar.plugins.objectivec.surefire.SurefireSensor;
import org.sonar.plugins.objectivec.violations.ObjectiveCProfile;
import org.sonar.plugins.objectivec.violations.fauxpas.FauxPasProfile;
import org.sonar.plugins.objectivec.violations.fauxpas.FauxPasProfileImporter;
import org.sonar.plugins.objectivec.violations.fauxpas.FauxPasRulesDefinition;
import org.sonar.plugins.objectivec.violations.fauxpas.FauxPasSensor;
import org.sonar.plugins.objectivec.violations.oclint.OCLintProfile;
import org.sonar.plugins.objectivec.violations.oclint.OCLintProfileImporter;
import org.sonar.plugins.objectivec.violations.oclint.OCLintRulesDefinition;
import org.sonar.plugins.objectivec.violations.oclint.OCLintSensor;
import org.sonar.plugins.swift.cpd.SwiftCpdMapping;
import org.sonar.plugins.swift.issues.SwiftProfile;
import org.sonar.plugins.swift.issues.swiftlint.SwiftLintProfileImporter;
import org.sonar.plugins.swift.issues.swiftlint.SwiftLintSensor;
import org.sonar.plugins.swift.issues.tailor.TailorProfileImporter;
import org.sonar.plugins.swift.issues.tailor.TailorRulesDefinition;
import org.sonar.plugins.swift.issues.tailor.TailorSensor;
import org.sonar.plugins.swift.surefire.SwiftSurefireSensor;
import org.sonar.plugins.swift.complexity.LizardSensor;
import org.sonar.plugins.swift.coverage.SwiftCoberturaSensor;
import org.sonar.plugins.swift.issues.swiftlint.SwiftLintProfile;
import org.sonar.plugins.swift.issues.swiftlint.SwiftLintRulesDefinition;
import org.sonar.plugins.swift.issues.tailor.TailorProfile;
import org.sonar.plugins.swift.lang.core.Swift;

import java.util.ArrayList;
import java.util.List;

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
                project = true),
        @Property(
                key = SwiftSurefireSensor.REPORTS_PATH_KEY,
                defaultValue = SwiftSurefireSensor.DEFAULT_REPORTS_PATH,
                name = "Path to surefire junit report",
                description = "Relative to projects' root.",
                global = false,
                project = true),


        @Property(
                key = CoberturaSensor.REPORT_PATTERN_KEY,
                defaultValue = CoberturaSensor.DEFAULT_REPORT_PATTERN,
                name = "Path to unit test coverage report(s)",
                description = "Relative to projects' root. Ant patterns are accepted",
                global = false,
                project = true),
        @Property(
                key = OCLintSensor.REPORT_PATH_KEY,
                defaultValue = OCLintSensor.DEFAULT_REPORT_PATH,
                name = "Path to oclint pmd formatted report",
                description = "Relative to projects' root.",
                global = false,
                project = true),
        @Property(
                key = FauxPasSensor.REPORT_PATH_KEY,
                defaultValue = FauxPasSensor.DEFAULT_REPORT_PATH,
                name = "Path to fauxpas json formatted report",
                description = "Relative to projects' root.",
                global = false,
                project = true)

})
public class SwiftPlugin extends SonarPlugin {

    @Override
    public List getExtensions() {


        return ImmutableList.of(
                // Language support
                Swift.class,
                SwiftProfile.class,
                ObjectiveC.class,
                ObjectiveCProfile.class,

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

                // OCLint rules
                OCLintSensor.class,
                OCLintRulesDefinition.class,

                // OCLint quality profile
                OCLintProfile.class,
                OCLintProfileImporter.class,

                // FauxPas rules
                FauxPasSensor.class,
                FauxPasRulesDefinition.class,

                // FauxPas quality profile
                FauxPasProfile.class,
                FauxPasProfileImporter.class,

                // Duplications search
                SwiftCpdMapping.class,
                ObjectiveCCpdMapping.class,

                // Code
                SwiftSquidSensor.class,
                ObjectiveCSquidSensor.class,

                // Surefire
                SwiftSurefireSensor.class,
                SwiftCoberturaSensor.class,
                SurefireSensor.class,
                CoberturaSensor.class,

                // Complexity
                LizardSensor.class

        );




    }
}
