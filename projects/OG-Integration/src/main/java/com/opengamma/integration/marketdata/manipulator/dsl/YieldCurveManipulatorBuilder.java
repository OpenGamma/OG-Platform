/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
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

  /**
   * Performs a bucketed shift on the curve.
   * @param bucketedShiftType type of bucketed shift
   * @param shiftSpecs the specs for each shift
   * @return This builder
   */
  @SafeVarargs
  public final YieldCurveManipulatorBuilder bucketedShifts(GroovyAliasable bucketedShiftType, List<Object>... shiftSpecs) {
    
    List<YieldCurveBucketedShift> shiftList = Lists.newArrayList();
    for (int i = 0; i < shiftSpecs.length; i++) {
      
      List<Object> shiftSpec = shiftSpecs[i];
      
      ArgumentChecker.isTrue(shiftSpec.size() == 4, "Shift spec {} should have 4 elements, found {}. Spec: {}", i, shiftSpec.size(), shiftSpec);
      
      Number startYears = isInstance(Number.class, shiftSpec.get(0), "startYears should be a number (arg 0 in shift spec {}). Found: {}", i, shiftSpec.get(0));
      Number endYears = isInstance(Number.class, shiftSpec.get(1), "endYears should be a number (arg 1 in shift spec {}).  Found: {}", i, shiftSpec.get(1));
      Number shift = isInstance(Number.class, shiftSpec.get(2), "shift should be a number (arg 2 in shift spec {}).  Found: {}", i, shiftSpec.get(2));
      CurveShiftType shiftType = isInstance(CurveShiftType.class, shiftSpec.get(3), "shift should be a shift type (arg 3 in shift spec {}).  Found: {}", i, shiftSpec.get(3));
      
      YieldCurveBucketedShift bucketedShift = YieldCurveBucketedShift.create(startYears.doubleValue(), endYears.doubleValue(), shiftType, shift.doubleValue());
      
      shiftList.add(bucketedShift);
      
    }
    
    YieldCurveBucketedShiftManipulator shifts = YieldCurveBucketedShiftManipulator.create(bucketedShiftType, ImmutableList.copyOf(shiftList));
    
    _scenario.add(_selector, shifts);
    
    return this;
  }

  private static <T> T isInstance(Class<T> clazz, Object obj, String message, Object... args) {
    ArgumentChecker.isTrue(clazz.isInstance(obj), message, args);
    return clazz.cast(obj);
  }
  
  /**
   * Performs a point shift on the curve.
   * @param pointShiftSpecs the specs for each shift
   * @return This builder
   */
  @SafeVarargs
  public final YieldCurveManipulatorBuilder pointShifts(List<Object>... pointShiftSpecs) {
    
    List<YieldCurvePointShift> shiftList = Lists.newArrayList();
    
    for (int i = 0; i < pointShiftSpecs.length; i++) {
      
      List<Object> shiftSpec = pointShiftSpecs[i];
      
      ArgumentChecker.isTrue(shiftSpec.size() == 3, "Shift spec {} should have 3 elements, found {}. Spec: {}", i, shiftSpec.size(), shiftSpec);
      
      Number year = isInstance(Number.class, shiftSpec.get(0), "year should be a number (arg 0 in shift spec {}). Found: {}", i, shiftSpec.get(0));
      Number shift = isInstance(Number.class, shiftSpec.get(1), "shift should be a number (arg 1 in shift spec {}).  Found: {}", i, shiftSpec.get(1));
      CurveShiftType shiftType = isInstance(CurveShiftType.class, shiftSpec.get(2), "shift should be a shift type (arg 2 in shift spec {}).  Found: {}", i, shiftSpec.get(2));
      
      YieldCurvePointShift pointShift = YieldCurvePointShift.create(year.doubleValue(), shift.doubleValue(), shiftType);
      shiftList.add(pointShift);
    }
    
    YieldCurvePointShiftManipulator pointShifts = YieldCurvePointShiftManipulator.create(shiftList);
    
    _scenario.add(_selector, pointShifts);
    return this;
  }

}
