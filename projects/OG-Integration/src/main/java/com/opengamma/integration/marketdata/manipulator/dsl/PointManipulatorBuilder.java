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
   */
  public PointManipulatorBuilder scaling(Number scalingFactor) {
    _scenario.add(_selector, new MarketDataScaling(scalingFactor.doubleValue()));
    return this;
  }

  /**
   * Adds an action to the scenario to apply an absolute shift to a value.
   * @param shift The shift amount
   * @return This builder
   * @deprecated Use {@link #shift(ScenarioShiftType, Number)}
   */
  @Deprecated
  public PointManipulatorBuilder shift(Number shift) {
    _scenario.add(_selector, new MarketDataShift(ScenarioShiftType.ABSOLUTE, shift.doubleValue()));
    return this;
  }

  /**
   * Shifts the value.
   * @param shiftType Whether the shift should be absolute or relative. A relative shift is expressed as an amount
   * to add or subtract, e.g. 10% shift = value * 1.1, -20% shift = value * 0.8
   * @param shift The amount of the shift
   * @return This builder
   */
  public PointManipulatorBuilder shift(ScenarioShiftType shiftType, Number shift) {
    _scenario.add(_selector, new MarketDataShift(shiftType, shift.doubleValue()));
    return this;
  }

  /**
   * Adds an action to the scenario to replace a market data point with a specified value.
   * @param value The replacement value
   * @return This builder
   */
  public PointManipulatorBuilder replace(Number value) {
    _scenario.add(_selector, new MarketDataReplace(value.doubleValue()));
    return this;
  }
}
