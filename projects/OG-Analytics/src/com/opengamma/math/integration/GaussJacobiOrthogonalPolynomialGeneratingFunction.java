/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.special.NaturalLogGammaFunction;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class GaussJacobiOrthogonalPolynomialGeneratingFunction extends OrthogonalPolynomialGeneratingFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(GaussJacobiOrthogonalPolynomialGeneratingFunction.class);
  private static final double EPS = 1e-12;
  private static final Function1D<Double, Double> LOG_GAMMA_FUNCTION = new NaturalLogGammaFunction();
  private final double _alpha;
  private final double _beta;

  public GaussJacobiOrthogonalPolynomialGeneratingFunction(final double alpha, final double beta, final int maxIter) {
    super(maxIter);
    _alpha = alpha;
    _beta = beta;
  }

  public GaussJacobiOrthogonalPolynomialGeneratingFunction(final double alpha, final double beta) {
    super();
    _alpha = alpha;
    _beta = beta;
  }

  @Override
  public GaussianQuadratureFunction generate(final int n, final Double... parameters) {
    if (parameters != null) {
      s_logger.info("Limits for this integration method are -1 and 1; ignoring bounds");
    }
    return generate(n);
  }

  private GaussianQuadratureFunction generate(final int n) {
    ArgumentChecker.notNegativeOrZero(n, "n");
    final double alphaBeta = _alpha + _beta;
    double an, bn, r1, r2, r3;
    double a, b, c;
    double z1;
    double z = 0, pp = 0, p1 = 0, p2 = 0, p3 = 0;
    double alphaBeta2 = 2 + alphaBeta;
    int j;
    final int max = getMaxIterations();
    final double[] x = new double[n];
    final double[] w = new double[n];
    for (int i = 0; i < n; i++) {
      if (i == 0) {
        an = _alpha / n;
        bn = _beta / n;
        r1 = (1 + _alpha) * (2.78 / (4 + n * n) + 0.768 * an / n);
        r2 = 1 + 1.48 * an + 0.96 * bn + 0.452 * an * an + 0.83 * an * bn;
        z = 1 - r1 / r2;
      } else if (i == 1) {
        r1 = (4.1 + _alpha) / ((1 + _alpha) * (1 + 0.156 * _alpha));
        r2 = 1 + 0.06 * (n - 8) * (1 + 0.12 * _alpha) / n;
        r3 = 1 + 0.012 * _beta * (1 + 0.25 * Math.abs(_alpha)) / n;
        z -= (1 - z) * r1 * r2 * r3;
      } else if (i == 2) {
        r1 = (1.67 + 0.28 * _alpha) / (1 + 0.37 * _alpha);
        r2 = 1 + 0.22 * (n - 8) / n;
        r3 = 1 + 8 * _beta / ((6.28 + _beta) * n * n);
        z -= (x[0] - z) * r1 * r2 * r3;
      } else if (i == n - 2) {
        r1 = (1 + 0.235 * _beta) / (0.766 + 0.119 * _beta);
        r2 = 1. / (1 + 0.639 * (n - 4.) / (1 + 0.71 * (n - 4.)));
        r3 = 1. / (1 + 20 * _alpha / ((7.5 + _alpha) * n * n));
        z += (z - x[n - 4]) * r1 * r2 * r3;
      } else if (i == n - 1) {
        r1 = (1 + 0.37 * _beta) / (1.67 + 0.28 * _beta);
        r2 = 1. / (1 + 0.22 * (n - 8.) / n);
        r3 = 1. / (1 + 8. * _alpha / ((6.28 + _alpha) * n * n));
        z += (z - x[n - 3]) * r1 * r2 * r3;
      } else {
        z = 3. * x[i - 1] - 3. * x[i - 2] + x[i - 3];
      }
      for (j = 0; j < max; j++) {
        alphaBeta2 = 2 + alphaBeta;
        p1 = (_alpha - _beta + alphaBeta2 * z) / 2.;
        p2 = 1;
        for (int k = 2; k <= n; k++) {
          p3 = p2;
          p2 = p1;
          alphaBeta2 = 2 * k + alphaBeta;
          a = 2 * k * (k + alphaBeta) * (alphaBeta2 - 2);
          b = (alphaBeta2 - 1) * (_alpha * _alpha - _beta * _beta + alphaBeta2 * (alphaBeta2 - 2) * z);
          c = 2 * (k - 1 + _alpha) * (k - 1 + _beta) * alphaBeta2;
          p1 = (b * p2 - c * p3) / a;
        }
        pp = (n * (_alpha - _beta - alphaBeta2 * z) * p1 + 2 * (n + _alpha) * (n + _beta) * p2) / (alphaBeta2 * (1 - z * z));
        z1 = z;
        z = z1 - p1 / pp;
        if (Math.abs(z - z1) <= EPS) {
          break;
        }
      }
      if (j == max) {
        throw new MathException("Could not converge in " + max + " iterations");
      }
      x[i] = z;
      w[i] = Math.exp(LOG_GAMMA_FUNCTION.evaluate(_alpha + n) + LOG_GAMMA_FUNCTION.evaluate(_beta + n) - LOG_GAMMA_FUNCTION.evaluate(n + 1.) - LOG_GAMMA_FUNCTION.evaluate(n + alphaBeta + 1))
          * alphaBeta2 * Math.pow(2, alphaBeta) / (pp * p2);
    }
    return new GaussianQuadratureFunction(x, w);
  }
}
