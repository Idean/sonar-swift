/*
 * SonarQube Swift Plugin
 * Copyright (C) 2015 Backelite
 * sonarqube@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.swift;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.core.metric.ScannerMetrics;
import org.sonar.plugins.swift.lang.SwiftAstScanner;
import org.sonar.plugins.swift.lang.SwiftConfiguration;
import org.sonar.plugins.swift.lang.api.SwiftGrammar;
import org.sonar.plugins.swift.lang.api.SwiftMetric;
import org.sonar.plugins.swift.lang.checks.CheckList;
import org.sonar.plugins.swift.lang.core.Swift;
import org.sonar.squidbridge.AstScanner;
import org.sonar.squidbridge.SquidAstVisitor;
import org.sonar.squidbridge.api.SourceCode;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.indexer.QueryByType;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class SwiftSquidSensor implements Sensor {

    private final Number[] FUNCTIONS_DISTRIB_BOTTOM_LIMITS = {1, 2, 4, 6, 8, 10, 12, 20, 30};
    private final Number[] FILES_DISTRIB_BOTTOM_LIMITS = {0, 5, 10, 20, 30, 60, 90};

    public void describe(SensorDescriptor sensorDescriptor) {
        //TODO: validate these entries
        sensorDescriptor
                .name("SwiftSensor")
                .onlyOnLanguage("swift");
    }

    public void execute(SensorContext sensorContext) {
        FileSystem fileSystem = sensorContext.fileSystem();
        FilePredicate mainFilePredicates = fileSystem.predicates().and(
                fileSystem.predicates().hasLanguage(Swift.KEY),
                fileSystem.predicates().hasType(InputFile.Type.MAIN));

        CheckFactory checkFactory = new CheckFactory(sensorContext.activeRules());
        Checks checks = checkFactory.create(CheckList.REPOSITORY_KEY).addAnnotatedChecks(CheckList.getChecks());

        List<SquidAstVisitor<SwiftGrammar>> visitors = Lists.<SquidAstVisitor<SwiftGrammar>>newArrayList(checks.all());
        SwiftConfiguration swiftConfiguration = new SwiftConfiguration(fileSystem.encoding());
        AstScanner<SwiftGrammar> scanner = SwiftAstScanner.create(swiftConfiguration, visitors.toArray(new SquidAstVisitor[visitors.size()]));
        scanner.scanFiles(ImmutableList.copyOf(fileSystem.files(mainFilePredicates)));

        Collection<SourceCode> squidSourceFiles = scanner.getIndex().search(new QueryByType(SourceFile.class));
        save(sensorContext, squidSourceFiles);
    }

    private void save(SensorContext sensorContext, Collection<SourceCode> squidSourceFiles) {
        for (SourceCode squidSourceFile : squidSourceFiles) {
            SourceFile squidFile = (SourceFile) squidSourceFile;

            FileSystem fileSystem = sensorContext.fileSystem();
            String relativePath = fileSystem.resolvePath(squidFile.getKey()).toString();
            InputFile inputFile = fileSystem.inputFile(fileSystem.predicates().hasRelativePath(relativePath));

            saveMeasures(sensorContext, inputFile, squidFile);
            saveIssues(inputFile, squidFile);
        }
    }

    private HashMap<String, Metric> batchMetrics() {
        Set<org.sonar.api.measures.Metric> scannerMetrics = new ScannerMetrics().getMetrics();
        HashMap<String, Metric> mappedMetrics = new HashMap<String, Metric>();
        for (final org.sonar.api.measures.Metric sMetric : scannerMetrics) {
            mappedMetrics.put(sMetric.key(), new Metric() {
                public String key() {
                    return sMetric.key();
                }

                public Class valueType() {
                    return sMetric.valueType();
                }
            });
        }
        return mappedMetrics;
    }

    private void saveMeasures(SensorContext sensorContext, InputFile inputFile, SourceFile squidFile) {
        HashMap<String, Metric> mappedMetrics = batchMetrics();

        sensorContext.newMeasure()
                .forMetric(mappedMetrics.get(CoreMetrics.FILES_KEY))
                .on(inputFile)
                .withValue(squidFile.getDouble(SwiftMetric.FILES))
                .save();
        sensorContext.newMeasure()
                .forMetric(mappedMetrics.get(CoreMetrics.LINES_KEY))
                .on(inputFile)
                .withValue(squidFile.getDouble(SwiftMetric.LINES))
                .save();
        sensorContext.newMeasure()
                .forMetric(mappedMetrics.get(CoreMetrics.NCLOC_KEY))
                .on(inputFile)
                .withValue(squidFile.getDouble(SwiftMetric.LINES_OF_CODE))
                .save();
        sensorContext.newMeasure()
                .forMetric(mappedMetrics.get(CoreMetrics.STATEMENTS_KEY))
                .on(inputFile)
                .withValue(squidFile.getDouble(SwiftMetric.STATEMENTS))
                .save();
        sensorContext.newMeasure()
                .forMetric(mappedMetrics.get(CoreMetrics.COMMENT_LINES_KEY))
                .on(inputFile)
                .withValue(squidFile.getDouble(SwiftMetric.COMMENT_LINES))
                .save();

        //context.saveMeasure(inputFile, CoreMetrics.FUNCTIONS, squidFile.getDouble(SwiftMetric.FUNCTIONS));
        //context.saveMeasure(inputFile, CoreMetrics.COMPLEXITY, squidFile.getDouble(SwiftMetric.COMPLEXITY));
    }

    private void saveIssues(InputFile inputFile, SourceFile squidFile) {
//        Collection<CheckMessage> messages = squidFile.getCheckMessages();
//
//        Resource resource = context.getResource(inputFile);
//
//        if (messages != null && resource != null) {
//            for (CheckMessage message : messages) {
//                RuleKey ruleKey = checks.ruleKey((SquidCheck<SwiftGrammar>) message.getCheck());
//                Issuable issuable = resourcePerspectives.as(Issuable.class, resource);
//
//                if (issuable != null) {
//                    Issuable.IssueBuilder issueBuilder = issuable.newIssueBuilder()
//                            .ruleKey(ruleKey)
//                            .line(message.getLine())
//                            .message(message.getText(Locale.ENGLISH));
//
//                    if (message.getCost() != null) {
//                        issueBuilder.effortToFix(message.getCost());
//                    }
//
//                    issuable.addIssue(issueBuilder.build());
//                }
//            }
//        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
