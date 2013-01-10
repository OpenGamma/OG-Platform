/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.PDETerminalResults1D;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class LocalVolatilityForwardPDEPriceGridCalculator implements PDELocalVolatilityCalculator<Interpolator1DDataBundle> {
  private final LocalVolatilityForwardPDECalculator _pdeCalculator;
  private final Interpolator1D _interpolator;

  public LocalVolatilityForwardPDEPriceGridCalculator(final LocalVolatilityForwardPDECalculator pdeCalculator, final Interpolator1D interpolator) {
    _pdeCalculator = pdeCalculator;
    _interpolator = interpolator;
  }

  @Override
  public Interpolator1DDataBundle getResult(final LocalVolatilitySurfaceMoneyness localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option,
      final YieldAndDiscountCurve discountingCurve) {
    final PDETerminalResults1D pdeGrid = _pdeCalculator.runPDESolver(localVolatility, option);
    final PDEGrid1D grid = pdeGrid.getGrid();
    final double expiry = option.getTimeToExpiry();
    final boolean isCall = option.isCall();
    final double strike = option.getStrike();
    final double forward = forwardCurve.getForward(expiry);
    final double[] moneynesses = grid.getSpaceNodes();
    final double[] modifiedPrices = pdeGrid.getTerminalResults();
    final int n = modifiedPrices.length;
    final DoubleArrayList strikes = new DoubleArrayList();
    final DoubleArrayList prices = new DoubleArrayList();
    for (int i = 0; i < n; i++) {
      try {
        final double impliedVol = BlackFormulaRepository.impliedVolatility(modifiedPrices[i], 1, moneynesses[i], expiry, isCall);
        prices.add(BlackFormulaRepository.price(forward, strike, expiry, impliedVol, isCall));
        strikes.add(forward * moneynesses[i]);
      } catch (final Exception e) {
      }
    }
    return _interpolator.getDataBundleFromSortedArrays(strikes.toDoubleArray(), prices.toDoubleArray());
  }

  @Override
  public Interpolator1DDataBundle getResult(final LocalVolatilitySurfaceStrike localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option,
      final YieldAndDiscountCurve discountingCurve) {
    return getResult(LocalVolatilitySurfaceConverter.toMoneynessSurface(localVolatility, forwardCurve), forwardCurve, option, discountingCurve);
  }

  public Interpolator1D getInterpolator() {
    return _interpolator;
  }
}
