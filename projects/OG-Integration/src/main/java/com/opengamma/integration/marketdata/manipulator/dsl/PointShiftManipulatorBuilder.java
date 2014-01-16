/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Builder for point shift manipulators
 */
public class PointShiftManipulatorBuilder {

  private final Scenario _scenario;
  private final YieldCurveSelector _selector;
  
  private final List<YieldCurvePointShift> _shiftList = Lists.newArrayList();
  
  /**
   * Package private
   */
  PointShiftManipulatorBuilder(YieldCurveSelector selector, Scenario scenario) {
    _selector = selector;
    _scenario = scenario;
  }


  /**
   * Adds a shift to the builder.
   * @param year the year (1.0 == 1 year)
   * @param shift the shift to apply
   * @param shiftType the type of shift
   * @return this builder
   */
  public PointShiftManipulatorBuilder shift(Number year, Number shift, CurveShiftType shiftType) {
    YieldCurvePointShift pointShift = YieldCurvePointShift.create(year.doubleValue(), shift.doubleValue(), shiftType);
    _shiftList.add(pointShift);
    return this;
  }
  
  
  /**
   * Adds the configured shifts to the scenario.
   * Should only be called once per {@link PointShiftManipulatorBuilder}.
   * TODO can this be got rid of?
   */
  public void apply() {
    YieldCurvePointShiftManipulator pointShifts = YieldCurvePointShiftManipulator.create(_shiftList);
    _scenario.add(_selector, pointShifts);
  }
  
}
