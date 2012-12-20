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
import com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SurfaceArrayUtils;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.StandardSmileSurfaceDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceMoneyness;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurfaceInterpolator;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class LocalVolatilityForwardPDEBucketedVegaCalculator {
  private static final double SHIFT = 1e-4;
  private final LocalVolatilityForwardPDECalculator _pdeCalculator;
  private final Interpolator1D _interpolator;
  private final DupireLocalVolatilityCalculator _dupireCalculator;
  private final VolatilitySurfaceInterpolator _surfaceInterpolator;

  public LocalVolatilityForwardPDEBucketedVegaCalculator(final LocalVolatilityForwardPDECalculator pdeCalculator, final Interpolator1D interpolator,
      final DupireLocalVolatilityCalculator dupireCalculator, final VolatilitySurfaceInterpolator surfaceInterpolator) {
    _pdeCalculator = pdeCalculator;
    _interpolator = interpolator;
    _dupireCalculator = dupireCalculator;
    _surfaceInterpolator = surfaceInterpolator;
  }

  public double[][] getResult(final LocalVolatilitySurfaceMoneyness localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option,
      final StandardSmileSurfaceDataBundle marketData) {
    final int n = marketData.getNumExpiries();
    final double[][] strikes = marketData.getStrikes();
    final double expiry = option.getTimeToExpiry();
    final double forward = forwardCurve.getForward(expiry);
    final double x = option.getStrike() / forward;
    final PDETerminalResults1D pdeGrid = _pdeCalculator.runPDESolver(localVolatility, option);
    final int spaceSteps = _pdeCalculator.getNSpaceSteps();
    final double[] xNodes = pdeGrid.getGrid().getSpaceNodes();
    int index = SurfaceArrayUtils.getLowerBoundIndex(xNodes, x);
    if (index >= 1) {
      index--;
    }
    if (index >= spaceSteps - 1) {
      index--;
      if (index >= spaceSteps - 1) {
        index--;
      }
    }
    final double[] vols = new double[4];
    final double[] moneyness = new double[4];
    System.arraycopy(xNodes, index, moneyness, 0, 4);
    for (int i = 0; i < 4; i++) {
      vols[i] = BlackFormulaRepository.impliedVolatility(pdeGrid.getFunctionValue(index + i), 1.0, moneyness[i],
          expiry, option.isCall());
    }
    Interpolator1DDataBundle db = _interpolator.getDataBundle(moneyness, vols);
    final double exampleVol = _interpolator.interpolate(db, x);
    final double[][] res = new double[n][];
    for (int i = 0; i < n; i++) {
      final int m = strikes[i].length;
      res[i] = new double[m];
      for (int j = 0; j < m; j++) {
        final BlackVolatilitySurfaceMoneyness bumpedSurface = _surfaceInterpolator.getBumpedVolatilitySurface(marketData, i, j, SHIFT);
        final LocalVolatilitySurfaceMoneyness bumpedLV = _dupireCalculator.getLocalVolatility(bumpedSurface);
        final PDETerminalResults1D pdeResBumped = _pdeCalculator.runPDESolver(bumpedLV, option);
        for (int k = 0; k < 4; k++) {
          vols[k] = BlackFormulaRepository.impliedVolatility(pdeResBumped.getFunctionValue(index + k), 1.0, moneyness[k],
              expiry, option.isCall());
        }
        db = _interpolator.getDataBundle(moneyness, vols);
        final double vol = _interpolator.interpolate(db, x);
        res[i][j] = (vol - exampleVol) / SHIFT;
      }
    }
    return res;
  }

  public double[][] getResult(final LocalVolatilitySurfaceStrike localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option,
      final StandardSmileSurfaceDataBundle marketData) {
    final int n = marketData.getNumExpiries();
    final double[][] strikes = marketData.getStrikes();
    final double expiry = option.getTimeToExpiry();
    final double forward = forwardCurve.getForward(expiry);
    final double x = option.getStrike() / forward;
    final PDETerminalResults1D pdeGrid = _pdeCalculator.runPDESolver(localVolatility, forwardCurve, option);
    final int spaceSteps = _pdeCalculator.getNSpaceSteps();
    final double[] xNodes = pdeGrid.getGrid().getSpaceNodes();
    int index = SurfaceArrayUtils.getLowerBoundIndex(xNodes, x);
    if (index >= 1) {
      index--;
    }
    if (index >= spaceSteps - 1) {
      index--;
      if (index >= spaceSteps - 1) {
        index--;
      }
    }
    final double[] vols = new double[4];
    final double[] moneyness = new double[4];
    System.arraycopy(xNodes, index, moneyness, 0, 4);
    for (int i = 0; i < 4; i++) {
      vols[i] = BlackFormulaRepository.impliedVolatility(pdeGrid.getFunctionValue(index + i), 1.0, moneyness[i],
          expiry, option.isCall());
    }
    Interpolator1DDataBundle db = _interpolator.getDataBundle(moneyness, vols);
    final double exampleVol = _interpolator.interpolate(db, x);
    final double[][] res = new double[n][];
    for (int i = 0; i < n; i++) {
      final int m = strikes[i].length;
      res[i] = new double[m];
      for (int j = 0; j < m; j++) {
        final BlackVolatilitySurfaceMoneyness bumpedSurface = _surfaceInterpolator.getBumpedVolatilitySurface(marketData, i, j, SHIFT);
        final LocalVolatilitySurfaceMoneyness bumpedLV = _dupireCalculator.getLocalVolatility(bumpedSurface);
        final PDETerminalResults1D pdeResBumped = _pdeCalculator.runPDESolver(bumpedLV, option);
        for (int k = 0; k < 4; k++) {
          vols[k] = BlackFormulaRepository.impliedVolatility(pdeResBumped.getFunctionValue(index + k), 1.0, moneyness[k],
              expiry, option.isCall());
        }
        db = _interpolator.getDataBundle(moneyness, vols);
        final double vol = _interpolator.interpolate(db, x);
        res[i][j] = (vol - exampleVol) / SHIFT;
      }
    }
    return res;
  }
}
