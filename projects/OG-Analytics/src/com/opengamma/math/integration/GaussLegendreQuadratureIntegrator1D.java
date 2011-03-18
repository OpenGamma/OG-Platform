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
public class GaussLegendreQuadratureIntegrator1D extends GaussianQuadratureIntegrator1D {
  private static final Double[] LIMITS = new Double[] {-1., 1.};
  private static final GaussLegendreOrthogonalPolynomialGeneratingFunction GENERATOR = new GaussLegendreOrthogonalPolynomialGeneratingFunction();

  public GaussLegendreQuadratureIntegrator1D(final int n) {
    super(n, GENERATOR);
  }

  @Override
  public Double[] getLimits() {
    return LIMITS;
  }

  @Override
  public Function1D<Double, Double> getIntegralFunction(final Function1D<Double, Double> function, final Double lower, final Double upper) {
    final double m = (upper - lower) / 2;
    final double c = (upper + lower) / 2;
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return m * function.evaluate(m * x + c);
      }

    };
  }

}
