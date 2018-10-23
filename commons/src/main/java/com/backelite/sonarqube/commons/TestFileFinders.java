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
package com.backelite.sonarqube.commons;

import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;

import java.util.ArrayList;
import java.util.List;

public class TestFileFinders {
    private static TestFileFinders instance;
    private final List<TestFileFinder> finders = new ArrayList<>();
    private TestFileFinders() {}

    public static synchronized TestFileFinders getInstance() {
        if (instance == null) {
            instance = new TestFileFinders();
        }
        return instance;
    }

    public void addFinder(TestFileFinder finder) {
        finders.add(finder);
    }

    InputFile getUnitTestResource(FileSystem fileSystem, String classname) {
        for (TestFileFinder finder : finders) {
            InputFile result = finder.getUnitTestResource(fileSystem, classname);
            if (result != null) {
                return result;
            }
        }

        return null;
    }


}
