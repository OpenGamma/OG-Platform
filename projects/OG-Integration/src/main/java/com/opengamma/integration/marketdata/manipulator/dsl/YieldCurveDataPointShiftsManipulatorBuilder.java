/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.List;

import org.threeten.bp.Period;

import com.google.common.collect.Lists;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class YieldCurveDataPointShiftsManipulatorBuilder {

  private final Scenario _scenario;
  private final YieldCurveDataSelector _selector;
  private final ScenarioShiftType _shiftType;

  private final List<YieldCurveDataPointShift> _shiftList = Lists.newArrayList();

  /* package */ YieldCurveDataPointShiftsManipulatorBuilder(YieldCurveDataSelector selector,
                                                            Scenario scenario,
                                                            ScenarioShiftType shiftType) {
    _selector = ArgumentChecker.notNull(selector, "selector");
    _scenario = ArgumentChecker.notNull(scenario, "scenario");
    _shiftType = ArgumentChecker.notNull(shiftType, "shiftType");
  }


  /**
   * Adds a shift to the builder.
   *
   * @param tenor The tenor of the point to shift
   * @param shift the shift to apply
   * @return this builder
   */
  public YieldCurveDataPointShiftsManipulatorBuilder shift(Period tenor, Number shift) {
    YieldCurveDataPointShift pointShift = new YieldCurveDataPointShift(tenor, shift.doubleValue());
    _shiftList.add(pointShift);
    return this;
  }

  /* package */ void build() {
    YieldCurveDataPointShiftsManipulator pointShifts = new YieldCurveDataPointShiftsManipulator(_shiftType, _shiftList);
    _scenario.add(_selector, pointShifts);
  }
}
