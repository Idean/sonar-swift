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
package com.backelite.sonarqube.commons;

import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.Metric;

/**
 * Created by gillesgrousset on 29/08/2018.
 */
public final class MeasureUtil {

    public static void saveMeasure(SensorContext context, InputComponent component, Metric<Integer> metric, int value) {
        context.<Integer>newMeasure()
                .on(component)
                .forMetric(metric)
                .withValue(value)
                .save();
    }

    public static void saveMeasure(SensorContext context, InputComponent component, Metric<Long> metric, long value) {
        context.<Long>newMeasure()
                .on(component)
                .forMetric(metric)
                .withValue(value)
                .save();
    }

    public static void saveMeasure(SensorContext context, InputComponent component, Metric<Double> metric, double value) {
        context.<Double>newMeasure()
                .on(component)
                .forMetric(metric)
                .withValue(value)
                .save();
    }
}
