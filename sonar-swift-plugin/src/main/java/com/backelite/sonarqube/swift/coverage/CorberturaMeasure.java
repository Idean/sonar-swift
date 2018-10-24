/**
 * backelite-sonar-swift-plugin - Enables analysis of Swift and Objective-C projects into SonarQube.
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
package com.backelite.sonarqube.swift.coverage;

import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.batch.sensor.measure.Measure;

class CorberturaMeasure implements Measure<Integer> {

    private final Metric<Integer> m;
    private final Integer v;

    CorberturaMeasure(Metric<Integer> m, Integer v) {
        this.m = m;
        this.v = v;
    }

    @Override
    public InputComponent inputComponent() {
        return null;
    }

    @Override
    public Metric<Integer> metric() {
        return m;
    }

    @Override
    public Integer value() {
        return v;
    }
}
