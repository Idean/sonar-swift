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
package com.backelite.sonarqube.objectivec.surefire;

import com.backelite.sonarqube.commons.TestFileFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;

/**
 * Created by gillesgrousset on 28/08/2018.
 */
public class ObjectiveCTestFileFinder implements TestFileFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectiveCTestFileFinder.class);

    @Override
    public InputFile getUnitTestResource(FileSystem fileSystem, String classname) {
        String fileName = classname.replace('.', '/') + ".m";

        FilePredicate fp = fileSystem.predicates().hasPath(fileName);
        if(fileSystem.hasFiles(fp)){
            return fileSystem.inputFile(fp);
        }

        /*
         * Most xcodebuild JUnit parsers don't include the path to the class in the class field, so search for it if it
         * wasn't found in the root.
         */
        fp = fileSystem.predicates().and(
            fileSystem.predicates().hasType(InputFile.Type.TEST),
            fileSystem.predicates().matchesPathPattern("**/" + fileName.replace("_", "+")));

        if(fileSystem.hasFiles(fp)){
            /*
             * Lazily get the first file, since we wouldn't be able to determine the correct one from just the
             * test class name in the event that there are multiple matches.
             */
            return fileSystem.inputFiles(fp).iterator().next();
        }
        LOGGER.info("Unable to locate test source file {}", fileName);
        return null;
    }
}
