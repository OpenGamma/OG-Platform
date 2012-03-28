/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.PresentValueBlackCalculator;
import com.opengamma.financial.model.option.definition.YieldCurveWithBlackCubeBundle;

/**
 * 
 */
public class InterestRateFutureOptionBlackPresentValueFunction extends InterestRateFutureOptionBlackFunction {
  private static final PresentValueBlackCalculator s_calculator = PresentValueBlackCalculator.getInstance();

  public InterestRateFutureOptionBlackPresentValueFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative irFutureOption, final YieldCurveWithBlackCubeBundle data, final ValueSpecification spec) {
    final double pv = s_calculator.visit(irFutureOption, data);
    return Collections.singleton(new ComputedValue(spec, pv));
  }

}
