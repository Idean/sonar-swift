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
package com.backelite.sonarqube.swift;

import com.backelite.sonarqube.commons.MeasureUtil;
import com.backelite.sonarqube.swift.lang.SwiftAstScanner;
import com.backelite.sonarqube.swift.lang.SwiftConfiguration;
import com.backelite.sonarqube.swift.lang.api.SwiftGrammar;
import com.backelite.sonarqube.swift.lang.api.SwiftMetric;
import com.backelite.sonarqube.swift.lang.checks.CheckList;
import com.backelite.sonarqube.swift.lang.core.Swift;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.batch.sensor.issue.internal.DefaultIssueLocation;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.scan.filesystem.PathResolver;
import org.sonar.squidbridge.AstScanner;
import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.squidbridge.api.CheckMessage;
import org.sonar.squidbridge.api.SourceCode;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.squidbridge.indexer.QueryByType;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;


public class SwiftSquidSensor implements Sensor {

    private final Number[] FUNCTIONS_DISTRIB_BOTTOM_LIMITS = {1, 2, 4, 6, 8, 10, 12, 20, 30};
    private final Number[] FILES_DISTRIB_BOTTOM_LIMITS = {0, 5, 10, 20, 30, 60, 90};

    private final PathResolver pathResolver;
    private final Checks<SquidCheck<SwiftGrammar>> checks;

    private SensorContext context;
    private AstScanner<SwiftGrammar> scanner;

    public SwiftSquidSensor(SensorContext context, PathResolver pathResolver, CheckFactory checkFactory) {
        this.context = context;
        this.pathResolver = pathResolver;
        this.checks = checkFactory.<SquidCheck<SwiftGrammar>>create(CheckList.REPOSITORY_KEY).addAnnotatedChecks((Iterable<Class>)CheckList.getChecks());
    }

    private SwiftConfiguration createConfiguration() {
        return new SwiftConfiguration(context.fileSystem().encoding());
    }

    private void save(Collection<SourceCode> squidSourceFiles) {
        for (SourceCode squidSourceFile : squidSourceFiles) {
            SourceFile squidFile = (SourceFile) squidSourceFile;
            String relativePath = pathResolver.relativePath(context.fileSystem().baseDir(), new File(squidFile.getKey()));
            InputFile inputFile = context.fileSystem().inputFile(context.fileSystem().predicates().hasRelativePath(relativePath));

            saveMeasures(inputFile, squidFile);
            saveIssues(inputFile, squidFile);
        }
    }

    private void saveMeasures(InputFile inputFile, SourceFile squidFile) {
        MeasureUtil.saveMeasure(context, inputFile, CoreMetrics.FILES, squidFile.getInt(SwiftMetric.FILES));
        MeasureUtil.saveMeasure(context, inputFile, CoreMetrics.LINES, squidFile.getInt(SwiftMetric.LINES));
        MeasureUtil.saveMeasure(context, inputFile, CoreMetrics.NCLOC, squidFile.getInt(SwiftMetric.LINES_OF_CODE));
        MeasureUtil.saveMeasure(context, inputFile, CoreMetrics.STATEMENTS, squidFile.getInt(SwiftMetric.STATEMENTS));
        MeasureUtil.saveMeasure(context, inputFile, CoreMetrics.COMMENT_LINES, squidFile.getInt(SwiftMetric.COMMENT_LINES));
    }

    private void saveIssues(InputFile inputFile, SourceFile squidFile) {
        Collection<CheckMessage> messages = squidFile.getCheckMessages();

        if (inputFile != null) {
            for (CheckMessage message : messages) {
                RuleKey ruleKey = checks.ruleKey((SquidCheck<SwiftGrammar>) message.getCheck());
                NewIssue issue = context.newIssue()
                    .forRule(ruleKey);
                NewIssueLocation dil = new DefaultIssueLocation()
                    .on(inputFile)
                    .at(inputFile.selectLine(message.getLine()))
                    .message(message.getText(Locale.ENGLISH));
                issue.at(dil);
                if (message.getCost() != null) {
                    issue.gap(message.getCost());
                }
                issue.save();
            }
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor
            .onlyOnLanguage(Swift.KEY)
            .name("Swift Squid")
            .onlyOnFileType(InputFile.Type.MAIN);
    }

    @Override
    public void execute(SensorContext sensorContext) {
        FilePredicate hasSwift = context.fileSystem().predicates().hasLanguage(Swift.KEY);
        FilePredicate isMain = context.fileSystem().predicates().hasType(InputFile.Type.MAIN);
        FilePredicate and = context.fileSystem().predicates().and(hasSwift, isMain);
        List<File> files = new ArrayList<>();
        for(InputFile inf : context.fileSystem().inputFiles(and)){
            files.add(inf.file());
        }

        List<SquidAstVisitor<SwiftGrammar>> visitors = Lists.newArrayList(checks.all());
        scanner = SwiftAstScanner.create(createConfiguration(), visitors.toArray(new SquidAstVisitor[visitors.size()]));
        scanner.scanFiles(ImmutableList.copyOf(files));

        Collection<SourceCode> squidSourceFiles = scanner.getIndex().search(new QueryByType(SourceFile.class));
        save(squidSourceFiles);
    }
}
