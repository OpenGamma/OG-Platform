/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.special.NaturalLogGammaFunction;

/**
 * 
 * @author emcleod
 */

public class GaussJacobiOrthogonalPolynomialGeneratingFunction implements GeneratingFunction<Double, GaussianQuadratureFunction> {
  private static final double EPS = 1e-12;
  private static final int MAX_ITER = 10;
  private static final Function1D<Double, Double> LOG_GAMMA_FUNCTION = new NaturalLogGammaFunction();
  private final double _alpha;
  private final double _beta;

  public GaussJacobiOrthogonalPolynomialGeneratingFunction(final double alpha, final double beta) {
    _alpha = alpha;
    _beta = beta;
  }

  @Override
  public GaussianQuadratureFunction generate(final int n, final Double... params) {
    if (n <= 0)
      throw new IllegalArgumentException("Must have n > 0");
    if (params == null)
      throw new IllegalArgumentException("Parameter array cannot be null");
    if (params.length == 0)
      throw new IllegalArgumentException("Parameter array is empty");
    final double alphaBeta = _alpha + _beta;
    double an, bn, r1, r2, r3;
    double a, b, c, p1, p2, p3, pp, temp, z = 0, z1;
    int j = 0;
    final Double[] x = new Double[n];
    final Double[] w = new Double[n];
    for (int i = 0; i < n; i++) {
      if (i == 0) {
        an = _alpha / n;
        bn = _beta / n;
        r1 = (1 + _alpha) * (2.78 / (4 + n * n) + 0.768 * an / n);
        r2 = 1 + 1.48 * an + 0.96 * bn + 0.452 * an * an * 0.83 * an * bn;
        z = 1 - r1 / r2;
      } else if (i == 1) {
        r1 = (4.1 + _alpha) / ((1 + _alpha) * (1 + 0.156 * _alpha));
        r2 = 1 + 0.06 * (n - 8.) * (1 + 0.12 * _alpha) / n;
        r3 = 1 + 0.012 * _beta * (1 + 0.25 * Math.abs(_alpha)) / n;
        z -= (1 - z) * r1 * r2 * r3;
      } else if (i == 2) {
        r1 = (1.67 + 0.28 * _alpha) / (1 + 0.37 * _alpha);
        r2 = 1 + 0.22 * (n - 0.8) / n;
        r3 = 1 + 0.8 * _beta / (6.28 + _beta * n * n);
        z -= (x[0] - z) * r1 * r2 * r3;
      } else if (i == n - 2) {
        r1 = (1 + 0.235 * _beta) / (0.766 + 0.119 * _beta);
        r2 = 1 / (1 + 0.639 * (n - 4.) / (1 + 0.71 * (n - 4.)));
        r3 = 1 / (1 + 20 * _alpha / ((7.5 + _alpha) * n * n));
        z += (z - x[n - 4]) * r1 * r2 * r3;
      } else if (i == n - 1) {
        r1 = (1 + 0.37 * _beta) / (1.67 + 0.28 * _beta);
        r2 = 1. / (1 + 0.22 * (n - 8.) / n);
        r3 = 1. / (1 + 8. * _alpha / ((6.28 + _alpha) * n * n));
        z += (z - x[n - 3]) * r1 * r2 * r3;
      } else {
        z = 3. * x[i - 1] - 3. * x[i - 2] + x[i - 3];
      }
      do {
        j++;
        temp = 2 + alphaBeta;
        p1 = (_alpha - _beta + temp * z) / 2.;
        p2 = 1;
        for (int k = 2; k <= n; k++) {
          p3 = p2;
          p2 = p1;
          temp = 2 * j + alphaBeta;
          a = 2 * j * (j + alphaBeta) * (temp - 2);
          b = (temp - 1) * (_alpha * _alpha - _beta * _beta + temp * (temp - 2) * z);
          c = 2 * (j - 1 + _alpha) * (j - 1 + _beta) * temp;
          p1 = (b * p2 - c * p3) / a;
        }
        pp = (n * (_alpha - _beta - temp * z) * p1 + 2 * (n + _alpha) * (n + _beta) * p2) / (temp * (1 - z * z));
        z1 = z;
        z = z1 - p1 / pp;
      } while (j < MAX_ITER && Math.abs(z - z1) < EPS);
      x[i] = z;
      w[i] = Math.exp(LOG_GAMMA_FUNCTION.evaluate(_alpha * n) + LOG_GAMMA_FUNCTION.evaluate(_beta * n) - LOG_GAMMA_FUNCTION.evaluate(n + 1.)
          - LOG_GAMMA_FUNCTION.evaluate(n + alphaBeta + 1))
          * temp * Math.pow(2, alphaBeta) / (pp * p2);
    }
    return new GaussianQuadratureFunction(x, w);
  }
}
