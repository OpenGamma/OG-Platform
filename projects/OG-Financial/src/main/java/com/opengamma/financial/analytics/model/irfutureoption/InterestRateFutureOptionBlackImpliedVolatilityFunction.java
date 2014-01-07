/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.ImpliedVolatilityBlackCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.black.BlackDiscountingImpliedVolatilityIRFutureOptionFunction;

/**
 * Interpolates, for InterestRateFutureOptions using Black model, and returns the implied volatility required.
 * @deprecated Use {@link BlackDiscountingImpliedVolatilityIRFutureOptionFunction}
 */
@Deprecated
public class InterestRateFutureOptionBlackImpliedVolatilityFunction extends InterestRateFutureOptionBlackFunction {
  /** The implied volatility calculator */
  private static final ImpliedVolatilityBlackCalculator CALCULATOR = ImpliedVolatilityBlackCalculator.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#IMPLIED_VOLATILITY}
   */
  public InterestRateFutureOptionBlackImpliedVolatilityFunction() {
    super(ValueRequirementNames.IMPLIED_VOLATILITY, false);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative irFutureOption, final YieldCurveWithBlackCubeBundle data, final ValueSpecification spec,
      final Set<ValueRequirement> desiredValues) {
    final Double impliedVol = irFutureOption.accept(CALCULATOR, data);
    return Collections.singleton(new ComputedValue(spec, impliedVol));
  }

}
