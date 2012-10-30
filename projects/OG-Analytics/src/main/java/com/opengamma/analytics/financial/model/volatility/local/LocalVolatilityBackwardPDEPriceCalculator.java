/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class LocalVolatilityBackwardPDEPriceCalculator implements PDELocalVolatilityCalculator<Double> {
  private final LocalVolatilityBackwardPDEPriceGridCalculator _priceCalculator;
  private final Interpolator1D _interpolator;

  public LocalVolatilityBackwardPDEPriceCalculator(final LocalVolatilityBackwardPDEPriceGridCalculator priceCalculator, final Interpolator1D interpolator) {
    _priceCalculator = priceCalculator;
    _interpolator = new NearestNPointsInterpolator(interpolator, 4);
  }

  @Override
  public Double getResult(final LocalVolatilitySurfaceMoneyness localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option, final YieldAndDiscountCurve discountingCurve) {
    final double expiry = option.getTimeToExpiry();
    final double forward = forwardCurve.getForward(expiry);
    final Interpolator1DDataBundle data = _priceCalculator.getResult(localVolatility, forwardCurve, option, discountingCurve);
    return _interpolator.interpolate(data, forward);
  }

  @Override
  public Double getResult(final LocalVolatilitySurfaceStrike localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option, final YieldAndDiscountCurve discountingCurve) {
    final double expiry = option.getTimeToExpiry();
    final double forward = forwardCurve.getForward(expiry);
    final Interpolator1DDataBundle data = _priceCalculator.getResult(localVolatility, forwardCurve, option, discountingCurve);
    return _interpolator.interpolate(data, forward);
  }
}
