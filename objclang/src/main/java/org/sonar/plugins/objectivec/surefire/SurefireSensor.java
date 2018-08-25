/**
 * Objective-C Language - Enables analysis of Swift and Objective-C projects into SonarQube.
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
package org.sonar.plugins.objectivec.surefire;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.CoverageExtension;
import org.sonar.api.batch.DependsUpon;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.plugins.objectivec.core.ObjectiveC;

import java.io.File;

public class SurefireSensor implements Sensor {

    private static final Logger LOG = LoggerFactory.getLogger(SurefireSensor.class);
    public static final String REPORT_PATH_KEY = "sonar.junit.reportsPath";
    public static final String DEFAULT_REPORT_PATH = "sonar-reports/";

    private final Settings settings;
    private final FileSystem fileSystem;
    private final ResourcePerspectives resourcePerspectives;

    public SurefireSensor(final FileSystem fileSystem, final Settings config, final ResourcePerspectives resourcePerspectives) {
        this.settings = config;
        this.fileSystem = fileSystem;
        this.resourcePerspectives = resourcePerspectives;
    }

    @DependsUpon
    public Class<?> dependsUponCoverageSensors() {
        return CoverageExtension.class;
    }

    public boolean shouldExecuteOnProject(Project project) {

        return project.isRoot() && fileSystem.hasFiles(fileSystem.predicates().hasLanguage(ObjectiveC.KEY));
    }

    public void analyse(Project project, SensorContext context) {

    /*
        GitHub Issue #50
        Formerly we used SurefireUtils.getReportsDirectory(project). It seems that is this one:
        http://grepcode.com/file/repo1.maven.org/maven2/org.codehaus.sonar.plugins/sonar-surefire-plugin/3.3.2/org/sonar/plugins/surefire/api/SurefireUtils.java?av=f#34
        However it turns out that the Java plugin contains its own version of SurefireUtils
        that is very different (and does not contain a matching method).
        That seems to be this one: http://svn.codehaus.org/sonar-plugins/tags/sonar-groovy-plugin-0.5/src/main/java/org/sonar/plugins/groovy/surefire/SurefireSensor.java

        The result is as follows:

        1.  At runtime getReportsDirectory(project) fails if you have the Java plugin installed
        2.  At build time the new getReportsDirectory(project,settings) because I guess something in the build chain doesn't know about the Java plugin version

        So the implementation here reaches into the project properties and pulls the path out by itself.
     */

        collect(project, context, new File(reportPath()));
    }

    protected void collect(Project project, SensorContext context, File reportsDir) {
        LOG.info("parsing {}", reportsDir);
        SurefireParser parser = new SurefireParser(project, fileSystem, resourcePerspectives, context);
        parser.collect(reportsDir);
    }

    @Override
    public String toString() {
        return "Objective-C SurefireSensor";
    }

    private String reportPath() {
        String reportPath = settings.getString(REPORT_PATH_KEY);
        if (reportPath == null) {
            reportPath = DEFAULT_REPORT_PATH;
        }
        return reportPath;
    }

}