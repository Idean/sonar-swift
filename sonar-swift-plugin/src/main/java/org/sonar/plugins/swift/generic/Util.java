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
package org.sonar.plugins.swift.generic;

import org.apache.tools.ant.DirectoryScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Util {

    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);

    public static List<File> findReports(final String reportDirectory, final String reportPattern) {
        final String[] relPaths = filesMatchingPattern(reportDirectory, reportPattern);
        final List<File> reports = new ArrayList<File>();
        for (final String relPath : relPaths) {
            reports.add(new File(reportDirectory, relPath));
        }
        return reports;
    }

    private static String[] filesMatchingPattern(final String reportDirectory, final String reportPattern) {
        final DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes(reportPattern.split(","));
        scanner.setBasedir(new File(reportDirectory));
        scanner.scan();
        LOGGER.info("Files found in directory '{}' including '{}': {}", reportDirectory, reportPattern, Arrays.toString(scanner.getIncludedFiles()));
        return scanner.getIncludedFiles();
    }
}