/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import com.opengamma.analytics.financial.model.finitedifference.PDEResults1D;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.StandardSmileSurfaceDataBundle;

/**
 * 
 */
public class LocalVolatilityBackwardPDEDeltaGammaCalculator {
  private final LocalVolatilityBackwardPDECalculator _backwardPDE;

  public LocalVolatilityBackwardPDEDeltaGammaCalculator(final double theta, final int nTimeSteps, final int nYSteps, final double timeMeshLambda, final double yMeshBunching) {
    _backwardPDE = new LocalVolatilityBackwardPDECalculator(theta, nTimeSteps, nYSteps, timeMeshLambda, yMeshBunching);
  }

  public Object runBackwardsPDESolver(final LocalVolatilitySurfaceStrike localVolatility, final ForwardCurve forwardCurve,
      final StandardSmileSurfaceDataBundle data, final EuropeanVanillaOption option) {
    PDEResults1D res = _backwardPDE.runBackwardsPDESolver(localVolatility, forwardCurve, data, option);
    final double strike = option.getStrike();
    final double expiry = option.getTimeToExpiry();
    final boolean isCall = option.isCall();
    final double forward = forwardCurve.getForward(expiry);
    final int n = res.getNumberSpaceNodes();
    for (int i = 0; i < n; i++) {
      final double price = res.getFunctionValue(i);
      final double fwd = res.getGrid().getSpaceNode(i);
      double impVol = 0;
      try {
        impVol = BlackFormulaRepository.impliedVolatility(price, fwd, strike, expiry, isCall);
      } catch (final Exception e) {
      }
      final double bsDelta = BlackFormulaRepository.delta(fwd, strike, expiry, impVol, isCall);
      final double bsGamma = BlackFormulaRepository.gamma(fwd, strike, expiry, impVol);
      final double modelDelta = res.getFirstSpatialDerivative(i);
      final double modelGamma = res.getSecondSpatialDerivative(i);
    }
    //finally run the backwards PDE solver 100 times with different strikes,  interpolating to get vol, delta and gamma at the forward
    final int xIndex = res.getGrid().getLowerBoundIndexForSpace(forward);
    final double actForward = res.getSpaceValue(xIndex);
    final double f1 = res.getSpaceValue(xIndex);
    final double f2 = res.getSpaceValue(xIndex + 1);
    final double w = (f2 - forward) / (f2 - f1);
    for (int i = 0; i < 100; i++) {
      final double k = forward * (0.3 + 2.7 * i / 99.0);
      res = _backwardPDE.runBackwardsPDESolver(localVolatility, forwardCurve, data, option);
      double vol = 0;
      try {
        final double vol1 = BlackFormulaRepository.impliedVolatility(res.getFunctionValue(xIndex), f1, k, expiry, isCall);
        final double vol2 = BlackFormulaRepository.impliedVolatility(res.getFunctionValue(xIndex + 1), f2, k, expiry, isCall);
        vol = w * vol1 + (1 - w) * vol2;
      } catch (final Exception e) {
      }
      final double modelDelta = w * res.getFirstSpatialDerivative(xIndex) + (1 - w) * res.getFirstSpatialDerivative(xIndex + 1);
      final double modelGamma = w * res.getSecondSpatialDerivative(xIndex) + (1 - w) * res.getSecondSpatialDerivative(xIndex + 1);
    }
    return null;
  }
}
