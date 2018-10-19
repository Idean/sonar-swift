package com.backelite.sonarqube.swift.complexity;

import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.batch.sensor.measure.Measure;

class LizardMeasure implements Measure<Integer> {

    private final Metric<Integer> m;
    private final Integer v;

    LizardMeasure(Metric<Integer> m, Integer v) {
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
