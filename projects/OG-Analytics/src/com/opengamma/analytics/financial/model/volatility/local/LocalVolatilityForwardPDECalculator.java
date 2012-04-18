/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDEDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.PDETerminalResults1D;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;

/**
 * 
 */
public class LocalVolatilityForwardPDECalculator extends LocalVolatilityPDECalculator {
  //TODO add max moneyness multiplier
  private static final double MAX_MONEYNESS = 3.5;
  private final int _nTimeSteps;
  private final int _nSpaceSteps;
  private final double _timeMeshLambda;
  private final double _spaceMeshBunching;

  public LocalVolatilityForwardPDECalculator(final double theta, final int nTimeSteps, final int nSpaceSteps, final double timeMeshLambda, final double spaceMeshBunching) {
    super(theta);
    _nTimeSteps = nTimeSteps;
    _nSpaceSteps = nSpaceSteps;
    _timeMeshLambda = timeMeshLambda;
    _spaceMeshBunching = spaceMeshBunching;
  }

  @Override
  public PDETerminalResults1D runPDESolver(final LocalVolatilitySurfaceMoneyness localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option) {
    final boolean isCall = option.isCall();
    final double expiry = option.getTimeToExpiry();
    final PDEGrid1D grid = getGrid(getTimeMesh(expiry), getSpaceMesh(MAX_MONEYNESS));
    final BoundaryCondition lower = getLowerBoundaryCondition(option);
    final BoundaryCondition upper = getUpperBoundaryCondition(option, MAX_MONEYNESS);
    final ConvectionDiffusionPDEDataBundle db = getProvider().getForwardLocalVol(localVolatility, isCall);
    return (PDETerminalResults1D) getSolver().solve(db, grid, lower, upper);
  }

  @Override
  public PDETerminalResults1D runPDESolver(final LocalVolatilitySurfaceStrike localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option) {
    return runPDESolver(LocalVolatilitySurfaceConverter.toMoneynessSurface(localVolatility, forwardCurve), forwardCurve, option);
  }

  private MeshingFunction getTimeMesh(final double maxTime) {
    return new ExponentialMeshing(0.0, maxTime, _nTimeSteps, _timeMeshLambda);
  }

  private MeshingFunction getSpaceMesh(final double maxSpace) {
    return new HyperbolicMeshing(0.0, maxSpace, 1, _nSpaceSteps, _spaceMeshBunching);
  }
}
