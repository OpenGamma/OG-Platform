/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.List;

import org.threeten.bp.Period;

import com.google.common.collect.Lists;
import com.opengamma.util.ArgumentChecker;

/**
 * Builder for point shift manipulators
 */
public class PointShiftManipulatorBuilder {

  private final Scenario _scenario;
  private final YieldCurveSelector _selector;
  private final ScenarioShiftType _shiftType;
  
  private final List<YieldCurvePointShift> _shiftList = Lists.newArrayList();
  
  /* package */ PointShiftManipulatorBuilder(YieldCurveSelector selector, Scenario scenario, ScenarioShiftType shiftType) {
    _selector = ArgumentChecker.notNull(selector, "selector");
    _scenario = ArgumentChecker.notNull(scenario, "scenario");
    _shiftType = ArgumentChecker.notNull(shiftType, "shiftType");
  }


  /**
   * Adds a shift to the builder.
   * @param tenor The tenor of the point to shift
   * @param shift the shift to apply
   * @return this builder
   */
  public PointShiftManipulatorBuilder shift(Period tenor, Number shift) {
    YieldCurvePointShift pointShift = new YieldCurvePointShift(tenor, shift.doubleValue());
    _shiftList.add(pointShift);
    return this;
  }
  
  
  /**
   * Adds the configured shifts to the scenario.
   * Should only be called once per {@link PointShiftManipulatorBuilder}.
   */
  public void build() {
    YieldCurvePointShiftManipulator pointShifts = new YieldCurvePointShiftManipulator(_shiftType, _shiftList);
    _scenario.add(_selector, pointShifts);
  }
  
}
