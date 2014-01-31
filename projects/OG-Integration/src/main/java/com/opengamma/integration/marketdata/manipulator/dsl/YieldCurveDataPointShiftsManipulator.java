/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.YieldCurveData;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class YieldCurveDataPointShiftsManipulator implements StructureManipulator<YieldCurveData> {

  private final ScenarioShiftType _shiftType;
  private final List<YieldCurvePointShift> _shiftList;

  public YieldCurveDataPointShiftsManipulator(ScenarioShiftType shiftType, List<YieldCurvePointShift> shiftList) {
    _shiftType = ArgumentChecker.notNull(shiftType, "shiftType");
    _shiftList = ImmutableList.copyOf(ArgumentChecker.notEmpty(shiftList, "shiftList"));
  }

  @Override
  public YieldCurveData execute(YieldCurveData structure,
                                ValueSpecification valueSpecification,
                                FunctionExecutionContext executionContext) {
    // TODO implement execute()
    throw new UnsupportedOperationException("execute not implemented");
  }

  @Override
  public Class<YieldCurveData> getExpectedType() {
    // TODO implement getExpectedType()
    throw new UnsupportedOperationException("getExpectedType not implemented");
  }
}
