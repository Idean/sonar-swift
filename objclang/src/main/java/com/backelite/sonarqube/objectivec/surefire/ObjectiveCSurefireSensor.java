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

import com.backelite.sonarqube.commons.surefire.BaseSurefireSensor;
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
import com.backelite.sonarqube.objectivec.lang.core.ObjectiveC;
import org.sonar.api.scan.filesystem.PathResolver;

import java.io.File;

public class ObjectiveCSurefireSensor extends BaseSurefireSensor {

    public ObjectiveCSurefireSensor(final FileSystem fileSystem, final PathResolver pathResolver, final Settings settings, final ResourcePerspectives resourcePerspectives) {
        super(fileSystem, pathResolver, resourcePerspectives, settings);
    }

    @Override
    public boolean shouldExecuteOnProject(Project project) {

        return project.isRoot() && fileSystem.hasFiles(fileSystem.predicates().hasLanguage(ObjectiveC.KEY));
    }

    @Override
    protected void collect(SensorContext context, File reportsDir) {
        LOGGER.info("parsing {}", reportsDir);
        new ObjectiveCSurefireParser(fileSystem, resourcePerspectives, context).collect(reportsDir);
    }

    @Override
    public String toString() {
        return "Objective-C Surefire Sensor";
    }


}