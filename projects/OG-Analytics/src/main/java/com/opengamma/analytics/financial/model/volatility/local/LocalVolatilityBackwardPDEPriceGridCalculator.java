/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.PDETerminalResults1D;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class LocalVolatilityBackwardPDEPriceGridCalculator implements PDELocalVolatilityCalculator<Interpolator1DDataBundle> {
  private final LocalVolatilityBackwardPDECalculator _pdeCalculator;
  private final Interpolator1D _interpolator;

  public LocalVolatilityBackwardPDEPriceGridCalculator(final LocalVolatilityBackwardPDECalculator pdeCalculator, final Interpolator1D interpolator) {
    _pdeCalculator = pdeCalculator;
    _interpolator = interpolator;
  }

  @Override
  public Interpolator1DDataBundle getResult(final LocalVolatilitySurfaceMoneyness localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option,
      final YieldAndDiscountCurve discountingCurve) {
    final PDETerminalResults1D pdeGrid = _pdeCalculator.runPDESolver(localVolatility, option);
    final PDEGrid1D grid = pdeGrid.getGrid();
    final double df = discountingCurve.getDiscountFactor(option.getTimeToExpiry());
    final double[] forwards = grid.getSpaceNodes();
    final double[] forwardPrices = pdeGrid.getTerminalResults();
    final int n = forwards.length;
    final double[] prices = new double[n];
    for (int i = 0; i < n; i++) {
      prices[i] = forwardPrices[i] * df;
    }
    return _interpolator.getDataBundleFromSortedArrays(forwards, prices);
  }

  @Override
  public Interpolator1DDataBundle getResult(final LocalVolatilitySurfaceStrike localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option,
      final YieldAndDiscountCurve discountingCurve) {
    final PDETerminalResults1D pdeGrid = _pdeCalculator.runPDESolver(localVolatility, forwardCurve, option);
    final PDEGrid1D grid = pdeGrid.getGrid();
    final double[] forwards = grid.getSpaceNodes();
    final double[] forwardPrices = pdeGrid.getTerminalResults();
    return _interpolator.getDataBundleFromSortedArrays(forwards, forwardPrices);
  }

}
