/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class PointManipulatorBuilder {

    private final PointSelector _selector;
    private final Scenario _scenario;

    public PointManipulatorBuilder(PointSelector selector, Scenario scenario) {
      ArgumentChecker.notNull(selector, "selector");
      ArgumentChecker.notNull(scenario, "scenario");
      _selector = selector;
      _scenario = scenario;
    }

    public PointManipulatorBuilder scaling(double scalingFactor) {
      _scenario.add(_selector, new Scaling(scalingFactor));
      return this;
    }
}
