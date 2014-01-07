/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Builder for bucketed shifts 
 */
public class BucketedShiftManipulatorBuilder {
  
  /** Selector whose selected items will be modified by the manipulators from this builder. */
  private final YieldCurveSelector _selector;
  /** The scenario to which manipulations are added. */
  private final Scenario _scenario;

  private final BucketedShiftType _type;

  private final List<YieldCurveBucketedShift> _shiftList = Lists.newArrayList();

  BucketedShiftManipulatorBuilder(YieldCurveSelector selector, Scenario scenario, BucketedShiftType type) {
    _selector = selector;
    _scenario = scenario;
    _type = type;
  }
  
  
  /**
   * Apply a bucketed shift to a range
   * @param startYears start
   * @param endYears end
   * @param shift shift amount
   * @param shiftType shift type
   * @return this
   */
  public BucketedShiftManipulatorBuilder shift(Number startYears, Number endYears, Number shift, CurveShiftType shiftType) {
    YieldCurveBucketedShift bucketedShift = YieldCurveBucketedShift.create(startYears.doubleValue(), endYears.doubleValue(), shiftType, shift.doubleValue());
    _shiftList.add(bucketedShift);
    return this;
  }
  
  
  /**
   * Apply shifts to the scenario.
   * Should only be called once per {@link BucketedShiftManipulatorBuilder}.
   */
  public void apply() {
    YieldCurveBucketedShiftManipulator shifts = YieldCurveBucketedShiftManipulator.create(_type, ImmutableList.copyOf(_shiftList));
    _scenario.add(_selector, shifts);
  }
  
}
