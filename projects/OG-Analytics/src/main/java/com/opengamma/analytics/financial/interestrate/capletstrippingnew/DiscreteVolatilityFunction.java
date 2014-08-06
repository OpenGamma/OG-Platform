/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import com.opengamma.analytics.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * abstraction for a model that takes a set of parameters and returns (Black) volatilities at specified points (in expiry-strike space)
 * If a continuous volatility surface is required, use {@link VolatilitySurfaceProvider} instead.
 */
public abstract class DiscreteVolatilityFunction extends VectorFunction {

  private static final VectorFieldFirstOrderDifferentiator DIFF = new VectorFieldFirstOrderDifferentiator();

  private final Function1D<DoubleMatrix1D, DoubleMatrix2D> _fdJac = DIFF.differentiate(this);

  /**
   * Evaluate the Jacobian using finite difference. This is used for testing when {@link evaluateJacobian} has
   * been overridden with an analytic method 
   * @param x set of model parameters
   * @return The Jacobian (matrix of sensitivities of caplet volatilities to model parameters)
   */
  protected DoubleMatrix2D evaluateJacobianViaFD(final DoubleMatrix1D x) {
    return _fdJac.evaluate(x);
  }

}
