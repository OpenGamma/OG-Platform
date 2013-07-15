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
   * Adds an action to scale the raw value.
   * @param scalingFactor The scaling factor
   * @return This builder
   * TODO should this be named multiplicativeShift?
   */
  public PointManipulatorBuilder scaling(double scalingFactor) {
    _scenario.add(_selector, new Scaling(scalingFactor));
    return this;
  }

  // TODO method / manipulator that does an additive shift (just called shift?)

  // TODO method / manipulator that replaces the input with a specified value (called value? replace? replacement?)
}
