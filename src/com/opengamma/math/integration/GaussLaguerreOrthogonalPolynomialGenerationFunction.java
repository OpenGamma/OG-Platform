package com.opengamma.math.integration;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.special.NaturalLogGammaFunction;

/**
 * 
 * @author emcleod
 * 
 */

public class GaussLaguerreOrthogonalPolynomialGenerationFunction implements GeneratingFunction<Double, GaussianQuadratureFunction> {
  private static final double EPS = 1e-12;
  private static final int MAX_ITER = 10;
  private static final Function1D<Double, Double> LOG_GAMMA_FUNCTION = new NaturalLogGammaFunction();
  private final double _alpha;

  public GaussLaguerreOrthogonalPolynomialGenerationFunction(final double alpha) {
    _alpha = alpha;
  }

  @Override
  public GaussianQuadratureFunction generate(final int n, final Double... params) {
    double z = 0, z1, p1, p2, p3, pp;
    int ai, j;
    final Double[] x = new Double[n];
    final Double[] w = new Double[n];
    for (int i = 0; i < n; i++) {
      if (i == 0) {
        z = (1 + _alpha) * (3 + 0.92 * _alpha) / (1 + 2.4 * n + 1.8 * _alpha);
      } else if (i == 1) {
        z += (15.0 + 6.25 * _alpha) / (1 + 0.9 * _alpha + 2.5 * n);
      } else {
        ai = i - 1;
        z += ((1 + 2.55 * ai) / (1.9 * ai) + 1.26 * ai * _alpha / (1 + 3.5 * ai)) * (z - x[i - 2]) / (1.0 + 0.3 * _alpha);
      }
      j = 0;
      do {
        j++;
        p1 = 0;
        p2 = 0;
        for (int k = 0; k < n; k++) {
          p3 = p2;
          p2 = p1;
          p1 = ((2 * j + 1 + _alpha - z) * p2 - (j + _alpha) * p3) / (j + 1);
        }
        pp = (n * p1 - (n + _alpha) * p2) / z;
        z1 = z;
        z = z1 - p1 / pp;
        // TODO error message for lack of convergence
      } while (Math.abs(z - z1) > EPS && j < MAX_ITER);
      x[i] = z;
      w[i] = -Math.exp(LOG_GAMMA_FUNCTION.evaluate(_alpha + n) - LOG_GAMMA_FUNCTION.evaluate(Double.valueOf(n))) / (pp * n * p2);
    }
    return new GaussianQuadratureFunction(x, w);
  }
}
