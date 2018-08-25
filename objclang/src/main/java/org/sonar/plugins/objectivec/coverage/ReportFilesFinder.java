/**
 * Objective-C Language - Enables analysis of Swift projects into SonarQube.
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
package org.sonar.plugins.objectivec.coverage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.DirectoryScanner;
import org.sonar.api.config.Settings;

final class ReportFilesFinder {
    private final Settings conf;
    private final String settingsKey;
    private final String settingsDefault;

    public ReportFilesFinder(final Settings settings, final String key,
            final String defaultValue) {
        conf = settings;
        settingsKey = key;
        settingsDefault = defaultValue;
    }

    public List<File> reportsIn(final String baseDirPath) {
        final String[] relPaths = filesMathingPattern(baseDirPath,
                reportPattern());

        final List<File> reports = new ArrayList<File>();
        for (final String relPath : relPaths) {
            reports.add(new File(baseDirPath, relPath));
        }

        return reports;
    }

    private String[] filesMathingPattern(final String baseDirPath,
            final String reportPath) {
        final DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes(new String[] { reportPath });
        scanner.setBasedir(new File(baseDirPath));
        scanner.scan();
        return scanner.getIncludedFiles();
    }

    private String reportPattern() {
        String reportPath = conf.getString(settingsKey);
        if (reportPath == null) {
            reportPath = settingsDefault;
        }
        return reportPath;
    }

}