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
package org.sonar.plugins.swift.coverage;

import org.apache.tools.ant.DirectoryScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Settings;

import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

final class ReportFilesFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportFilesFinder.class);

    private final Settings conf;
    private final String settingsReportKey;
    private final String settingsReportDefault;
    private final String settingsDirectoryKey;

    public ReportFilesFinder(final Settings settings, final String settingsReportKey, final String settingsReportDefault, final String settingsDirectoryKey) {

        conf = settings;
        this.settingsReportKey = settingsReportKey;
        this.settingsReportDefault = settingsReportDefault;
        this.settingsDirectoryKey = settingsDirectoryKey;
    }

    public List<File> reportsIn(final String module, final String rootDirectory, final String baseDirectory) {

        final String reportDirectory = getReportDirectory(module, rootDirectory, baseDirectory);
        final String reportPattern = getReportPattern(module);
        final String[] relPaths = filesMatchingPattern(reportDirectory, reportPattern);

        final List<File> reports = new ArrayList<File>();

        for (final String relPath : relPaths) {
            reports.add(new File(reportDirectory, relPath));
        }

        return reports;
    }

    private String[] filesMatchingPattern(final String reportDirectory, final String reportPath) {

        final DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes(new String[] { reportPath });
        scanner.setBasedir(new File(reportDirectory));
        scanner.scan();
        
        LOGGER.info("Files found in directory '" + reportDirectory + "' including '" + reportPath + "': " + Arrays.toString(scanner.getIncludedFiles()));

        return scanner.getIncludedFiles();
    }

    private String getReportPattern(final String module) {

        String reportPath = conf.getString(module + "." + settingsReportKey);

        if (reportPath == null) {
            reportPath = conf.getString(settingsReportKey);
        }

        if (reportPath == null) {
            reportPath = settingsReportDefault;
        }

        return reportPath;
    }

    private String getReportDirectory(final String module, final String rootDirectory, final String defaultDirectory) {

        String reportDirectory = conf.getString(module + "." + settingsDirectoryKey);

        if (reportDirectory == null) {
            reportDirectory = conf.getString(settingsDirectoryKey);
        }

        if (reportDirectory != null) {
            reportDirectory = rootDirectory + reportDirectory;
        }
        else {
            reportDirectory = defaultDirectory;
        }

        return reportDirectory;
    }

}