/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import com.opengamma.analytics.financial.model.finitedifference.PDETerminalResults1D;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public abstract class LocalVolatilityForwardPDEStrikeGreeksGridCalculator implements PDELocalVolatilityCalculator<Interpolator1DDataBundle> {
  private final LocalVolatilityForwardPDECalculator _pdeCalculator;
  private final Interpolator1D _interpolator;

  public LocalVolatilityForwardPDEStrikeGreeksGridCalculator(final LocalVolatilityForwardPDECalculator pdeCalculator, final Interpolator1D interpolator) {
    _pdeCalculator = pdeCalculator;
    _interpolator = interpolator;
  }

  @Override
  public Interpolator1DDataBundle getResult(final LocalVolatilitySurfaceMoneyness localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option,
      final YieldAndDiscountCurve discountingCurve) {
    final double expiry = option.getTimeToExpiry();
    final double forward = forwardCurve.getForward(expiry);
    final PDETerminalResults1D pdeGrid = _pdeCalculator.runPDESolver(localVolatility, option);
    final PDETerminalResults1D pdeGridUp = _pdeCalculator.runPDESolver(localVolatility, option);
    final PDETerminalResults1D pdeGridDown = _pdeCalculator.runPDESolver(localVolatility, option);
    final int n = pdeGrid.getNumberSpaceNodes();
    final double[] strikes = new double[n];
    final double[] greeks = new double[n];
    for (int i = 0; i < n; i++) {
      final double moneyness = pdeGrid.getSpaceValue(i);
      strikes[i] = moneyness * forward;
      greeks[i] = getResultForMoneyness(pdeGrid, pdeGridUp, pdeGridDown, i, forward, option);
    }
    return _interpolator.getDataBundleFromSortedArrays(strikes, greeks);
  }

  @Override
  public Interpolator1DDataBundle getResult(final LocalVolatilitySurfaceStrike localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option,
      final YieldAndDiscountCurve discountingCurve) {
    return getResult(LocalVolatilitySurfaceConverter.toMoneynessSurface(localVolatility, forwardCurve), forwardCurve, option, discountingCurve);
  }

  protected abstract double getResultForMoneyness(final PDETerminalResults1D pdeGrid, final PDETerminalResults1D pdeGridUp, final PDETerminalResults1D pdeGridDown,
      final int index, final double forward, final EuropeanVanillaOption option);

  public Interpolator1D getInterpolator() {
    return _interpolator;
  }

  /**
   * Calculates the dual delta (a.k.a. strike delta)
   */
  public static class DualDeltaCalculator extends LocalVolatilityForwardPDEStrikeGreeksGridCalculator {

    public DualDeltaCalculator(final LocalVolatilityForwardPDECalculator pdeCalculator, final Interpolator1D interpolator) {
      super(pdeCalculator, interpolator);
    }

    @Override
    protected double getResultForMoneyness(final PDETerminalResults1D pdeGrid, final PDETerminalResults1D pdeGridUp, final PDETerminalResults1D pdeGridDown,
        final int index, final double forward, final EuropeanVanillaOption option) {
      return pdeGrid.getFirstSpatialDerivative(index);
    }
  }

  /**
   * Calculates the dual gamma (a.k.a. strike gamma)
   */
  public static class DualGammaCalculator extends LocalVolatilityForwardPDEStrikeGreeksGridCalculator {

    public DualGammaCalculator(final LocalVolatilityForwardPDECalculator pdeCalculator, final Interpolator1D interpolator) {
      super(pdeCalculator, interpolator);
    }

    @Override
    protected double getResultForMoneyness(final PDETerminalResults1D pdeGrid, final PDETerminalResults1D pdeGridUp, final PDETerminalResults1D pdeGridDown,
        final int index, final double forward, final EuropeanVanillaOption option) {
      return pdeGrid.getSecondSpatialDerivative(index) / forward;
    }
  }

}
