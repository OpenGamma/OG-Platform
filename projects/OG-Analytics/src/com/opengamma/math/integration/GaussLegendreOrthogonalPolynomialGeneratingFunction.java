/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import org.apache.commons.lang.Validate;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class GaussLegendreOrthogonalPolynomialGeneratingFunction extends OrthogonalPolynomialGeneratingFunction {
  private static final double EPS = 1e-12;

  public GaussLegendreOrthogonalPolynomialGeneratingFunction() {
    super();
  }

  public GaussLegendreOrthogonalPolynomialGeneratingFunction(final int maxIter) {
    super(maxIter);
  }

  @Override
  public GaussianQuadratureFunction generate(final int n, final Double... parameters) {
    ArgumentChecker.notNegativeOrZero(n, "n");
    Validate.notNull(parameters, "parameters");
    ArgumentChecker.notEmpty(parameters, "parameters");
    final double lower = parameters[0];
    final double upper = parameters[1];
    final int m = (n + 1) / 2;
    final double xm = (upper + lower) / 2.;
    final double xl = (upper - lower) / 2.;
    double z, z1, p1, p2, p3, pp;
    final double[] x = new double[n];
    final double[] w = new double[n];
    for (int i = 0; i < m; i++) {
      z = Math.cos(Math.PI * (i + 0.75) / (n + 0.5));
      do {
        p1 = 1;
        p2 = 0;
        for (int j = 0; j < n; j++) {
          p3 = p2;
          p2 = p1;
          p1 = ((2.0 * j + 1) * z * p2 - j * p3) / (j + 1);
        }
        pp = n * (z * p1 - p2) / (z * z - 1);
        z1 = z;
        z = z1 - p1 / pp;
      } while (Math.abs(z - z1) > EPS);
      x[i] = xm - xl * z;
      x[n - i - 1] = xm + xl * z;
      w[i] = 2 * xl / ((1 - z * z) * pp * pp);
      w[n - i - 1] = w[i];
    }
    return new GaussianQuadratureFunction(x, w);
  }
}
