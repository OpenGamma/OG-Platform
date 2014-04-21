/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Arrays;

import com.opengamma.util.ArgumentChecker;

/**
 * Collects actions to manipulate data for a yield curve and adds them to a scenario.
 */
public class YieldCurveDataManipulatorBuilder {

  /** Selector whose selected items will be modified by the manipulators from this builder. */
  private final YieldCurveDataSelector _selector;

  /** The scenario to which manipulations are added. */
  private final Scenario _scenario;

  /* package */ YieldCurveDataManipulatorBuilder(YieldCurveDataSelector selector, Scenario scenario) {
    ArgumentChecker.notNull(selector, "selector");
    ArgumentChecker.notNull(scenario, "scenario");
    _selector = selector;
    _scenario = scenario;
  }

  /**
   * @return the configured selector
   */
  public YieldCurveDataSelector getSelector() {
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
   * @param shiftType Specifies how to apply the shift. A relative shift is expressed as an amount
   * to add or subtract, e.g. 10% shift = rate * 1.1, -20% shift = rate * 0.8
   * @param shift The size of the shift
   * @return This builder
   */
  public YieldCurveDataManipulatorBuilder parallelShift(ScenarioShiftType shiftType, Number shift) {
    _scenario.add(_selector, new YieldCurveDataParallelShift(shiftType, shift.doubleValue()));
    return this;
  }

  /**
   * Adds an action to perform a parallel shift to the scenario.
   * @param shift The size of the shift
   * @return This builder
   * @deprecated Use {@link #parallelShift(ScenarioShiftType, Number)}
   */
  @Deprecated
  public YieldCurveDataManipulatorBuilder parallelShift(Number shift) {
    _scenario.add(_selector, new YieldCurveDataParallelShift(ScenarioShiftType.ABSOLUTE, shift.doubleValue()));
    return this;
  }

  /**
   * Creates a bucketed shift builder with the given type
   * @param shiftType the type of the shift
   * @param shifts The shifts to apply to the curve data
   * @return the bucketed shift builder
   */
  public final YieldCurveDataManipulatorBuilder bucketedShifts(ScenarioShiftType shiftType, YieldCurveBucketedShift... shifts) {
    ArgumentChecker.notNull(shiftType, "shiftType");
    ArgumentChecker.notEmpty(shifts, "shifts");
    YieldCurveDataBucketedShiftManipulator manipulator =
        new YieldCurveDataBucketedShiftManipulator(shiftType, Arrays.asList(shifts));
    _scenario.add(_selector, manipulator);
    return this;
  }
  

  /**
   * Creates a point shift builder
   * @param shiftType the type of the shift
   * @param shifts The shifts to apply to the curve data
   * @return the point shifts builder
   */
  public final YieldCurveDataManipulatorBuilder pointShifts(ScenarioShiftType shiftType, YieldCurveDataPointShift... shifts) {
    ArgumentChecker.notNull(shiftType, "shiftType");
    ArgumentChecker.notEmpty(shifts, "shifts");
    YieldCurveDataPointShiftsManipulator manipulator =
        new YieldCurveDataPointShiftsManipulator(shiftType, Arrays.asList(shifts));
    _scenario.add(_selector, manipulator);
    return this;
  }
}
