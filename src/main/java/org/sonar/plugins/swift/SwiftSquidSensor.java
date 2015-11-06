/*
 * SonarQube Swift Plugin
 * Copyright (C) 2015 Backelite
 * dev@sonar.codehaus.org
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

import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.checks.AnnotationCheckFactory;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.PersistenceMode;
import org.sonar.api.measures.RangeDistributionBuilder;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.File;
import org.sonar.api.resources.InputFileUtils;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Violation;
import org.sonar.plugins.swift.lang.SwiftAstScanner;
import org.sonar.plugins.swift.lang.SwiftConfiguration;
import org.sonar.plugins.swift.lang.api.SwiftGrammar;
import org.sonar.plugins.swift.lang.api.SwiftMetric;
import org.sonar.plugins.swift.lang.checks.CheckList;
import org.sonar.plugins.swift.lang.core.Swift;
import org.sonar.squidbridge.AstScanner;
import org.sonar.squidbridge.api.CheckMessage;
import org.sonar.squidbridge.api.SourceCode;
import org.sonar.squidbridge.api.SourceFile;
import org.sonar.squidbridge.api.SourceFunction;
import org.sonar.squidbridge.checks.SquidCheck;
import org.sonar.squidbridge.indexer.QueryByParent;
import org.sonar.squidbridge.indexer.QueryByType;

import java.util.Collection;
import java.util.Locale;


public class SwiftSquidSensor implements Sensor {

    private final Number[] FUNCTIONS_DISTRIB_BOTTOM_LIMITS = {1, 2, 4, 6, 8, 10, 12, 20, 30};
    private final Number[] FILES_DISTRIB_BOTTOM_LIMITS = {0, 5, 10, 20, 30, 60, 90};

    private final AnnotationCheckFactory annotationCheckFactory;

    private Project project;
    private SensorContext context;
    private AstScanner<SwiftGrammar> scanner;

    public SwiftSquidSensor(RulesProfile profile) {

        this.annotationCheckFactory = AnnotationCheckFactory.create(profile, CheckList.REPOSITORY_KEY, CheckList.getChecks());
    }

    public boolean shouldExecuteOnProject(Project project) {

        return Swift.KEY.equals(project.getLanguageKey());
    }

    public void analyse(Project project, SensorContext context) {

        this.project = project;
        this.context = context;

        Collection<SquidCheck> squidChecks = annotationCheckFactory.getChecks();
        this.scanner = SwiftAstScanner.create(createConfiguration(project), squidChecks.toArray(new SquidCheck[squidChecks.size()]));
        scanner.scanFiles(InputFileUtils.toFiles(project.getFileSystem().mainFiles(Swift.KEY)));

        Collection<SourceCode> squidSourceFiles = scanner.getIndex().search(new QueryByType(SourceFile.class));
        save(squidSourceFiles);
    }

    private SwiftConfiguration createConfiguration(Project project) {

        return new SwiftConfiguration(project.getFileSystem().getSourceCharset());
    }

    private void save(Collection<SourceCode> squidSourceFiles) {

        for (SourceCode squidSourceFile : squidSourceFiles) {
            SourceFile squidFile = (SourceFile) squidSourceFile;

            File sonarFile = File.fromIOFile(new java.io.File(squidFile.getKey()), project);

            saveFilesComplexityDistribution(sonarFile, squidFile);
            saveFunctionsComplexityDistribution(sonarFile, squidFile);
            saveMeasures(sonarFile, squidFile);
            saveViolations(sonarFile, squidFile);
        }
    }

    private void saveMeasures(File sonarFile, SourceFile squidFile) {

        context.saveMeasure(sonarFile, CoreMetrics.FILES, squidFile.getDouble(SwiftMetric.FILES));
        context.saveMeasure(sonarFile, CoreMetrics.LINES, squidFile.getDouble(SwiftMetric.LINES));
        context.saveMeasure(sonarFile, CoreMetrics.NCLOC, squidFile.getDouble(SwiftMetric.LINES_OF_CODE));
        context.saveMeasure(sonarFile, CoreMetrics.FUNCTIONS, squidFile.getDouble(SwiftMetric.FUNCTIONS));
        context.saveMeasure(sonarFile, CoreMetrics.STATEMENTS, squidFile.getDouble(SwiftMetric.STATEMENTS));
        context.saveMeasure(sonarFile, CoreMetrics.COMPLEXITY, squidFile.getDouble(SwiftMetric.COMPLEXITY));
        context.saveMeasure(sonarFile, CoreMetrics.COMMENT_LINES, squidFile.getDouble(SwiftMetric.COMMENT_LINES));

    }

    private void saveFunctionsComplexityDistribution(File sonarFile, SourceFile squidFile) {

        Collection<SourceCode> squidFunctionsInFile = scanner.getIndex().search(new QueryByParent(squidFile), new QueryByType(SourceFunction.class));
        RangeDistributionBuilder complexityDistribution = new RangeDistributionBuilder(CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION, FUNCTIONS_DISTRIB_BOTTOM_LIMITS);
        for (SourceCode squidFunction : squidFunctionsInFile) {
            complexityDistribution.add(squidFunction.getDouble(SwiftMetric.COMPLEXITY));
        }
        context.saveMeasure(sonarFile, complexityDistribution.build().setPersistenceMode(PersistenceMode.MEMORY));
    }

    private void saveFilesComplexityDistribution(File sonarFile, SourceFile squidFile) {

        RangeDistributionBuilder complexityDistribution = new RangeDistributionBuilder(CoreMetrics.FILE_COMPLEXITY_DISTRIBUTION, FILES_DISTRIB_BOTTOM_LIMITS);
        complexityDistribution.add(squidFile.getDouble(SwiftMetric.COMPLEXITY));
        context.saveMeasure(sonarFile, complexityDistribution.build().setPersistenceMode(PersistenceMode.MEMORY));
    }

    private void saveViolations(File sonarFile, SourceFile squidFile) {

        Collection<CheckMessage> messages = squidFile.getCheckMessages();
        if (messages != null) {
            for (CheckMessage message : messages) {
                Violation violation = Violation.create(annotationCheckFactory.getActiveRule(message.getChecker()), sonarFile)
                        .setLineId(message.getLine())
                        .setMessage(message.getText(Locale.ENGLISH));
                context.saveViolation(violation);
            }
        }
    }

    @Override
    public String toString() {

        return getClass().getSimpleName();
    }
}
