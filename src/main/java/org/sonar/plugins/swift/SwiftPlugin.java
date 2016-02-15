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
package org.sonar.plugins.swift;

import com.google.common.collect.ImmutableList;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.SonarPlugin;
import org.sonar.plugins.swift.colorizer.SwiftCodeColorizerFormat;
import org.sonar.plugins.swift.complexity.LizardSensor;
import org.sonar.plugins.swift.coverage.SwiftCoberturaSensor;
import org.sonar.plugins.swift.cpd.SwiftCpdMapping;
import org.sonar.plugins.swift.lang.core.Swift;
import org.sonar.plugins.swift.issues.SwiftProfile;
import org.sonar.plugins.swift.tests.SwiftSurefireSensor;
import org.sonar.plugins.swift.issues.swiftlint.*;

import java.util.List;

@Properties({
        @Property(key = SwiftCoberturaSensor.REPORT_PATTERN_KEY, defaultValue = SwiftCoberturaSensor.DEFAULT_REPORT_PATTERN, name = "Path to unit test coverage report(s)", description = "Relative to projects' root. Ant patterns are accepted", global = false, project = true),
        @Property(key = SwiftLintSensor.REPORT_PATH_KEY, defaultValue = SwiftLintSensor.DEFAULT_REPORT_PATH, name = "Path to SwiftLint report", description = "Relative to projects' root.", global = false, project = true),
        @Property(key = LizardSensor.REPORT_PATH_KEY, defaultValue = LizardSensor.DEFAULT_REPORT_PATH, name = "Path to lizard report", description = "Relative to projects' root.", global = false, project = true)
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
                Swift.class,
                SwiftCodeColorizerFormat.class,
                SwiftCpdMapping.class,

                SwiftSquidSensor.class,
                SwiftSurefireSensor.class,
                SwiftCoberturaSensor.class,

                SwiftProfile.class,

                SwiftLintSensor.class,
                SwiftLintRulesDefinition.class,
                SwiftLintProfile.class,
                SwiftLintProfileImporter.class,

                LizardSensor.class
                );
    }
}
