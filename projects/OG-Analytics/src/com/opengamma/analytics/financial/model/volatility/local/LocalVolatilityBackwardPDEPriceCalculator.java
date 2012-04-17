/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.NeumannBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.PDEResults1D;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.StandardSmileSurfaceDataBundle;

/**
 * 
 */
public class LocalVolatilityBackwardPDEPriceCalculator {
  private final int _nYSteps;
  private final LocalVolatilityBackwardPDECalculator _backwardPDE;

  public LocalVolatilityBackwardPDEPriceCalculator(final double theta, final int nTimeSteps, final int nYSteps, final double timeMeshLambda, final double yMeshBunching) {
    _backwardPDE = new LocalVolatilityBackwardPDECalculator(theta, nTimeSteps, nYSteps, timeMeshLambda, yMeshBunching);
    _nYSteps = nYSteps;
  }

  public Object runBackwardsPDESolver(final LocalVolatilitySurfaceStrike localVolatility, final ForwardCurve forwardCurve, final YieldAndDiscountCurve yieldCurve,
      final StandardSmileSurfaceDataBundle data, final EuropeanVanillaOption option) {
    final double expiry = option.getTimeToExpiry();
    final double strike = option.getStrike();
    final boolean isCall = option.isCall();
    final double forward = forwardCurve.getForward(expiry);
    final double maxFwd = 3.5 * forward;
    final PDEResults1D res = _backwardPDE.runBackwardsPDESolver(localVolatility, forwardCurve, data, option);
    final int n = res.getGrid().getNumSpaceNodes();
    BoundaryCondition upper;
    if (isCall) {
      upper = new NeumannBoundaryCondition(1.0, maxFwd, false);
    } else {
      upper = new NeumannBoundaryCondition(0.0, maxFwd, false);
    }
    final int index = (int) (_nYSteps * forward / upper.getLevel());
    final double df = yieldCurve.getDiscountFactor(expiry);
    final double price = df * res.getFunctionValue(index);
    return null;
  }
}
