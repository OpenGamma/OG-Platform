/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.List;

import org.threeten.bp.Period;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class YieldCurveDataBucketedShiftsManipulatorBuilder {

  /** Selector whose selected items will be modified by the manipulators from this builder. */
  private final YieldCurveDataSelector _selector;

  /** The scenario to which manipulations are added. */
  private final Scenario _scenario;

  /** The type of shift to apply. */
  private final ScenarioShiftType _shiftType;

  private final List<YieldCurveBucketedShift> _shiftList = Lists.newArrayList();

  public YieldCurveDataBucketedShiftsManipulatorBuilder(YieldCurveDataSelector selector,
                                                        Scenario scenario,
                                                        ScenarioShiftType shiftType) {
    _selector = ArgumentChecker.notNull(selector, "selector");
    _scenario = ArgumentChecker.notNull(scenario, "scenario");
    _shiftType = ArgumentChecker.notNull(shiftType, "shiftType");
  }

  /**
   * Apply a bucketed shift to a range
   * @param start Period between the valuation date and the start of the shift
   * @param end Period between the valuation date and the end of the shift
   * @param shift shift amount
   * @return this
   */
  public YieldCurveDataBucketedShiftsManipulatorBuilder shift(Period start, Period end, Number shift) {
    YieldCurveBucketedShift bucketedShift = new YieldCurveBucketedShift(start, end, shift.doubleValue());
    _shiftList.add(bucketedShift);
    return this;
  }

  /**
   * Apply shifts to the scenario.
   */
  public void build() {
    YieldCurveDataBucketedShiftManipulator shifts =
        new YieldCurveDataBucketedShiftManipulator(_shiftType, ImmutableList.copyOf(_shiftList));
    _scenario.add(_selector, shifts);
  }
}
