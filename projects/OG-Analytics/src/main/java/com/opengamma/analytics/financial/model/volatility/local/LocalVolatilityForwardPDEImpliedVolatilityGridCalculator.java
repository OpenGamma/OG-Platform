/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import java.util.Arrays;

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
public class LocalVolatilityForwardPDEImpliedVolatilityGridCalculator implements PDELocalVolatilityCalculator<Interpolator1DDataBundle> {
  private final LocalVolatilityForwardPDECalculator _pdeCalculator;
  private final Interpolator1D _interpolator;

  public LocalVolatilityForwardPDEImpliedVolatilityGridCalculator(final LocalVolatilityForwardPDECalculator pdeCalculator, final Interpolator1D interpolator) {
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
    final double forward = forwardCurve.getForward(expiry);
    final double[] moneynesses = grid.getSpaceNodes();
    final double[] modifiedPrices = pdeGrid.getTerminalResults();
    final int n = modifiedPrices.length;
    double[] strikes = new double[n];
    double[] impliedVols = new double[n];
    int count = 0;
    for (int i = 0; i < n; i++) {
      try {
        impliedVols[count] = BlackFormulaRepository.impliedVolatility(modifiedPrices[i], 1, moneynesses[i], expiry, isCall);
        strikes[count] = forward * moneynesses[i];
        count++;
      } catch (Exception e) {
      }
    }
    strikes = Arrays.copyOfRange(strikes, 0, count);
    impliedVols = Arrays.copyOfRange(impliedVols, 0, count);
    return _interpolator.getDataBundleFromSortedArrays(strikes, impliedVols);
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
