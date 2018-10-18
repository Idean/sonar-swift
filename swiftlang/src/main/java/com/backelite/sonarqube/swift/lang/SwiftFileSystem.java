/**
 * Swift SonarQube Plugin - Swift module - Enables analysis of Swift and Objective-C projects into SonarQube.
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
package com.backelite.sonarqube.swift.lang;

import com.backelite.sonarqube.swift.lang.core.Swift;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;

import javax.annotation.CheckForNull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


@ScannerSide
public class SwiftFileSystem {

    private final FileSystem fileSystem;
    private final FilePredicates predicates;
    private final FilePredicate isSwiftLanguage;
    private final FilePredicate isMainTypeFile;

    public SwiftFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
        this.predicates = fileSystem.predicates();
        this.isSwiftLanguage = predicates.hasLanguage(Swift.KEY);
        this.isMainTypeFile = predicates.hasType(InputFile.Type.MAIN);
    }

    public boolean hasSwiftFiles() {
        return fileSystem.hasFiles(isSwiftLanguage);
    }

    public List<File> sourceFiles() {
        Iterable<File> files = fileSystem.files(predicates.and(isSwiftLanguage, isMainTypeFile));
        List<File> list = new ArrayList<>();
        files.iterator().forEachRemaining(list::add);
        return list;
    }

    public List<InputFile> swiftInputFiles() {
        Iterable<InputFile> inputFiles = fileSystem.inputFiles(isSwiftLanguage);
        List<InputFile> list = new ArrayList<>();
        inputFiles.iterator().forEachRemaining(list::add);
        return list;
    }

    public List<InputFile> sourceInputFiles() {
        Iterable<InputFile> inputFiles = fileSystem.inputFiles(predicates.and(isSwiftLanguage, isMainTypeFile));
        List<InputFile> list = new ArrayList<>();
        inputFiles.iterator().forEachRemaining(list::add);
        return list;
    }

    @CheckForNull
    public InputFile sourceInputFileFromRelativePath(String relativePath) {
        return fileSystem.inputFile(predicates.and(predicates.matchesPathPattern("**/" + relativePath), isSwiftLanguage, isMainTypeFile));
    }

    public File baseDir() {
        return fileSystem.baseDir();
    }
}
