/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDEDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.DoubleExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.PDETerminalResults1D;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;

/**
 * 
 */
public class LocalVolatilityBackwardPDECalculator extends LocalVolatilityPDECalculator {
  //TODO add max moneyness multiplier
  private static final double MAX_MONEYNESS = 3.5;
  private final int _nTimeSteps;
  private final int _nSpaceSteps;
  private final double _timeMeshLambda;
  private final double _spaceMeshBunching;

  public LocalVolatilityBackwardPDECalculator(final double theta, final int nTimeSteps, final int nSpaceSteps, final double timeMeshLambda, final double spaceMeshBunching) {
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
    final double forward = forwardCurve.getForward(expiry);
    final BoundaryCondition lower = getLowerBoundaryCondition(option);
    final BoundaryCondition upper = getUpperBoundaryCondition(option, MAX_MONEYNESS * forward);
    final PDEGrid1D grid = getGrid(getTimeMesh(expiry), getSpaceMesh(MAX_MONEYNESS * forward, expiry / 2));
    final ConvectionDiffusionPDEDataBundle db = getProvider().getForwardLocalVol(localVolatility, isCall);
    return (PDETerminalResults1D) getSolver().solve(db, grid, lower, upper);
  }

  @Override
  public PDETerminalResults1D runPDESolver(final LocalVolatilitySurfaceStrike localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option) {
    return runPDESolver(LocalVolatilitySurfaceConverter.toMoneynessSurface(localVolatility, forwardCurve), forwardCurve, option);
  }

  private MeshingFunction getTimeMesh(final double maxTime) {
    return new DoubleExponentialMeshing(0, maxTime, maxTime / 2, _nTimeSteps, _timeMeshLambda, -_timeMeshLambda);
  }

  private MeshingFunction getSpaceMesh(final double maxSpace, final double heart) {
    return new HyperbolicMeshing(0.0, maxSpace, heart, _nSpaceSteps, _spaceMeshBunching);
  }
}
//  final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
//  final ConvectionDiffusionPDEDataBundle db = _provider.getBackwardsLocalVol(strike, expiry, isCall, localVolatility);
//  return (PDETerminalResults1D) _solver.solve(db, grid, lower, upper);
//}
//
//public PDETerminalResults1D runBackwardsPDESolver(final LocalVolatilitySurfaceStrike localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option) {
//  final boolean isCall = option.isCall();
//  final double expiry = option.getTimeToExpiry();
//  final double strike = option.getStrike();
//  final double forward = forwardCurve.getForward(expiry);
//  final double maxFwd = 3.5 * forward;
//  final BoundaryCondition lower;
//  final BoundaryCondition upper;
//  if (isCall) {
//    lower = new DirichletBoundaryCondition(0.0, 0.0); //call option with strike zero is worth 0
//    upper = new NeumannBoundaryCondition(1.0, maxFwd, false);
//  } else {
//    lower = new DirichletBoundaryCondition(strike, 0.0);
//    upper = new NeumannBoundaryCondition(0.0, maxFwd, false);
//  }
//  final DoubleExponentialMeshing timeMesh = new DoubleExponentialMeshing(0, expiry, expiry / 2, _nTimeSteps, _timeMeshLambda, -_timeMeshLambda);
//  final HyperbolicMeshing spaceMesh = new HyperbolicMeshing(0.0, maxFwd, forward, _nYSteps, _yMeshBunching);
//  final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
//  final ConvectionDiffusionPDEDataBundle db = _provider.getBackwardsLocalVol(strike, expiry, isCall, localVolatility, forwardCurve);
//  return (PDETerminalResults1D) _solver.solve(db, grid, lower, upper);
//}
//}
