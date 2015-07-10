/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.UnderlyingMarketPriceCalculator;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionPremiumTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureSecurityDiscountingMethod;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.black.BlackDiscountingForwardIRFutureOptionFunction;

/**
 * Calls into {@link InterestRateFutureSecurityDiscountingMethod} to compute forward used in BlackFunctions. No convexity is applied, so this may be used to compare to
 * {@link ValueRequirementNames#UNDERLYING_MARKET_PRICE} computed in {@link InterestRateFutureOptionMarketUnderlyingPriceFunction}
 *
 * @deprecated Use {@link BlackDiscountingForwardIRFutureOptionFunction}
 */
@Deprecated
public class InterestRateFutureOptionBlackForwardFunction extends InterestRateFutureOptionBlackFunction {

  /** The calculator to compute the delta value */
  private static final UnderlyingMarketPriceCalculator CALCULATOR = UnderlyingMarketPriceCalculator.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#FORWARD}
   */
  public InterestRateFutureOptionBlackForwardFunction() {
    super(ValueRequirementNames.FORWARD, false);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative irFutureOptionTransaction, final YieldCurveWithBlackCubeBundle curveBundle, final ValueSpecification spec,
      final Set<ValueRequirement> desiredValues) {
    if (irFutureOptionTransaction instanceof InterestRateFutureOptionMarginTransaction) {
      final InstrumentDerivative irFutureOptionSecurity = ((InterestRateFutureOptionMarginTransaction) irFutureOptionTransaction).getUnderlyingSecurity();
      final double forward = irFutureOptionSecurity.accept(CALCULATOR, curveBundle);
      return Collections.singleton(new ComputedValue(spec, forward));
    } else if (irFutureOptionTransaction instanceof InterestRateFutureOptionPremiumTransaction) {
      final InstrumentDerivative irFutureOptionSecurity = ((InterestRateFutureOptionPremiumTransaction) irFutureOptionTransaction).getUnderlyingSecurity();
      final double forward = irFutureOptionSecurity.accept(CALCULATOR, curveBundle);
      return Collections.singleton(new ComputedValue(spec, forward));
    }
    throw new OpenGammaRuntimeException("Could not handle instrument of type " + irFutureOptionTransaction.getClass());
  }

}
