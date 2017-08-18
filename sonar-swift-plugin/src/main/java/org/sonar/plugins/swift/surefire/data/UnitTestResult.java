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
package org.sonar.plugins.swift.surefire.data;

public final class UnitTestResult {
    public static final String STATUS_OK = "ok";
    public static final String STATUS_ERROR = "error";
    public static final String STATUS_FAILURE = "failure";
    public static final String STATUS_SKIPPED = "skipped";

    private String name;
    private String status;
    private String stackTrace;
    private String message;
    private long durationMilliseconds = 0L;

    public String getName() {
        return name;
    }

    public UnitTestResult setName(String name) {
        this.name = name;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public UnitTestResult setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public UnitTestResult setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public UnitTestResult setMessage(String message) {
        this.message = message;
        return this;
    }

    public long getDurationMilliseconds() {
        return durationMilliseconds;
    }

    public UnitTestResult setDurationMilliseconds(long l) {
        this.durationMilliseconds = l;
        return this;
    }

    public boolean isErrorOrFailure() {
        return STATUS_ERROR.equals(status) || STATUS_FAILURE.equals(status);
    }

    public boolean isError() {
        return STATUS_ERROR.equals(status);
    }
}
