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
public abstract class LocalVolatilityBackwardPDESpotGreeksGridCalculator implements PDELocalVolatilityCalculator<Interpolator1DDataBundle> {
  private final LocalVolatilityBackwardPDECalculator _pdeCalculator;
  private final Interpolator1D _interpolator;

  public LocalVolatilityBackwardPDESpotGreeksGridCalculator(final LocalVolatilityBackwardPDECalculator pdeCalculator, final Interpolator1D interpolator) {
    _pdeCalculator = pdeCalculator;
    _interpolator = interpolator;
  }

  @Override
  public Interpolator1DDataBundle getResult(final LocalVolatilitySurfaceMoneyness localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option,
      final YieldAndDiscountCurve discountingCurve) {
    final PDETerminalResults1D pdeGrid = _pdeCalculator.runPDESolver(localVolatility, option);
    final int n = pdeGrid.getNumberSpaceNodes();
    final double[] forwards = new double[n];
    final double[] greeks = new double[n];
    for (int i = 0; i < n; i++) {
      forwards[i] = pdeGrid.getSpaceValue(i);
      greeks[i] = getResultForForward(pdeGrid, i);
    }
    return _interpolator.getDataBundleFromSortedArrays(forwards, greeks);
  }

  @Override
  public Interpolator1DDataBundle getResult(final LocalVolatilitySurfaceStrike localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option,
      final YieldAndDiscountCurve discountingCurve) {
    final PDETerminalResults1D pdeGrid = _pdeCalculator.runPDESolver(localVolatility, forwardCurve, option);
    final int n = pdeGrid.getNumberSpaceNodes();
    final double[] forwards = new double[n];
    final double[] greeks = new double[n];
    for (int i = 0; i < n; i++) {
      forwards[i] = pdeGrid.getSpaceValue(i);
      greeks[i] = getResultForForward(pdeGrid, i);
    }
    return _interpolator.getDataBundleFromSortedArrays(forwards, greeks);
  }

  protected abstract double getResultForForward(final PDETerminalResults1D pdeGrid, final int index);

  /**
   * Calculates the delta
   */
  public static class DeltaCalculator extends LocalVolatilityBackwardPDESpotGreeksGridCalculator {

    public DeltaCalculator(final LocalVolatilityBackwardPDECalculator pdeCalculator, final Interpolator1D interpolator) {
      super(pdeCalculator, interpolator);
    }

    @Override
    protected double getResultForForward(final PDETerminalResults1D pdeGrid, final int index) {
      return pdeGrid.getFirstSpatialDerivative(index);
    }
  }

  /**
   * Calculates the gamma
   */
  public static class GammaCalculator extends LocalVolatilityBackwardPDESpotGreeksGridCalculator {

    public GammaCalculator(final LocalVolatilityBackwardPDECalculator pdeCalculator, final Interpolator1D interpolator) {
      super(pdeCalculator, interpolator);
    }

    @Override
    protected double getResultForForward(final PDETerminalResults1D pdeGrid, final int index) {
      return pdeGrid.getSecondSpatialDerivative(index);
    }
  }
}
