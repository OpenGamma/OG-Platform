/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import com.opengamma.analytics.financial.model.finitedifference.BoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.DirichletBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.NeumannBoundaryCondition;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.PDETerminalResults1D;
import com.opengamma.analytics.financial.model.finitedifference.ThetaMethodFiniteDifference;
import com.opengamma.analytics.financial.model.finitedifference.applications.PDEDataBundleProvider;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;

/**
 * 
 */
public abstract class LocalVolatilityPDECalculator {
  private final PDEDataBundleProvider _provider;
  private final ThetaMethodFiniteDifference _solver;

  public LocalVolatilityPDECalculator(final double theta) {
    _provider = new PDEDataBundleProvider();
    _solver = new ThetaMethodFiniteDifference(theta, false);
  }

  public abstract PDETerminalResults1D runPDESolver(final LocalVolatilitySurfaceMoneyness localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option);

  public abstract PDETerminalResults1D runPDESolver(final LocalVolatilitySurfaceStrike localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option);

  protected PDEGrid1D getGrid(final MeshingFunction timeMesh, final MeshingFunction spaceMesh) {
    return new PDEGrid1D(timeMesh, spaceMesh);
  }

  protected BoundaryCondition getLowerBoundaryCondition(final EuropeanVanillaOption option) {
    //call option with strike zero is worth the forward, while a put is worthless
    return option.isCall() ? new DirichletBoundaryCondition(1.0, 0.0) : new DirichletBoundaryCondition(0.0, 0.0);
  }

  protected BoundaryCondition getUpperBoundaryCondition(final EuropeanVanillaOption option, final double maxSpace) {
    //call option with strike zero is worth the forward, while a put is worthless
    return option.isCall() ? new DirichletBoundaryCondition(0.0, maxSpace) : new NeumannBoundaryCondition(1.0, maxSpace, false);
  }

  protected PDEDataBundleProvider getProvider() {
    return _provider;
  }

  protected ThetaMethodFiniteDifference getSolver() {
    return _solver;
  }
}
