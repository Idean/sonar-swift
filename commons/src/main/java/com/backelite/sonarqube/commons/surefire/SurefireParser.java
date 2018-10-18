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
package com.backelite.sonarqube.commons.surefire;

import com.backelite.sonarqube.commons.MeasureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.CoreMetrics;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gillesgrousset on 28/08/2018.
 */
public class SurefireParser {
    protected static final Logger LOGGER = LoggerFactory.getLogger(SurefireParser.class);

    protected final SensorContext context;

    protected SurefireParser(SensorContext context) {
        this.context = context;
    }

    private void parseFiles(List<File> reports) {
        UnitTestIndex index = new UnitTestIndex();
        SurefireStaxHandler staxParser = new SurefireStaxHandler(index);
        StaxParser parser = new StaxParser(staxParser);
        for (File report : reports) {
            try {
                parser.parse(report);
            } catch (XMLStreamException e) {
                throw new IllegalStateException("Fail to parse the Surefire report: " + report, e);
            }
        }
        save(index);
    }

    public void collect(File reportsDir) {
        List<File> files = new ArrayList<>();
        if(reportsDir != null && reportsDir.isDirectory()){
            try {
                for (Path p : Files.newDirectoryStream(Paths.get(reportsDir.toURI()), name -> (name.startsWith("TEST") && name.endsWith(".xml")) || name.endsWith(".junit"))) {
                    files.add(p.toFile());
                }
            } catch (IOException ex){
                LOGGER.error( "Error while finding test files.", ex);
            }
        }

        if (!files.isEmpty()) {
            parseFiles(files);
        }
    }

    private void save(UnitTestIndex index) {
        long negativeTimeTestNumber = 0;
        int testsCount = 0;
        int testsSkipped = 0;
        int testsErrors = 0;
        int testsFailures = 0;
        long testsTime = 0;

        for (UnitTestClassReport report : index.getIndexByClassname().values()) {
            testsCount += report.getTests() - report.getSkipped();
            testsSkipped += report.getSkipped();
            testsErrors += report.getErrors();
            testsFailures += report.getFailures();

            if (report.getTests() > 0) {
                negativeTimeTestNumber += report.getNegativeTimeTestNumber();
            }
        }


        if (negativeTimeTestNumber > 0) {
            LOGGER.warn("There is {} test(s) reported with negative time by data, total duration may not be accurate.", negativeTimeTestNumber);
        }

        if (testsCount > 0) {
            InputComponent module = context.module();
            MeasureUtil.saveMeasure(context, module, CoreMetrics.TESTS, testsCount);
            MeasureUtil.saveMeasure(context, module, CoreMetrics.SKIPPED_TESTS, testsSkipped);
            MeasureUtil.saveMeasure(context, module, CoreMetrics.TEST_ERRORS, testsErrors);
            MeasureUtil.saveMeasure(context, module, CoreMetrics.TEST_FAILURES, testsFailures);
            MeasureUtil.saveMeasure(context, module, CoreMetrics.TEST_EXECUTION_TIME, testsTime);
        }
    }
}
