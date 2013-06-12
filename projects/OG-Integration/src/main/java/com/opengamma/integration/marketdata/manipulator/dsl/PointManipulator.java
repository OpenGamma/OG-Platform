/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class PointManipulator implements StructureManipulator<Double> {

  private final double _scalingFactor;

  /* package */ PointManipulator(double scalingFactor) {
    ArgumentChecker.notNull(scalingFactor, "scalingFactor");
    if (Double.isInfinite(scalingFactor) || Double.isNaN(scalingFactor)) {
      throw new IllegalArgumentException("scalingFactor must not be infinite or NaN. value=" + scalingFactor);
    }
    _scalingFactor = scalingFactor;
  }

  @Override
  public Double execute(Double structure) {
    return structure == null ? null : structure * _scalingFactor;
  }

  public static class Builder {

    private final PointSelector _selector;
    private final Scenario _scenario;

    private Double _scalingFactor;

    public Builder(PointSelector selector, Scenario scenario) {
      ArgumentChecker.notNull(selector, "selector");
      ArgumentChecker.notNull(scenario, "scenario");
      _selector = selector;
      _scenario = scenario;
    }

    public Builder scaling(double scalingFactor) {
      if (_scalingFactor != null) {
        throw new IllegalStateException("scalingFactor already set");
      }
      _scalingFactor = scalingFactor;
      return this;
    }

    public void execute() {
      PointManipulator pointManipulator = new PointManipulator(_scalingFactor);
      List<StructureManipulator<Double>> manipulators = Lists.<StructureManipulator<Double>>newArrayList(pointManipulator);
      _scenario.add(_selector, new CompositeStructureManipulator<>(manipulators));
    }
  }
}
