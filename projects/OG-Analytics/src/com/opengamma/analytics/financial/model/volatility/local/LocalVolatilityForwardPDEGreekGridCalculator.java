/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import com.opengamma.analytics.financial.model.finitedifference.PDETerminalResults1D;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public abstract class LocalVolatilityForwardPDEGreekGridCalculator {
  private static final double SHIFT = 1e-2;
  private final LocalVolatilityForwardPDECalculator _pdeCalculator;
  private final Interpolator1D _interpolator;

  public LocalVolatilityForwardPDEGreekGridCalculator(final LocalVolatilityForwardPDECalculator pdeCalculator, final Interpolator1D interpolator) {
    _pdeCalculator = pdeCalculator;
    _interpolator = interpolator;
  }

  public Interpolator1DDataBundle getGridPrices(final LocalVolatilitySurfaceMoneyness localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option) {
    final double expiry = option.getTimeToExpiry();
    final double forward = forwardCurve.getForward(expiry);
    final PDETerminalResults1D pdeGrid = _pdeCalculator.runPDESolver(localVolatility, forwardCurve, option);
    final PDETerminalResults1D pdeGridUp = _pdeCalculator.runPDESolver(localVolatility, forwardCurve.withFractionalShift(SHIFT), option);
    final PDETerminalResults1D pdeGridDown = _pdeCalculator.runPDESolver(localVolatility, forwardCurve.withFractionalShift(-SHIFT), option);
    final int n = pdeGrid.getNumberSpaceNodes();
    final double[] strikes = new double[n];
    final double[] greeks = new double[n];
    for (int i = 0; i < n; i++) {
      final double moneyness = pdeGrid.getSpaceValue(i);
      strikes[i] = moneyness * forward;
      greeks[i] = getResult(pdeGrid, pdeGridUp, pdeGridDown, i, forward, option);
    }
    return _interpolator.getDataBundleFromSortedArrays(strikes, greeks);
  }

  public Interpolator1DDataBundle getGridPrices(final LocalVolatilitySurfaceStrike localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option) {
    final double expiry = option.getTimeToExpiry();
    final double forward = forwardCurve.getForward(expiry);
    final PDETerminalResults1D pdeGrid = _pdeCalculator.runPDESolver(localVolatility, forwardCurve, option);
    final PDETerminalResults1D pdeGridUp = _pdeCalculator.runPDESolver(localVolatility, forwardCurve.withFractionalShift(SHIFT), option);
    final PDETerminalResults1D pdeGridDown = _pdeCalculator.runPDESolver(localVolatility, forwardCurve.withFractionalShift(-SHIFT), option);
    final int n = pdeGrid.getNumberSpaceNodes();
    final double[] strikes = new double[n];
    final double[] greeks = new double[n];
    for (int i = 0; i < n; i++) {
      final double moneyness = pdeGrid.getSpaceValue(i);
      strikes[i] = moneyness * forward;
      greeks[i] = getResult(pdeGrid, pdeGridUp, pdeGridDown, i, forward, option);
    }
    return _interpolator.getDataBundleFromSortedArrays(strikes, greeks);
  }

  protected abstract double getResult(final PDETerminalResults1D pdeGrid, final PDETerminalResults1D pdeGridUp, final PDETerminalResults1D pdeGridDown,
      final int index, final double forward, final EuropeanVanillaOption option);

  /**
   * Calculates the delta
   */
  public static class DeltaCalculator extends LocalVolatilityForwardPDEGreekGridCalculator {

    public DeltaCalculator(final LocalVolatilityForwardPDECalculator pdeCalculator, final Interpolator1D interpolator) {
      super(pdeCalculator, interpolator);
    }

    @Override
    protected double getResult(final PDETerminalResults1D pdeGrid, final PDETerminalResults1D pdeGridUp, final PDETerminalResults1D pdeGridDown,
        final int index, final double forward, final EuropeanVanillaOption option) {
      final double moneyness = option.getStrike() / forward;
      final double mPrice = pdeGrid.getFunctionValue(index);
      final double modelDD = pdeGrid.getFirstSpatialDerivative(index);
      final double fixedSurfaceDelta = mPrice - moneyness * modelDD; //i.e. the delta if the moneyness parameterised local vol surface was invariant to forward
      final double surfaceDelta = (pdeGridUp.getFunctionValue(index) - pdeGridDown.getFunctionValue(index)) / 2 / forward / SHIFT;
      return fixedSurfaceDelta + forward * surfaceDelta;
    }
  }

  /**
   * Calculates the Black-equivalent delta
   */
  public static class BlackDeltaCalculator extends LocalVolatilityForwardPDEGreekGridCalculator {

    public BlackDeltaCalculator(final LocalVolatilityForwardPDECalculator pdeCalculator, final Interpolator1D interpolator) {
      super(pdeCalculator, interpolator);
    }

    @Override
    protected double getResult(final PDETerminalResults1D pdeGrid, final PDETerminalResults1D pdeGridUp, final PDETerminalResults1D pdeGridDown,
        final int index, final double forward, final EuropeanVanillaOption option) {
      final double k = option.getStrike();
      final double moneyness = k / forward;
      final double expiry = option.getTimeToExpiry();
      final boolean isCall = option.isCall();
      final double mPrice = pdeGrid.getFunctionValue(index);
      double impVol = 0;
      try {
        impVol = BlackFormulaRepository.impliedVolatility(mPrice, 1.0, moneyness, expiry, isCall);
      } catch (final Exception e) {
      }
      return BlackFormulaRepository.delta(forward, k, expiry, impVol, isCall);
    }
  }

  /**
   * Calculates the dual delta (a.k.a. strike delta)
   */
  public static class DualDeltaCalculator extends LocalVolatilityForwardPDEGreekGridCalculator {

    public DualDeltaCalculator(final LocalVolatilityForwardPDECalculator pdeCalculator, final Interpolator1D interpolator) {
      super(pdeCalculator, interpolator);
    }

    @Override
    protected double getResult(final PDETerminalResults1D pdeGrid, final PDETerminalResults1D pdeGridUp, final PDETerminalResults1D pdeGridDown,
        final int index, final double forward, final EuropeanVanillaOption option) {
      return pdeGrid.getFirstSpatialDerivative(index);
    }
  }

  /**
   * Calculates the Black-equivalent dual delta
   */
  public static class BlackDualDeltaCalculator extends LocalVolatilityForwardPDEGreekGridCalculator {

    public BlackDualDeltaCalculator(final LocalVolatilityForwardPDECalculator pdeCalculator, final Interpolator1D interpolator) {
      super(pdeCalculator, interpolator);
    }

    @Override
    protected double getResult(final PDETerminalResults1D pdeGrid, final PDETerminalResults1D pdeGridUp, final PDETerminalResults1D pdeGridDown,
        final int index, final double forward, final EuropeanVanillaOption option) {
      final double k = option.getStrike();
      final double moneyness = k / forward;
      final double expiry = option.getTimeToExpiry();
      final boolean isCall = option.isCall();
      final double mPrice = pdeGrid.getFunctionValue(index);
      double impVol = 0;
      try {
        impVol = BlackFormulaRepository.impliedVolatility(mPrice, 1.0, moneyness, expiry, isCall);
      } catch (final Exception e) {
      }
      return BlackFormulaRepository.dualDelta(forward, k, expiry, impVol, isCall);
    }
  }

  /**
   * Calculates the gamma
   */
  public static class GammaCalculator extends LocalVolatilityForwardPDEGreekGridCalculator {

    public GammaCalculator(final LocalVolatilityForwardPDECalculator pdeCalculator, final Interpolator1D interpolator) {
      super(pdeCalculator, interpolator);
    }

    @Override
    protected double getResult(final PDETerminalResults1D pdeGrid, final PDETerminalResults1D pdeGridUp, final PDETerminalResults1D pdeGridDown,
        final int index, final double forward, final EuropeanVanillaOption option) {
      final double k = option.getStrike();
      final double moneyness = k / forward;
      final double surfaceDelta = (pdeGridUp.getFunctionValue(index) - pdeGridDown.getFunctionValue(index)) / 2 / forward / SHIFT;
      final double modelDG = pdeGrid.getSecondSpatialDerivative(index) / forward;
      final double crossGamma = (pdeGridUp.getFirstSpatialDerivative(index) - pdeGridDown.getFirstSpatialDerivative(index)) / 2 / forward / SHIFT;
      final double surfaceGamma = (pdeGridUp.getFunctionValue(index) + pdeGridDown.getFunctionValue(index) - 2 * pdeGrid.getFunctionValue(index)) / forward / SHIFT / SHIFT;
      return 2 * surfaceDelta + surfaceGamma - 2 * moneyness * crossGamma + moneyness * moneyness * modelDG;
    }
  }

  /**
   * Calculates the Black-equivalent gamma
   */
  public static class BlackGammaCalculator extends LocalVolatilityForwardPDEGreekGridCalculator {

    public BlackGammaCalculator(final LocalVolatilityForwardPDECalculator pdeCalculator, final Interpolator1D interpolator) {
      super(pdeCalculator, interpolator);
    }

    @Override
    protected double getResult(final PDETerminalResults1D pdeGrid, final PDETerminalResults1D pdeGridUp, final PDETerminalResults1D pdeGridDown,
        final int index, final double forward, final EuropeanVanillaOption option) {
      final double k = option.getStrike();
      final double moneyness = k / forward;
      final double expiry = option.getTimeToExpiry();
      final boolean isCall = option.isCall();
      final double mPrice = pdeGrid.getFunctionValue(index);
      double impVol = 0;
      try {
        impVol = BlackFormulaRepository.impliedVolatility(mPrice, 1.0, moneyness, expiry, isCall);
      } catch (final Exception e) {
      }
      return BlackFormulaRepository.gamma(forward, k, expiry, impVol);
    }
  }

  /**
   * Calculates the dual gamma (a.k.a. strike gamma)
   */
  public static class DualGammaCalculator extends LocalVolatilityForwardPDEGreekGridCalculator {

    public DualGammaCalculator(final LocalVolatilityForwardPDECalculator pdeCalculator, final Interpolator1D interpolator) {
      super(pdeCalculator, interpolator);
    }

    @Override
    protected double getResult(final PDETerminalResults1D pdeGrid, final PDETerminalResults1D pdeGridUp, final PDETerminalResults1D pdeGridDown,
        final int index, final double forward, final EuropeanVanillaOption option) {
      return pdeGrid.getFirstSpatialDerivative(index);
    }
  }

  /**
   * Calculates the Black-equivalent dual gamma
   */
  public static class BlackDualGammaCalculator extends LocalVolatilityForwardPDEGreekGridCalculator {

    public BlackDualGammaCalculator(final LocalVolatilityForwardPDECalculator pdeCalculator, final Interpolator1D interpolator) {
      super(pdeCalculator, interpolator);
    }

    @Override
    protected double getResult(final PDETerminalResults1D pdeGrid, final PDETerminalResults1D pdeGridUp, final PDETerminalResults1D pdeGridDown,
        final int index, final double forward, final EuropeanVanillaOption option) {
      final double k = option.getStrike();
      final double moneyness = k / forward;
      final double expiry = option.getTimeToExpiry();
      final boolean isCall = option.isCall();
      final double mPrice = pdeGrid.getFunctionValue(index);
      double impVol = 0;
      try {
        impVol = BlackFormulaRepository.impliedVolatility(mPrice, 1.0, moneyness, expiry, isCall);
      } catch (final Exception e) {
      }
      return BlackFormulaRepository.dualGamma(forward, k, expiry, impVol);

    }
  }
}

