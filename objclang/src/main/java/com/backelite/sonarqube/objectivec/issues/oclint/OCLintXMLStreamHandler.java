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

import com.backelite.sonarqube.commons.surefire.StaxParser;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.batch.sensor.issue.internal.DefaultIssueLocation;
import org.sonar.api.rule.RuleKey;

import javax.xml.stream.XMLStreamException;
import java.io.File;

final class OCLintXMLStreamHandler implements StaxParser.XmlStreamHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(OCLintXMLStreamHandler.class);
    private static final int PMD_MINIMUM_PRIORITY = 5;
    private final SensorContext context;

    public OCLintXMLStreamHandler(SensorContext context) {
        this.context = context;
    }

    public void stream(final SMHierarchicCursor rootCursor) throws XMLStreamException {
        final SMInputCursor inputCursor = rootCursor.advance().childElementCursor("file");
        while (null != inputCursor.getNext()) {
            final String filePath = inputCursor.getAttrValue("name");
            LOGGER.debug("Collection issues for {}", filePath);

            File file = new File(filePath);
            FilePredicate fp = context.fileSystem().predicates().hasAbsolutePath(file.getAbsolutePath());
            if(!context.fileSystem().hasFiles(fp)){
                LOGGER.warn("file not included in sonar {}", filePath);
            }

            InputFile inputFile = context.fileSystem().inputFile(fp);
            SMInputCursor violation = inputCursor.childElementCursor("violation");
            collectFileIssues(violation,inputFile);
        }
    }

    private void collectFileIssues(final SMInputCursor line, final InputFile inputFile) throws XMLStreamException {
        while (null != line.getNext()) {
            NewIssueLocation dil = new DefaultIssueLocation()
                .on(inputFile)
                .at(inputFile.selectLine(Integer.valueOf(line.getAttrValue("beginline"))))
                .message(line.getElemStringValue());
            context.newIssue()
                .forRule(RuleKey.of(OCLintRulesDefinition.REPOSITORY_KEY, line.getAttrValue("rule")))
                .at(dil)
                .save();
        }
    }
}
