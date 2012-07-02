/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.local;

import com.opengamma.analytics.financial.model.finitedifference.MeshingFunction;
import com.opengamma.analytics.financial.model.finitedifference.PDEGrid1D;
import com.opengamma.analytics.financial.model.finitedifference.PDETerminalResults1D;
import com.opengamma.analytics.financial.model.finitedifference.ThetaMethodFiniteDifference;
import com.opengamma.analytics.financial.model.finitedifference.applications.InitialConditionsProvider;
import com.opengamma.analytics.financial.model.finitedifference.applications.PDE1DCoefficientsProvider;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;

/**
 * 
 */
public abstract class LocalVolatilityPDECalculator {
  private final PDE1DCoefficientsProvider _pdeProvider;
  private final InitialConditionsProvider _initialCondProvider;
  private final ThetaMethodFiniteDifference _solver;

  public LocalVolatilityPDECalculator(final double theta) {
    _pdeProvider = new PDE1DCoefficientsProvider();
    _initialCondProvider = new InitialConditionsProvider();
    _solver = new ThetaMethodFiniteDifference(theta, false);
  }

  public abstract PDETerminalResults1D runPDESolver(final LocalVolatilitySurfaceMoneyness localVolatility, final EuropeanVanillaOption option);

  public abstract PDETerminalResults1D runPDESolver(final LocalVolatilitySurfaceStrike localVolatility, final ForwardCurve forwardCurve, final EuropeanVanillaOption option);

  protected PDEGrid1D getGrid(final MeshingFunction timeMesh, final MeshingFunction spaceMesh) {
    return new PDEGrid1D(timeMesh, spaceMesh);
  }

  protected PDE1DCoefficientsProvider getPDEProvider() {
    return _pdeProvider;
  }

  protected ThetaMethodFiniteDifference getSolver() {
    return _solver;
  }

  protected InitialConditionsProvider getInitialConditionProvider() {
    return _initialCondProvider;
  }
}
