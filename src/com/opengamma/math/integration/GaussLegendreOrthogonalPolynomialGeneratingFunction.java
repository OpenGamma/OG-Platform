package com.opengamma.math.integration;

import com.opengamma.math.MathException;

/**
 * 
 * @author emcleod
 * 
 */

public class GaussLegendreOrthogonalPolynomialGeneratingFunction implements GeneratingFunction<Double, GaussianQuadratureFunction, MathException> {
  private static final double EPS = 1e-12;

  @Override
  public GaussianQuadratureFunction generate(int n, Double... params) throws MathException {
    double lower = params[0];
    double upper = params[1];
    int m = (n + 1) / 2;
    double xm, xl, z, z1, p1, p2, p3, pp;
    Double[] x = new Double[n];
    Double[] w = new Double[n];
    xm = 0.5 * (upper + lower);
    xl = 0.5 * (upper - lower);
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
