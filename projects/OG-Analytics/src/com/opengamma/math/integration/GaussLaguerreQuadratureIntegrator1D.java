/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class GaussLaguerreQuadratureIntegrator1D extends GaussianQuadratureIntegrator1D {
  private static final Double[] LIMITS = new Double[] {0., Double.POSITIVE_INFINITY};

  public GaussLaguerreQuadratureIntegrator1D(final int n) {
    super(n, new GaussLaguerreOrthogonalPolynomialGeneratingFunction());
  }

  public GaussLaguerreQuadratureIntegrator1D(final int n, final double alpha) {
    super(n, new GaussLaguerreOrthogonalPolynomialGeneratingFunction(alpha));
  }

  @Override
  public Double[] getLimits() {
    return LIMITS;
  }

  @Override
  public Function1D<Double, Double> getIntegralFunction(final Function1D<Double, Double> function, final Double lower, final Double upper) {
    if (lower.equals(LIMITS[0]) && upper.equals(LIMITS[1])) {
      return new Function1D<Double, Double>() {

        @Override
        public Double evaluate(final Double x) {
          return function.evaluate(x) * Math.exp(x);
        }

      };
    }
    throw new UnsupportedOperationException("Limits for Gauss-Laguerre integration are 0 and +infinity");
  }
}
