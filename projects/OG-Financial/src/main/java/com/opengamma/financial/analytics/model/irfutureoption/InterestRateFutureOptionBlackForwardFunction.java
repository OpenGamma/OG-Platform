/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.UnderlyingMarketPriceCalculator;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginTransaction;
import com.opengamma.analytics.financial.interestrate.future.method.InterestRateFutureSecurityDiscountingMethod;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.util.ArgumentChecker;

/**
 * Calls into {@link InterestRateFutureSecurityDiscountingMethod} to compute forward used in BlackFunctions.
 * No convexity is applied, so this may be used to compare to {@link ValueRequirementNames#UNDERLYING_MARKET_PRICE}
 * computed in {@link InterestRateFutureOptionMarketUnderlyingPriceFunction}
 */
public class InterestRateFutureOptionBlackForwardFunction extends InterestRateFutureOptionBlackFunction {

  /** The calculator to compute the delta value */
  private static final UnderlyingMarketPriceCalculator CALCULATOR = UnderlyingMarketPriceCalculator.getInstance();
  
  public InterestRateFutureOptionBlackForwardFunction() {
    super(ValueRequirementNames.FORWARD);
  }

  @Override
  protected Set<ComputedValue> getResult(InstrumentDerivative irFutureOptionTransaction, YieldCurveWithBlackCubeBundle curveBundle, ValueSpecification spec) {
    ArgumentChecker.isTrue(irFutureOptionTransaction instanceof InterestRateFutureOptionMarginTransaction, 
        "InterestRateFutureOptionMarginTransaction expected. " + irFutureOptionTransaction.getClass().toString() + " found.");
    InstrumentDerivative irFutureOptionSecurity = ((InterestRateFutureOptionMarginTransaction) irFutureOptionTransaction).getUnderlyingOption();
    final double forward = irFutureOptionSecurity.accept(CALCULATOR, curveBundle);
    return Collections.singleton(new ComputedValue(spec, forward));
  }
  
  @Override
  /** Removed CURRENCY from properties */
  protected ValueProperties getResultProperties(final String currency) {
    return createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_METHOD)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .withAny(ValuePropertyNames.SURFACE)
        .get();
  }

  @Override
  /** Removed CURRENCY from properties */
  protected ValueProperties getResultProperties(final String currency, final String curveCalculationConfig, final String surfaceName) {
    return createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.BLACK_METHOD)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .get();
  }

}
