/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import com.opengamma.analytics.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.StandardSmileSurfaceDataBundle;

/**
 * 
 */
public class LocalVolatilityForwardPDEDeltaGammaCalculator {
  private final LocalVolatilityForwardPDECalculator _forwardPDE;

  public LocalVolatilityForwardPDEDeltaGammaCalculator(final double theta, final int nTimeSteps, final int nYSteps, final double timeMeshLambda, final double yMeshBunching) {
    _forwardPDE = new LocalVolatilityForwardPDECalculator(theta, nTimeSteps, nYSteps, timeMeshLambda, yMeshBunching);
  }

  public Object runForwardsPDESolver(final LocalVolatilitySurfaceStrike localVolatility, final ForwardCurve forwardCurve,
      final StandardSmileSurfaceDataBundle data, final EuropeanVanillaOption option) {
    final double strike = option.getStrike();
    final double expiry = option.getTimeToExpiry();
    final boolean isCall = option.isCall();
    final double forward = forwardCurve.getForward(expiry);
    final double shift = 1e-2;
    final double maxForward = 3.5 * forward;
    final double maxMoneyness = 3.5;

    final PDEFullResults1D pdeRes = _forwardPDE.runForwardPDESolver(localVolatility, forwardCurve, data, option);
    final PDEFullResults1D pdeResUp = _forwardPDE.runForwardPDESolver(localVolatility, forwardCurve.withFractionalShift(shift), data, option);
    final PDEFullResults1D pdeResDown = _forwardPDE.runForwardPDESolver(localVolatility, forwardCurve, data, option);
    final int n = pdeRes.getNumberSpaceNodes();
    for (int i = 0; i < n; i++) {
      final double m = pdeRes.getSpaceValue(i);
      if (m > 0.3 && m < 3.0) {
        final double k = m * forward;

        final double mPrice = pdeRes.getFunctionValue(i);
        double impVol = 0;
        try {
          impVol = BlackFormulaRepository.impliedVolatility(mPrice, 1.0, m, expiry, isCall);
        } catch (final Exception e) {
        }

        final double bsDelta = BlackFormulaRepository.delta(forward, k, expiry, impVol, isCall);
        final double bsDualDelta = BlackFormulaRepository.dualDelta(forward, k, expiry, impVol, isCall);
        final double bsGamma = BlackFormulaRepository.gamma(forward, k, expiry, impVol);
        final double bsDualGamma = BlackFormulaRepository.dualGamma(forward, k, expiry, impVol);

        final double modelDD = pdeRes.getFirstSpatialDerivative(i);
        final double fixedSurfaceDelta = mPrice - m * modelDD; //i.e. the delta if the moneyness parameterised local vol surface was invariant to forward
        final double surfaceDelta = (pdeResUp.getFunctionValue(i) - pdeResDown.getFunctionValue(i)) / 2 / forward / shift;
        final double modelDelta = fixedSurfaceDelta + forward * surfaceDelta;

        final double modelDG = pdeRes.getSecondSpatialDerivative(i) / forward;
        final double crossGamma = (pdeResUp.getFirstSpatialDerivative(i) - pdeResDown.getFirstSpatialDerivative(i)) / 2 / forward / shift;
        final double surfaceGamma = (pdeResUp.getFunctionValue(i) + pdeResDown.getFunctionValue(i) - 2 * pdeRes.getFunctionValue(i)) / forward / shift / shift;
        final double modelGamma = 2 * surfaceDelta + surfaceGamma - 2 * m * crossGamma + m * m * modelDG;
      }
    }
    return null;
  }
}
