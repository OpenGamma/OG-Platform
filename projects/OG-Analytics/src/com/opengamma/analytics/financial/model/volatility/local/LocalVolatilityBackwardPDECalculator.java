/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDEDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.DirichletBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.DoubleExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.analytics.financial.model.finitedifference.NeumannBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEResults1D;
import com.opengamma.analytics.financial.model.finitedifference.ThetaMethodFiniteDifference;
import com.opengamma.analytics.financial.model.finitedifference.applications.PDEDataBundleProvider;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.StandardSmileSurfaceDataBundle;

/**
 * 
 */
public class LocalVolatilityBackwardPDECalculator {
  private final PDEDataBundleProvider _provider;
  private final ThetaMethodFiniteDifference _solver;
  private final int _nTimeSteps;
  private final int _nYSteps;
  private final double _timeMeshLambda;
  private final double _yMeshBunching;

  public LocalVolatilityBackwardPDECalculator(final double theta, final int nTimeSteps, final int nYSteps, final double timeMeshLambda, final double yMeshBunching) {
    _nTimeSteps = nTimeSteps;
    _nYSteps = nYSteps;
    _timeMeshLambda = timeMeshLambda;
    _yMeshBunching = yMeshBunching;
    _provider = new PDEDataBundleProvider();
    _solver = new ThetaMethodFiniteDifference(theta, true);
  }

  public PDEResults1D runBackwardsPDESolver(final LocalVolatilitySurfaceMoneyness localVolatility, final ForwardCurve forwardCurve,
      final StandardSmileSurfaceDataBundle data, final EuropeanVanillaOption option) {
    final boolean isCall = option.isCall();
    final double expiry = option.getTimeToExpiry();
    final double strike = option.getStrike();
    final double forward = forwardCurve.getForward(expiry);
    final double maxFwd = 3.5 * forward;
    BoundaryCondition lower;
    BoundaryCondition upper;
    if (isCall) {
      lower = new DirichletBoundaryCondition(0.0, 0.0); //call option with strike zero is worth 0
      upper = new NeumannBoundaryCondition(1.0, maxFwd, false);
    } else {
      lower = new DirichletBoundaryCondition(strike, 0.0);
      upper = new NeumannBoundaryCondition(0.0, maxFwd, false);
    }
    final DoubleExponentialMeshing timeMesh = new DoubleExponentialMeshing(0, expiry, expiry / 2, _nTimeSteps, _timeMeshLambda, -_timeMeshLambda);
    final HyperbolicMeshing spaceMesh = new HyperbolicMeshing(0.0, maxFwd, forward, _nYSteps, _yMeshBunching);
    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    final ConvectionDiffusionPDEDataBundle db = _provider.getBackwardsLocalVol(strike, expiry, isCall, localVolatility);
    final PDEResults1D res = _solver.solve(db, grid, lower, upper);
    return res;
  }

  public PDEResults1D runBackwardsPDESolver(final LocalVolatilitySurfaceStrike localVolatility, final ForwardCurve forwardCurve,
      final StandardSmileSurfaceDataBundle data, final EuropeanVanillaOption option) {
    final boolean isCall = option.isCall();
    final double expiry = option.getTimeToExpiry();
    final double strike = option.getStrike();
    final double forward = forwardCurve.getForward(expiry);
    final double maxFwd = 3.5 * forward;
    BoundaryCondition lower;
    BoundaryCondition upper;
    if (isCall) {
      lower = new DirichletBoundaryCondition(0.0, 0.0); //call option with strike zero is worth 0
      upper = new NeumannBoundaryCondition(1.0, maxFwd, false);
    } else {
      lower = new DirichletBoundaryCondition(strike, 0.0);
      upper = new NeumannBoundaryCondition(0.0, maxFwd, false);
    }
    final DoubleExponentialMeshing timeMesh = new DoubleExponentialMeshing(0, expiry, expiry / 2, _nTimeSteps, _timeMeshLambda, -_timeMeshLambda);
    final HyperbolicMeshing spaceMesh = new HyperbolicMeshing(0.0, maxFwd, forward, _nYSteps, _yMeshBunching);
    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    final ConvectionDiffusionPDEDataBundle db = _provider.getBackwardsLocalVol(strike, expiry, isCall, localVolatility, forwardCurve);
    final PDEResults1D res = _solver.solve(db, grid, lower, upper);
    return res;
  }
}
