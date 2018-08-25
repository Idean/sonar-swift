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
package org.sonar.plugins.swift.lang.checks;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Created by gillesgrousset on 27/10/2015.
 */
public final class CheckList {

    public static final String REPOSITORY_KEY = "swift";

    public static final String SONAR_WAY_PROFILE = "Sonar way";

    private CheckList() {
    }

    public static List<Class> getChecks() {
        return ImmutableList.of(

        );
    }
}
