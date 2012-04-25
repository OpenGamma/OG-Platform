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
//TODO desperately needs renaming
public class LocalVolatilityForwardPDESingleResultCalculator implements PDELocalVolatilityCalculator<Double> {
  private final NearestNPointsInterpolator _interpolator;
  private final PDELocalVolatilityCalculator<Interpolator1DDataBundle> _calculator;

  public LocalVolatilityForwardPDESingleResultCalculator(final PDELocalVolatilityCalculator<Interpolator1DDataBundle> calculator, final Interpolator1D interpolator) {
    _calculator = calculator;
    _interpolator = new NearestNPointsInterpolator(interpolator, 4);
  }

  @Override
  public Double getResult(final LocalVolatilitySurfaceMoneyness localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option, final YieldAndDiscountCurve discountingCurve) {
    final double strike = option.getStrike();
    final Interpolator1DDataBundle data = _calculator.getResult(localVolatility, forwardCurve, option, discountingCurve);
    return _interpolator.interpolate(data, strike);
  }

  @Override
  public Double getResult(final LocalVolatilitySurfaceStrike localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option, final YieldAndDiscountCurve discountingCurve) {
    final double strike = option.getStrike();
    final Interpolator1DDataBundle data = _calculator.getResult(localVolatility, forwardCurve, option, discountingCurve);
    return _interpolator.interpolate(data, strike);
  }
}
