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
package com.backelite.sonarqube.objectivec.issues.oclint;

import java.io.File;

import javax.xml.stream.XMLStreamException;

import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.StaxParser.XmlStreamHandler;

final class OCLintXMLStreamHandler implements XmlStreamHandler {
    private static final int PMD_MINIMUM_PRIORITY = 5;
    private final Project project;
    private final SensorContext context;
    private final ResourcePerspectives resourcePerspectives;
    private final FileSystem fileSystem;

    public OCLintXMLStreamHandler(final Project p, final SensorContext c, final ResourcePerspectives resourcePerspectives, final FileSystem fileSystem) {
        project = p;
        context = c;
        this.resourcePerspectives = resourcePerspectives;
        this.fileSystem = fileSystem;
    }

    public void stream(final SMHierarchicCursor rootCursor) throws XMLStreamException {

        final SMInputCursor file = rootCursor.advance().childElementCursor("file");
        while (null != file.getNext()) {
            collectIssuesFor(file);
        }
    }

    private void collectIssuesFor(final SMInputCursor file) throws XMLStreamException {

        final String filePath = file.getAttrValue("name");
        LoggerFactory.getLogger(getClass()).debug("Collection issues for {}", filePath);
        final InputFile inputFile = findResource(filePath);
        if (fileExists(inputFile)) {
            LoggerFactory.getLogger(getClass()).debug("File {} was found in the project.", filePath);
            collectFileIssues(inputFile, file);
        }
    }

    private void collectFileIssues(final InputFile inputFile, final SMInputCursor file) throws XMLStreamException {

        final SMInputCursor line = file.childElementCursor("violation");

        while (null != line.getNext()) {
            recordViolation(inputFile, line);
        }
    }

    private InputFile findResource(final String filePath) {

        File file = new File(filePath);
        return fileSystem.inputFile(fileSystem.predicates().hasAbsolutePath(file.getAbsolutePath()));

    }

    private void recordViolation(InputFile inputFile, final SMInputCursor line) throws XMLStreamException {

        Issuable issuable = resourcePerspectives.as(Issuable.class, inputFile);

        if (issuable != null) {

            Issue issue = issuable.newIssueBuilder()
                    .ruleKey(RuleKey.of(OCLintRulesDefinition.REPOSITORY_KEY, line.getAttrValue("rule")))
                    .line(Integer.valueOf(line.getAttrValue("beginline")))
                    .message(line.getElemStringValue())
                    .build();

            issuable.addIssue(issue);


        }
    }

    private boolean fileExists(InputFile file) {
        if (file == null) {
            return false;
        }

        return context.getResource(file) != null;
    }

}
