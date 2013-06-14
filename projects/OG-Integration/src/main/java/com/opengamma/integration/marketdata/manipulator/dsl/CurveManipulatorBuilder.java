/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import com.opengamma.util.ArgumentChecker;

/**
 * Collects actions to manipulate a curve and adds them to a scenario.
 */
public class CurveManipulatorBuilder {

  /** Selector whose selected items will be modified by the manipulators from this builder. */
  private final CurveSelector _selector;
  /** The scenario to which manipulations are added. */
  private final Scenario _scenario;

  /* package */ CurveManipulatorBuilder(CurveSelector selector, Scenario scenario) {
    ArgumentChecker.notNull(selector, "selector");
    ArgumentChecker.notNull(scenario, "scenario");
    _selector = selector;
    _scenario = scenario;
  }

  /**
   * Adds an action to perform a parallel shift to the scenario.
   * @param shift The size of the shift
   * @return This builder
   */
  public CurveManipulatorBuilder parallelShift(double shift) {
    _scenario.add(_selector, new ParallelShift(shift));
    return this;
  }
}
