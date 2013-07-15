/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.ConvectionDiffusionPDE1DCoefficients;
import com.opengamma.analytics.financial.model.finitedifference.DirichletBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.DoubleExponentialMeshing;
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
public class LocalVolatilityBackwardPDECalculator extends LocalVolatilityPDECalculator {
  private final int _nTimeSteps;
  private final int _nSpaceSteps;
  private final double _timeMeshLambda;
  private final double _spaceMeshBunching;
  private final double _maxMoneyness;

  public LocalVolatilityBackwardPDECalculator(final double theta, final int nTimeSteps, final int nSpaceSteps, final double timeMeshLambda, final double spaceMeshBunching, final double maxMoneyness) {
    super(theta);
    _nTimeSteps = nTimeSteps;
    _nSpaceSteps = nSpaceSteps;
    _timeMeshLambda = timeMeshLambda;
    _spaceMeshBunching = spaceMeshBunching;
    _maxMoneyness = maxMoneyness;
  }

  @Override
  public PDETerminalResults1D runPDESolver(final LocalVolatilitySurfaceMoneyness localVolatility, final EuropeanVanillaOption option) {
    final boolean isCall = option.isCall();
    final double expiry = option.getTimeToExpiry();
    final double forward = localVolatility.getForwardCurve().getForward(expiry);
    final double strike = option.getStrike();
    final double maxForward = forward * _maxMoneyness;
    final BoundaryCondition lower = getLowerBoundaryCondition(option, strike);
    final BoundaryCondition upper = getUpperBoundaryCondition(option, _maxMoneyness * forward);
    final PDEGrid1D grid = getGrid(getTimeMesh(expiry), getSpaceMesh(maxForward, forward));
    final ConvectionDiffusionPDE1DCoefficients pde = getPDEProvider().getBackwardsLocalVol(expiry, localVolatility);
    final Function1D<Double, Double> payoff = getInitialConditionProvider().getEuropeanPayoff(strike, isCall);
    final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db = new PDE1DDataBundle<>(pde, payoff, lower, upper, grid);
    return (PDETerminalResults1D) getSolver().solve(db);
  }

  @Override
  public PDETerminalResults1D runPDESolver(final LocalVolatilitySurfaceStrike localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option) {
    final boolean isCall = option.isCall();
    final double expiry = option.getTimeToExpiry();
    final double forward = forwardCurve.getForward(expiry);
    final double strike = option.getStrike();
    final double maxForward = forward * _maxMoneyness;
    final BoundaryCondition lower = getLowerBoundaryCondition(option, strike);
    final BoundaryCondition upper = getUpperBoundaryCondition(option, expiry);
    final PDEGrid1D grid = getGrid(getTimeMesh(expiry), getSpaceMesh(maxForward, forward));
    final ConvectionDiffusionPDE1DCoefficients pde = getPDEProvider().getBackwardsLocalVol(forwardCurve, expiry, localVolatility);
    final Function1D<Double, Double> payoff = getInitialConditionProvider().getEuropeanPayoff(strike, isCall);
    final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> db = new PDE1DDataBundle<>(pde, payoff, lower, upper, grid);
    return (PDETerminalResults1D) getSolver().solve(db);
  }

  private MeshingFunction getTimeMesh(final double maxTime) {
    return new DoubleExponentialMeshing(0, maxTime, maxTime / 2, _nTimeSteps, _timeMeshLambda, -_timeMeshLambda);
  }

  private MeshingFunction getSpaceMesh(final double maxSpace, final double heart) {
    return new HyperbolicMeshing(0.0, maxSpace, heart, _nSpaceSteps, _spaceMeshBunching);
  }

  private BoundaryCondition getLowerBoundaryCondition(final EuropeanVanillaOption option, final double strike) {
    //call option with strike zero is worth the forward, while a put is worthless
    return option.isCall() ? new DirichletBoundaryCondition(0, 0) : new DirichletBoundaryCondition(strike, 0);
  }

  private BoundaryCondition getUpperBoundaryCondition(final EuropeanVanillaOption option, final double maxForward) {
    //call option with strike zero is worth the forward, while a put is worthless
    return option.isCall() ? new NeumannBoundaryCondition(1.0, maxForward, false) : new NeumannBoundaryCondition(0.0, maxForward, false);
  }

}
