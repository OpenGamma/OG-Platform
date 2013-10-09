/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import com.opengamma.util.ArgumentChecker;

/**
 * Collects actions to manipulate raw market data points and adds them to a scenario.
 */
public class PointManipulatorBuilder {

  /** Selects the points to which the manipulations are applied. */
  private final PointSelector _selector;
  /** The scenario to which the manipulators are added */
  private final Scenario _scenario;

  /* package */ PointManipulatorBuilder(Scenario scenario, PointSelector selector) {
    ArgumentChecker.notNull(selector, "selector");
    ArgumentChecker.notNull(scenario, "scenario");
    _selector = selector;
    _scenario = scenario;
  }

  /**
   * Adds an action to the scenario to scale the raw value.
   * @param scalingFactor The scaling factor
   * @return This builder
   * TODO should this be named multiplicativeShift?
   */
  public PointManipulatorBuilder scaling(Number scalingFactor) {
    _scenario.add(_selector, new Scaling(scalingFactor.doubleValue()));
    return this;
  }

  /**
   * Adds an action to the scenario to apply an absolute shift to a value.
   * @param shift The shift amount
   * @return This builder
   */
  public PointManipulatorBuilder shift(Number shift) {
    _scenario.add(_selector, new Shift(shift.doubleValue()));
    return this;
  }

  /**
   * Adds an action to the scenario to replace a market data point with a specified value.
   * @param value The replacement value
   * @return This builder
   */
  public PointManipulatorBuilder replace(Number value) {
    _scenario.add(_selector, new Replace(value.doubleValue()));
    return this;
  }
}
