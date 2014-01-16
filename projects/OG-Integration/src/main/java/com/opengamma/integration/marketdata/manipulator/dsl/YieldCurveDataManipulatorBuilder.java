/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * Collects actions to manipulate data for a yield curve and adds them to a scenario.
 */
public class YieldCurveDataManipulatorBuilder {

  /** Selector whose selected items will be modified by the manipulators from this builder. */
  // TODO this needs to be a YieldCurveDataSelector
  private final YieldCurveSelector _selector;
  /** The scenario to which manipulations are added. */
  private final Scenario _scenario;

  /* package */ YieldCurveDataManipulatorBuilder(YieldCurveSelector selector, Scenario scenario) {
    ArgumentChecker.notNull(selector, "selector");
    ArgumentChecker.notNull(scenario, "scenario");
    _selector = selector;
    _scenario = scenario;
  }

  /**
   * @return the configured selector
   */
  public YieldCurveSelector getSelector() {
    return _selector;
  }

  /**
   * @return the configured scenario
   */
  public Scenario getScenario() {
    return _scenario;
  }

  /**
   * Adds an action to perform a parallel shift to the scenario.
   * @param shift The size of the shift
   * @return This builder
   */
  public YieldCurveDataManipulatorBuilder parallelShift(Number shift) {
    _scenario.add(_selector, new YieldCurveDataParallelShift(shift.doubleValue()));
    return this;
  }

  /**
   * Shifts the curve using {@link YieldAndDiscountCurve#withSingleShift}
   * @param t The time.
   * @param shift The shift amount.
   * @return This builder
   * TODO can this be replaced with a point shift with one point?
   */
  public YieldCurveDataManipulatorBuilder singleShift(Number t, Number shift) {
    _scenario.add(_selector, new YieldCurveSingleShift(t.doubleValue(), shift.doubleValue()));
    return this;
  }

  
  /**
   * Creates a bucketed shift builder with the given type
   * @param type the type of the shift
   * @return the bucketed shift builder
   */
  public final BucketedShiftManipulatorBuilder bucketedShifts(/*BucketedShiftType type*/) {
    return new BucketedShiftManipulatorBuilder(_selector, _scenario/*, type*/);
  }
  

  /**
   * Creates a point shift builder
   * @return the point shifts builder
   */
  public final PointShiftManipulatorBuilder pointShifts() {
    return new PointShiftManipulatorBuilder(_selector, _scenario);
  }
  
  
}
