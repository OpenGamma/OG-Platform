/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;

/**
 * 
 * @param <T> The type of the result returned
 */
public interface PDELocalVolatilityCalculator<T> {

  T getResult(final LocalVolatilitySurfaceMoneyness localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option, final YieldAndDiscountCurve discountingCurve);

  T getResult(final LocalVolatilitySurfaceStrike localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option, final YieldAndDiscountCurve discountingCurve);
}
