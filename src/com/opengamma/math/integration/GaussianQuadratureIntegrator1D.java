/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 */

public class GaussianQuadratureIntegrator1D extends Integrator1D<Double, Function1D<Double, Double>, Double> {
  private final int _n;
  private final OrthogonalPolynomialGeneratingFunction _generator;

  public GaussianQuadratureIntegrator1D(final int n, final OrthogonalPolynomialGeneratingFunction generator) {
    if (n <= 0)
      throw new IllegalArgumentException("Number of divisions was less than one");
    if (generator == null)
      throw new IllegalArgumentException("Generating function was null");
    _n = n;
    _generator = generator;
  }

  @Override
  public Double integrate(final Function1D<Double, Double> function, final Double lower, final Double upper) {
    if (function == null)
      throw new IllegalArgumentException("Function was null");
    if (lower == null)
      throw new IllegalArgumentException("Lower bound was null");
    if (upper == null)
      throw new IllegalArgumentException("Upper bound was null");
    final GaussianQuadratureFunction quadrature = _generator.generate(_n, new Double[] { lower, upper });
    final Double[] ordinals = quadrature.evaluate(function);
    final Double[] weights = quadrature.getWeights();
    double sum = 0;
    for (int i = 0; i < ordinals.length; i++) {
      sum += ordinals[i] * weights[i];
    }
    return sum;
  }
}
