package com.opengamma.math.integration;

/**
 * 
 * @author emcleod
 * 
 */

public class GaussHermiteOrthogonalPolynomialGenerationFunction implements GeneratingFunction<Double, GaussianQuadratureFunction> {
  private static final int MAX_ITER = 10;
  private static final double EPS = 1e-12;
  private static final double POWER_OF_PI = Math.pow(Math.PI, -0.25);

  @Override
  public GaussianQuadratureFunction generate(final int n, final Double... params) {
    int j;
    double z = 0, z1, p1, p2, p3, pp;
    final int m = (n + 1) / 2;
    final Double[] x = new Double[n];
    final Double[] w = new Double[n];
    for (int i = 0; i < m; i++) {
      if (i == 0) {
        z = Math.sqrt(2 * n + 1.) - 1.85575 * Math.pow(2 * n + 1., -0.1666667);
      } else if (i == 1) {
        z -= 1.14 * Math.pow(n, 0.426) / z;
      } else if (i == 2) {
        z = 1.86 * z - 0.86 * x[0];
      } else if (i == 3) {
        z = 1.91 * z - 0.91 * x[1];
      } else {
        z = 2 * z - x[i - 2];
      }
      j = 0;
      do {
        j++;
        p1 = POWER_OF_PI;
        p2 = 0.;
        for (int k = 0; k < n; k++) {
          p3 = p2;
          p2 = p1;
          p1 = z * Math.sqrt(2. / (j + 1)) * p2 - Math.sqrt((double) j / (j + 1)) * p3;
        }
        pp = Math.sqrt(2. * n) * p2;
        z1 = z;
        z = z1 - p1 / pp;
      } while (Math.abs(z - z1) > EPS && j < MAX_ITER);
      x[i] = z;
      x[n - i - 1] = -z;
      w[i] = 2. / (pp * pp);
      w[n - i - 1] = w[i];
    }
    return new GaussianQuadratureFunction(x, w);
  }
}
