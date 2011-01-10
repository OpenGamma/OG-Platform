/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class GaussianQuadratureIntegrator1D extends Integrator1D<Double, Function1D<Double, Double>, Double> {
  private final int _n;
  private final OrthogonalPolynomialGeneratingFunction _generator;

  public GaussianQuadratureIntegrator1D(final int n, final OrthogonalPolynomialGeneratingFunction generator) {
    ArgumentChecker.notNegativeOrZero(n, "number of divisions");
    Validate.notNull(generator, "generating function");
    _n = n;
    _generator = generator;
  }

  @Override
  public Double integrate(final Function1D<Double, Double> function, final Double lower, final Double upper) {
    Validate.notNull(function);
    Validate.notNull(lower);
    Validate.notNull(upper);
    final GaussianQuadratureFunction quadrature = _generator.generate(_n, new Double[] {lower, upper});
    final double[] ordinals = quadrature.evaluate(function);
    final double[] weights = quadrature.getWeights();
    double sum = 0;
    for (int i = 0; i < ordinals.length; i++) {
      sum += ordinals[i] * weights[i];
    }
    return sum;
  }
}
