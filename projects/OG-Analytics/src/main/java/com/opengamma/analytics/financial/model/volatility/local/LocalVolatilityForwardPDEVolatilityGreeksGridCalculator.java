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
import com.opengamma.analytics.math.surface.SurfaceShiftFunctionFactory;

/**
 * 
 */
public abstract class LocalVolatilityForwardPDEVolatilityGreeksGridCalculator implements PDELocalVolatilityCalculator<Interpolator1DDataBundle> {
  private static final double VOL_SHIFT = 1e-3;
  private static final double FWD_SHIFT = 5e-2;
  private final LocalVolatilityForwardPDECalculator _pdeCalculator;
  private final Interpolator1D _interpolator;

  public LocalVolatilityForwardPDEVolatilityGreeksGridCalculator(final LocalVolatilityForwardPDECalculator pdeCalculator, final Interpolator1D interpolator) {
    _pdeCalculator = pdeCalculator;
    _interpolator = interpolator;
  }

  @Override
  public Interpolator1DDataBundle getResult(final LocalVolatilitySurfaceMoneyness localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option,
      final YieldAndDiscountCurve discountingCurve) {
    final LocalVolatilitySurfaceStrike lvStrike = LocalVolatilitySurfaceConverter.toStrikeSurface(localVolatility);
    final LocalVolatilitySurfaceStrike localVolatilityUp = new LocalVolatilitySurfaceStrike(SurfaceShiftFunctionFactory.getShiftedSurface(lvStrike.getSurface(), VOL_SHIFT, true));
    final LocalVolatilitySurfaceStrike localVolatilityDown = new LocalVolatilitySurfaceStrike(SurfaceShiftFunctionFactory.getShiftedSurface(lvStrike.getSurface(), -VOL_SHIFT, true));
    final ForwardCurve forwardCurveUp = forwardCurve.withFractionalShift(FWD_SHIFT);
    final ForwardCurve forwardCurveDown = forwardCurve.withFractionalShift(-FWD_SHIFT);
    final PDETerminalResults1D pdeGrid = _pdeCalculator.runPDESolver(localVolatility, option);
    final PDETerminalResults1D pdeGridUp = _pdeCalculator.runPDESolver(localVolatilityUp, forwardCurve, option);
    final PDETerminalResults1D pdeGridDown = _pdeCalculator.runPDESolver(localVolatilityDown, forwardCurve, option);
    final PDETerminalResults1D pdeGridUpUp = _pdeCalculator.runPDESolver(localVolatilityUp, forwardCurveUp, option);
    final PDETerminalResults1D pdeGridUpDown = _pdeCalculator.runPDESolver(localVolatilityDown, forwardCurveUp, option);
    final PDETerminalResults1D pdeGridDownUp = _pdeCalculator.runPDESolver(localVolatilityUp, forwardCurveDown, option);
    final PDETerminalResults1D pdeGridDownDown = _pdeCalculator.runPDESolver(localVolatilityDown, forwardCurveDown, option);
    final int n = pdeGrid.getNumberSpaceNodes();
    final double expiry = option.getTimeToExpiry();
    final double forward = forwardCurve.getForward(expiry);
    final double[] strikes = new double[n];
    final double[] greeks = new double[n];
    for (int i = 0; i < n; i++) {
      final double moneyness = pdeGrid.getSpaceValue(i);
      strikes[i] = moneyness * forward;
      greeks[i] = getResultForMoneyness(pdeGrid, pdeGridUp, pdeGridDown, pdeGridUpUp, pdeGridUpDown, pdeGridDownUp, pdeGridDownDown, i, forward, option);
    }
    //    //debug
    //    double[] t = new double[] {7. / 365, 14 / 365., 21 / 365., 1 / 12., 3 / 12., 0.5, 0.75, 1, 5, 10 };
    //    int m = t.length;
    //    double[] debug = new double[m];
    //    for (int i = 0; i < m; i++) {
    //      debug[i] = forwardCurve.getForward(t[i]);
    //    }

    return _interpolator.getDataBundleFromSortedArrays(strikes, greeks);
  }

  @Override
  public Interpolator1DDataBundle getResult(final LocalVolatilitySurfaceStrike localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option,
      final YieldAndDiscountCurve discountingCurve) {
    final LocalVolatilitySurfaceStrike localVolatilityUp = new LocalVolatilitySurfaceStrike(SurfaceShiftFunctionFactory.getShiftedSurface(localVolatility.getSurface(), VOL_SHIFT, true));
    final LocalVolatilitySurfaceStrike localVolatilityDown = new LocalVolatilitySurfaceStrike(SurfaceShiftFunctionFactory.getShiftedSurface(localVolatility.getSurface(), -VOL_SHIFT, true));
    final ForwardCurve forwardCurveUp = forwardCurve.withFractionalShift(FWD_SHIFT);
    final ForwardCurve forwardCurveDown = forwardCurve.withFractionalShift(-FWD_SHIFT);
    final PDETerminalResults1D pdeGrid = _pdeCalculator.runPDESolver(localVolatility, forwardCurve, option);
    final PDETerminalResults1D pdeGridUp = _pdeCalculator.runPDESolver(localVolatilityUp, forwardCurve, option);
    final PDETerminalResults1D pdeGridDown = _pdeCalculator.runPDESolver(localVolatilityDown, forwardCurve, option);
    final PDETerminalResults1D pdeGridUpUp = _pdeCalculator.runPDESolver(localVolatilityUp, forwardCurveUp, option);
    final PDETerminalResults1D pdeGridUpDown = _pdeCalculator.runPDESolver(localVolatilityDown, forwardCurveUp, option);
    final PDETerminalResults1D pdeGridDownUp = _pdeCalculator.runPDESolver(localVolatilityUp, forwardCurveDown, option);
    final PDETerminalResults1D pdeGridDownDown = _pdeCalculator.runPDESolver(localVolatilityDown, forwardCurveDown, option);
    final int n = pdeGrid.getNumberSpaceNodes();
    final double expiry = option.getTimeToExpiry();
    final double forward = forwardCurve.getForward(expiry);
    final double[] strikes = new double[n];
    final double[] greeks = new double[n];
    for (int i = 0; i < n; i++) {
      final double moneyness = pdeGrid.getSpaceValue(i);
      strikes[i] = moneyness * forward;
      greeks[i] = getResultForMoneyness(pdeGrid, pdeGridUp, pdeGridDown, pdeGridUpUp, pdeGridUpDown, pdeGridDownUp, pdeGridDownDown, i, forward, option);
    }
    return _interpolator.getDataBundleFromSortedArrays(strikes, greeks);
  }

  protected abstract double getResultForMoneyness(final PDETerminalResults1D pdeGrid, final PDETerminalResults1D pdeGridUp, final PDETerminalResults1D pdeGridDown,
      final PDETerminalResults1D pdeGridUpUp, final PDETerminalResults1D pdeGridUpDown, final PDETerminalResults1D pdeGridDownUp, final PDETerminalResults1D pdeGridDownDown,
      final int index, final double forward, final EuropeanVanillaOption option);

  public Interpolator1D getInterpolator() {
    return _interpolator;
  }

  /**
   * Calculates the vega
   */
  public static class VegaCalculator extends LocalVolatilityForwardPDEVolatilityGreeksGridCalculator {

    public VegaCalculator(final LocalVolatilityForwardPDECalculator pdeCalculator, final Interpolator1D interpolator) {
      super(pdeCalculator, interpolator);
    }

    @Override
    protected double getResultForMoneyness(final PDETerminalResults1D pdeGrid, final PDETerminalResults1D pdeGridUp, final PDETerminalResults1D pdeGridDown,
        final PDETerminalResults1D pdeGridUpUp, final PDETerminalResults1D pdeGridUpDown, final PDETerminalResults1D pdeGridDownUp, final PDETerminalResults1D pdeGridDownDown,
        final int index, final double forward, final EuropeanVanillaOption option) {
      return forward * (pdeGridUp.getFunctionValue(index) - pdeGridDown.getFunctionValue(index)) / 2 / VOL_SHIFT;
    }
  }

  /**
   * Calculates the vanna
   */
  public static class VannaCalculator extends LocalVolatilityForwardPDEVolatilityGreeksGridCalculator {

    public VannaCalculator(final LocalVolatilityForwardPDECalculator pdeCalculator, final Interpolator1D interpolator) {
      super(pdeCalculator, interpolator);
    }

    @Override
    protected double getResultForMoneyness(final PDETerminalResults1D pdeGrid, final PDETerminalResults1D pdeGridUp, final PDETerminalResults1D pdeGridDown,
        final PDETerminalResults1D pdeGridUpUp, final PDETerminalResults1D pdeGridUpDown, final PDETerminalResults1D pdeGridDownUp, final PDETerminalResults1D pdeGridDownDown,
        final int index, final double forward, final EuropeanVanillaOption option) {
      final double x = pdeGrid.getSpaceValue(index);
      //xVanna is the vanna if the moneyness parameterised local vol surface were invariant to changes in the forward curve
      final double xVanna = (pdeGridUp.getFunctionValue(index) - pdeGridDown.getFunctionValue(index)
          - x * (pdeGridUp.getFirstSpatialDerivative(index) - pdeGridDown.getFirstSpatialDerivative(index))) / 2 / VOL_SHIFT;
      //this is the vanna coming purely from deformation of the local volatility surface
      final double surfaceVanna = (pdeGridUpUp.getFunctionValue(index) + pdeGridDownDown.getFunctionValue(index) -
          pdeGridUpDown.getFunctionValue(index) - pdeGridDownUp.getFunctionValue(index)) / 4 / FWD_SHIFT / VOL_SHIFT;
      return xVanna + surfaceVanna;
    }
  }

  /**
   * Calculates the vomma
   */
  public static class VommaCalculator extends LocalVolatilityForwardPDEVolatilityGreeksGridCalculator {

    public VommaCalculator(final LocalVolatilityForwardPDECalculator pdeCalculator, final Interpolator1D interpolator) {
      super(pdeCalculator, interpolator);
    }

    @Override
    protected double getResultForMoneyness(final PDETerminalResults1D pdeGrid, final PDETerminalResults1D pdeGridUp, final PDETerminalResults1D pdeGridDown,
        final PDETerminalResults1D pdeGridUpUp, final PDETerminalResults1D pdeGridUpDown, final PDETerminalResults1D pdeGridDownUp, final PDETerminalResults1D pdeGridDownDown,
        final int index, final double forward, final EuropeanVanillaOption option) {
      return forward * (pdeGridUp.getFunctionValue(index) + pdeGridDown.getFunctionValue(index) - 2 * pdeGrid.getFunctionValue(index)) / VOL_SHIFT / VOL_SHIFT;
    }
  }
}
