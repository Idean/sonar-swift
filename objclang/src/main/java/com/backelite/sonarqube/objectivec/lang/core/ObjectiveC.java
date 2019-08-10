/**
 * Swift SonarQube Plugin - Objective-C module - Enables analysis of Swift and Objective-C projects into SonarQube.
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
package com.backelite.sonarqube.objectivec.lang.core;

import com.backelite.sonarqube.objectivec.ObjectiveCConstants;
import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;

import java.util.ArrayList;
import java.util.List;

public class ObjectiveC extends AbstractLanguage {
    public static final String KEY = "objc";
    private final Configuration config;

    public ObjectiveC(Configuration config) {
        super(KEY, "Objective-C");
        this.config = config;
    }

    public String[] getFileSuffixes() {
        String[] suffixes = filterEmptyStrings(config.getStringArray(ObjectiveCConstants.FILE_SUFFIXES));
        if (suffixes.length == 0) {
            suffixes = ObjectiveCConstants.FILE_SUFFIXES.split( ",");
        }
        return suffixes;
    }

    private String[] filterEmptyStrings(String[] stringArray) {
        List<String> nonEmptyStrings = new ArrayList<>();
        for (String string : stringArray) {
            if (string.trim().length() > 0) {
                nonEmptyStrings.add(string.trim());
            }
        }
        return nonEmptyStrings.toArray(new String[nonEmptyStrings.size()]);
    }
}
