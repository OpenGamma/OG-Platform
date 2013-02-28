/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.DirichletBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ExponentialMeshing;
import com.opengamma.analytics.financial.model.finitedifference.HyperbolicMeshing;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.NeumannBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.PDE1DDataBundle;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.PDETerminalResults1D;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.function.Function1D;

/**
 * 
 */
public class LocalVolatilityForwardPDECalculator extends LocalVolatilityPDECalculator {
  private final int _nTimeSteps;
  private final int _nSpaceSteps;
  private final double _timeMeshLambda;
  private final double _spaceMeshBunching;
  private final double _maxProxyDelta;
  private final double _centreMoneyness;

  public LocalVolatilityForwardPDECalculator(final double theta, final int nTimeSteps, final int nSpaceSteps, final double timeMeshLambda, final double spaceMeshBunching,
      final double maxProxyDelta, final double centreMoneyness) {
    super(theta);
    _nTimeSteps = nTimeSteps;
    _nSpaceSteps = nSpaceSteps;
    _timeMeshLambda = timeMeshLambda;
    _spaceMeshBunching = spaceMeshBunching;
    _maxProxyDelta = maxProxyDelta;
    _centreMoneyness = centreMoneyness;
  }

  @Override
  public PDETerminalResults1D runPDESolver(final LocalVolatilitySurfaceMoneyness localVolatility, final EuropeanVanillaOption option) {
    final boolean isCall = option.isCall();
    final double expiry = option.getTimeToExpiry();
    final double minMoneyness = Math.exp(-_maxProxyDelta * Math.sqrt(expiry));
    final double maxMoneyness = 1.0 / minMoneyness;
    final PDEGrid1D grid = getGrid(getTimeMesh(expiry), getSpaceMesh(minMoneyness, maxMoneyness));
    final BoundaryCondition lower = getLowerBoundaryCondition(option, minMoneyness);
    final BoundaryCondition upper = getUpperBoundaryCondition(option, maxMoneyness);
    final ConvectionDiffusionPDE1DCoefficients pde = getPDEProvider().getForwardLocalVol(localVolatility);
    final Function1D<Double, Double> intCond = getInitialConditionProvider().getForwardCallPut(isCall);
    final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db = new PDE1DDataBundle<>(pde, intCond, lower, upper, grid);
    return (PDETerminalResults1D) getSolver().solve(db);
  }

  @Override
  public PDETerminalResults1D runPDESolver(final LocalVolatilitySurfaceStrike localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option) {
    final boolean isCall = option.isCall();
    final double expiry = option.getTimeToExpiry();
    final double minMoneyness = Math.exp(-_maxProxyDelta * Math.sqrt(expiry));
    final double maxMoneyness = 1.0 / minMoneyness;
    final PDEGrid1D grid = getGrid(getTimeMesh(expiry), getSpaceMesh(minMoneyness, maxMoneyness));
    final BoundaryCondition lower = getLowerBoundaryCondition(option, minMoneyness);
    final BoundaryCondition upper = getUpperBoundaryCondition(option, maxMoneyness);
    final ConvectionDiffusionPDE1DCoefficients pde = getPDEProvider().getForwardLocalVol(forwardCurve, localVolatility);
    final Function1D<Double, Double> intCond = getInitialConditionProvider().getForwardCallPut(isCall);
    final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db = new PDE1DDataBundle<>(pde, intCond, lower, upper, grid);
    return (PDETerminalResults1D) getSolver().solve(db);
  }

  public int getNTimeSteps() {
    return _nTimeSteps;
  }

  public int getNSpaceSteps() {
    return _nSpaceSteps;
  }

  public double getTimeMeshLambda() {
    return _timeMeshLambda;
  }

  public double getSpaceMeshBunching() {
    return _spaceMeshBunching;
  }

  public double getMaxProxyDelta() {
    return _maxProxyDelta;
  }

  public double getCentreMoneyness() {
    return _centreMoneyness;
  }

  private MeshingFunction getTimeMesh(final double maxTime) {
    return new ExponentialMeshing(0.0, maxTime, _nTimeSteps, _timeMeshLambda);
  }

  private MeshingFunction getSpaceMesh(final double minMoneyness, final double maxMoneyness) {
    return new HyperbolicMeshing(minMoneyness, maxMoneyness, _centreMoneyness, _nSpaceSteps, _spaceMeshBunching);
  }

  private BoundaryCondition getLowerBoundaryCondition(final EuropeanVanillaOption option, final double minMoneyness) {
    //call option with strike zero is worth the forward, while a put is worthless
    return option.isCall() ? new DirichletBoundaryCondition(1.0 - minMoneyness, minMoneyness) : new DirichletBoundaryCondition(0.0, minMoneyness);
  }

  private BoundaryCondition getUpperBoundaryCondition(final EuropeanVanillaOption option, final double maxMoneyness) {
    //call option with strike zero is worth the forward, while a put is worthless
    return option.isCall() ? new DirichletBoundaryCondition(0.0, maxMoneyness) : new NeumannBoundaryCondition(1.0, maxMoneyness, false);
  }

}
