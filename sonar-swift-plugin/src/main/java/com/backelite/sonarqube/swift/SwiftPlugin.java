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
package com.backelite.sonarqube.swift;

import com.backelite.sonarqube.commons.surefire.SurefireSensor;
import com.backelite.sonarqube.commons.surefire.TestFileFinders;
import com.backelite.sonarqube.objectivec.ObjectiveCSquidSensor;
import com.backelite.sonarqube.objectivec.issues.ObjectiveCProfile;
import com.backelite.sonarqube.objectivec.issues.fauxpas.FauxPasProfile;
import com.backelite.sonarqube.objectivec.issues.fauxpas.FauxPasProfileImporter;
import com.backelite.sonarqube.objectivec.issues.fauxpas.FauxPasRulesDefinition;
import com.backelite.sonarqube.objectivec.issues.fauxpas.FauxPasSensor;
import com.backelite.sonarqube.objectivec.issues.oclint.OCLintProfile;
import com.backelite.sonarqube.objectivec.issues.oclint.OCLintProfileImporter;
import com.backelite.sonarqube.objectivec.issues.oclint.OCLintRulesDefinition;
import com.backelite.sonarqube.objectivec.issues.oclint.OCLintSensor;
import com.backelite.sonarqube.objectivec.lang.core.ObjectiveC;
import com.backelite.sonarqube.objectivec.surefire.ObjectiveCTestFileFinder;
import com.backelite.sonarqube.swift.complexity.LizardSensor;
import com.backelite.sonarqube.swift.coverage.CoberturaSensor;
import com.backelite.sonarqube.swift.issues.SwiftProfile;
import com.backelite.sonarqube.swift.issues.swiftlint.SwiftLintProfile;
import com.backelite.sonarqube.swift.issues.swiftlint.SwiftLintProfileImporter;
import com.backelite.sonarqube.swift.issues.swiftlint.SwiftLintRulesDefinition;
import com.backelite.sonarqube.swift.issues.swiftlint.SwiftLintSensor;
import com.backelite.sonarqube.swift.issues.tailor.TailorProfile;
import com.backelite.sonarqube.swift.issues.tailor.TailorProfileImporter;
import com.backelite.sonarqube.swift.issues.tailor.TailorRulesDefinition;
import com.backelite.sonarqube.swift.issues.tailor.TailorSensor;
import com.backelite.sonarqube.swift.lang.core.Swift;
import com.backelite.sonarqube.swift.surefire.SwiftTestFileFinder;
import com.google.common.collect.ImmutableList;
import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.Property;

@Properties({
        @Property(
                key = CoberturaSensor.REPORT_PATH_KEY,
                defaultValue = CoberturaSensor.DEFAULT_REPORT_PATTERN,
                name = "Path to Cobertura reports (coverage)",
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
                name = "Path to Lizard report (complexity)",
                description = "Relative to projects' root.",
                global = false,
                project = true),
        @Property(
                key = SurefireSensor.REPORT_PATH_KEY,
                defaultValue = SurefireSensor.DEFAULT_REPORT_PATH,
                name = "Path to surefire JUnit reports (unit tests)",
                description = "Relative to projects' root.",
                global = false,
                project = true),

        @Property(
                key = OCLintSensor.REPORT_PATH_KEY,
                defaultValue = OCLintSensor.DEFAULT_REPORT_PATH,
                name = "Path to OCLint pmd formatted report",
                description = "Relative to projects' root.",
                global = false,
                project = true),
        @Property(
                key = FauxPasSensor.REPORT_PATH_KEY,
                defaultValue = FauxPasSensor.DEFAULT_REPORT_PATH,
                name = "Path to FauxPas json formatted report",
                description = "Relative to projects' root.",
                global = false,
                project = true)

})
public class SwiftPlugin implements Plugin {

    @Override
    public void define(Context context) {
        TestFileFinders.getInstance().addFinder(new SwiftTestFileFinder());
        TestFileFinders.getInstance().addFinder(new ObjectiveCTestFileFinder());
        context.addExtensions(
            ImmutableList.of(
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

                // Code
                SwiftSquidSensor.class,
                ObjectiveCSquidSensor.class,

                // Surefire
                SurefireSensor.class,

                // Coverage
                CoberturaSensor.class,

                // Complexity
                LizardSensor.class
            )
        );
    }
}
