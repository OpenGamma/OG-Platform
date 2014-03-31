/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.util.ArgumentChecker;

/**
 * Builder for point shift manipulators for fitted yield curves.
 */
public class YieldCurvePointShiftManipulatorBuilder {

  private final Scenario _scenario;
  private final YieldCurveSelector _selector;
  private final ScenarioShiftType _shiftType;
  
  private final List<YieldCurvePointShift> _shiftList = Lists.newArrayList();
  
  /* package */ YieldCurvePointShiftManipulatorBuilder(YieldCurveSelector selector,
                                                       Scenario scenario,
                                                       ScenarioShiftType shiftType) {
    _selector = ArgumentChecker.notNull(selector, "selector");
    _scenario = ArgumentChecker.notNull(scenario, "scenario");
    _shiftType = ArgumentChecker.notNull(shiftType, "shiftType");
  }


  /**
   * Adds a shift to the builder.
   * @param pointIndex the index of the curve point to shift
   * @param shift the shift to apply
   * @return this builder
   */
  public YieldCurvePointShiftManipulatorBuilder shift(int pointIndex, Number shift) {
    YieldCurvePointShift pointShift = new YieldCurvePointShift(pointIndex, shift.doubleValue());
    _shiftList.add(pointShift);
    return this;
  }
  
  
  /**
   * Adds the configured shifts to the scenario.
   * Should only be called once per {@link YieldCurvePointShiftManipulatorBuilder}.
   */
  public void build() {
    YieldCurvePointShiftManipulator pointShifts = new YieldCurvePointShiftManipulator(_shiftType, _shiftList);
    _scenario.add(_selector, pointShifts);
  }
  
}
