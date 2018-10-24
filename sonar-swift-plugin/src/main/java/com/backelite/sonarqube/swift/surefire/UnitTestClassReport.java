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
package com.backelite.sonarqube.swift.surefire;

import java.util.ArrayList;
import java.util.List;

public final class UnitTestClassReport {
    private int errors = 0;
    private int failures = 0;
    private int skipped = 0;
    private int tests = 0;

    private long negativeTimeTestNumber = 0L;
    private List<UnitTestResult> results = null;

    public UnitTestClassReport add(UnitTestResult result) {
        if (results == null) {
            results = new ArrayList<>();
        }
        results.add(result);
        if (result.getStatus().equals(UnitTestResult.STATUS_SKIPPED)) {
            skipped += 1;

        } else if (result.getStatus().equals(UnitTestResult.STATUS_FAILURE)) {
            failures += 1;

        } else if (result.getStatus().equals(UnitTestResult.STATUS_ERROR)) {
            errors += 1;
        }
        tests += 1;
        if (result.getDurationMilliseconds() < 0) {
            negativeTimeTestNumber += 1;
        }
        return this;
    }

    public int getErrors() {
        return errors;
    }

    public int getFailures() {
        return failures;
    }

    public int getSkipped() {
        return skipped;
    }

    public int getTests() {
        return tests;
    }

    public long getNegativeTimeTestNumber() {
        return negativeTimeTestNumber;
    }

}
