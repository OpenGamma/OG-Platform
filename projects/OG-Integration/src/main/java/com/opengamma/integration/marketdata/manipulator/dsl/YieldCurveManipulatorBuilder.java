/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * Collects actions to manipulate a curve and adds them to a scenario.
 */
public class YieldCurveManipulatorBuilder {

  /** Selector whose selected items will be modified by the manipulators from this builder. */
  private final YieldCurveSelector _selector;
  /** The scenario to which manipulations are added. */
  private final Scenario _scenario;

  /* package */ YieldCurveManipulatorBuilder(YieldCurveSelector selector, Scenario scenario) {
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
  public YieldCurveManipulatorBuilder parallelShift(Number shift) {
    _scenario.add(_selector, new YieldCurveParallelShift(shift.doubleValue()));
    return this;
  }

  /**
   * Shifts the curve using {@link YieldAndDiscountCurve#withSingleShift}
   * @param t The time.
   * @param shift The shift amount.
   * @return This builder
   */
  public YieldCurveManipulatorBuilder singleShift(Number t, Number shift) {
    _scenario.add(_selector, new YieldCurveSingleShift(t.doubleValue(), shift.doubleValue()));
    return this;
  }

  // TODO what other manipulations do we need to support?
}
