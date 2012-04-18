/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class LocalVolatilityForwardPDEPriceCalculator {
  private final LocalVolatilityForwardPDEPriceGridCalculator _pdeCalculator;
  private final Interpolator1D _interpolator;

  public LocalVolatilityForwardPDEPriceCalculator(final LocalVolatilityForwardPDEPriceGridCalculator pdeCalculator, final Interpolator1D interpolator) {
    _pdeCalculator = pdeCalculator;
    _interpolator = interpolator;
  }

  public double getPrice(final LocalVolatilitySurfaceMoneyness localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option) {
    final double strike = option.getStrike();
    final double expiry = option.getTimeToExpiry();
    final double moneyness = strike / forwardCurve.getForward(expiry);
    final Interpolator1DDataBundle data = _pdeCalculator.getGridPrices(localVolatility, forwardCurve, option);
    return _interpolator.interpolate(data, moneyness); //TODO not correct
  }
}
