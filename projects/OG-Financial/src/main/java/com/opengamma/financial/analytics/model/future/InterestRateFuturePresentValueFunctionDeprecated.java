/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Present value function for interest rate futures.
 * @deprecated Use the version of the function that does not have funding and forward curve properties
 * @see InterestRateFuturePresentValueFunction
 */
@Deprecated
public class InterestRateFuturePresentValueFunctionDeprecated extends InterestRateFutureFunctionDeprecated {
  private static final PresentValueCalculator CALCULATOR = PresentValueCalculator.getInstance();

  @Override
  protected Set<ComputedValue> getResults(final InstrumentDerivative irFuture, final YieldCurveBundle data, final ValueSpecification spec) {
    final double presentValue = irFuture.accept(CALCULATOR, data);
    return Collections.singleton(new ComputedValue(spec, presentValue));
  }
}
