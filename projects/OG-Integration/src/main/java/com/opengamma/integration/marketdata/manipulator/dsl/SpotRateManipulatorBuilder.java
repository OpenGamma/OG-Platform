/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class SpotRateManipulatorBuilder {

  private final Scenario _scenario;
  private final SpotRateSelector _selector;

  public SpotRateManipulatorBuilder(Scenario scenario, SpotRateSelector selector) {
    _scenario = ArgumentChecker.notNull(scenario, "scenario");
    _selector = ArgumentChecker.notNull(selector, "selector");
  }

  public SpotRateManipulatorBuilder shift(ScenarioShiftType shiftType, Number shiftAmount) {
    _scenario.add(_selector, new SpotRateShift(shiftType, shiftAmount, _selector.getCurrencyPairs()));
    return this;
  }

  public SpotRateManipulatorBuilder scaling(Number scalingFactor) {
    _scenario.add(_selector, new SpotRateScaling(scalingFactor, _selector.getCurrencyPairs()));
    return this;
  }

  public SpotRateManipulatorBuilder replace(Number value) {
    _scenario.add(_selector, new SpotRateReplace(value));
    return this;
  }
}
