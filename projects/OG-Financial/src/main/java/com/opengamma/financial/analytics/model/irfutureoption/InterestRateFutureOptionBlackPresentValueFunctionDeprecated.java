/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * @deprecated Use the version of the function that does not refer to a funding or forward curve
 * @see InterestRateFutureOptionBlackPresentValueFunction
 */
@Deprecated
public class InterestRateFutureOptionBlackPresentValueFunctionDeprecated extends InterestRateFutureOptionBlackFunctionDeprecated {
  private static final PresentValueBlackCalculator CALCULATOR = PresentValueBlackCalculator.getInstance();

  public InterestRateFutureOptionBlackPresentValueFunctionDeprecated() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative irFutureOption, final YieldCurveWithBlackCubeBundle data, final ValueSpecification spec) {
    final double pv = irFutureOption.accept(CALCULATOR, data);
    return Collections.singleton(new ComputedValue(spec, pv));
  }

}
