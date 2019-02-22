/**
 * commons - Enables analysis of Swift and Objective-C projects into SonarQube.
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
package com.backelite.sonarqube.commons.surefire;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 2.8
 */
public class UnitTestIndex {

    private Map<String, UnitTestClassReport> indexByClassname;

    public UnitTestIndex() {
        this.indexByClassname = new HashMap<>();
    }

    public UnitTestClassReport index(String classname) {
        return indexByClassname.computeIfAbsent(classname, k -> new UnitTestClassReport());
    }

    public UnitTestClassReport get(String classname) {
        return indexByClassname.get(classname);
    }

    public Map<String, UnitTestClassReport> getIndexByClassname() {
        return indexByClassname;
    }

    public int size() {
        return indexByClassname.size();
    }
}