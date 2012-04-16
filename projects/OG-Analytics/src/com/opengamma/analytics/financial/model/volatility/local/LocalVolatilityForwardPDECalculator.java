/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDEDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.DirichletBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.analytics.financial.model.finitedifference.NeumannBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.PDEFullResults1D;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.ThetaMethodFiniteDifference;
import com.opengamma.analytics.financial.model.finitedifference.applications.PDEDataBundleProvider;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.StandardSmileSurfaceDataBundle;

/**
 * 
 */
public class LocalVolatilityForwardPDECalculator {
  private final PDEDataBundleProvider _provider;
  private final ThetaMethodFiniteDifference _solver;
  private final int _nTimeSteps;
  private final int _nYSteps;
  private final double _timeMeshLambda;
  private final double _yMeshBunching;

  public LocalVolatilityForwardPDECalculator(final double theta, final int nTimeSteps, final int nYSteps, final double timeMeshLambda, final double yMeshBunching) {
    _nTimeSteps = nTimeSteps;
    _nYSteps = nYSteps;
    _timeMeshLambda = timeMeshLambda;
    _yMeshBunching = yMeshBunching;
    _provider = new PDEDataBundleProvider();
    _solver = new ThetaMethodFiniteDifference(theta, true);
  }

  public PDEFullResults1D runForwardPDESolver(final LocalVolatilitySurfaceMoneyness localVolatility, final ForwardCurve forwardCurve,
      final StandardSmileSurfaceDataBundle data, final EuropeanVanillaOption option) {
    final BoundaryCondition lower;
    final BoundaryCondition upper;
    final boolean isCall = option.isCall();
    final double maxTime = data.getExpiries()[data.getNumExpiries() - 1];
    final double maxMoneyness = 3.5;
    if (isCall) {
      //call option with strike zero is worth the forward, while a put is worthless
      lower = new DirichletBoundaryCondition(1.0, 0.0);
      upper = new DirichletBoundaryCondition(0.0, maxMoneyness);
    } else {
      lower = new DirichletBoundaryCondition(0.0, 0.0);
      upper = new NeumannBoundaryCondition(1.0, maxMoneyness, false);
    }
    final ExponentialMeshing timeMesh = new ExponentialMeshing(0.0, maxTime, _nTimeSteps, _timeMeshLambda);
    final HyperbolicMeshing spaceMesh = new HyperbolicMeshing(0.0, maxMoneyness, 1, _nYSteps, _yMeshBunching);
    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    final ConvectionDiffusionPDEDataBundle db = _provider.getForwardLocalVol(localVolatility, isCall);
    final PDEFullResults1D res = (PDEFullResults1D) _solver.solve(db, grid, lower, upper);
    return res;
  }

  public PDEFullResults1D runForwardPDESolver(final LocalVolatilitySurfaceStrike localVolatility, final ForwardCurve forwardCurve,
      final StandardSmileSurfaceDataBundle data, final EuropeanVanillaOption option) {
    final BoundaryCondition lower;
    final BoundaryCondition upper;
    final boolean isCall = option.isCall();
    final double maxTime = data.getExpiries()[data.getNumExpiries() - 1];
    final double maxMoneyness = 3.5;
    if (isCall) {
      //call option with strike zero is worth the forward, while a put is worthless
      lower = new DirichletBoundaryCondition(1.0, 0.0);
      upper = new DirichletBoundaryCondition(0.0, maxMoneyness);
    } else {
      lower = new DirichletBoundaryCondition(0.0, 0.0);
      upper = new NeumannBoundaryCondition(1.0, maxMoneyness, false);
    }
    final ExponentialMeshing timeMesh = new ExponentialMeshing(0.0, maxTime, _nTimeSteps, _timeMeshLambda);
    final HyperbolicMeshing spaceMesh = new HyperbolicMeshing(0.0, maxMoneyness, 1, _nYSteps, _yMeshBunching);
    final PDEGrid1D grid = new PDEGrid1D(timeMesh, spaceMesh);
    final ConvectionDiffusionPDEDataBundle db = _provider.getForwardLocalVol(localVolatility, forwardCurve, isCall);
    final PDEFullResults1D res = (PDEFullResults1D) _solver.solve(db, grid, lower, upper);
    return res;
  }
}
