/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
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
public class GaussLaguerreOrthogonalPolynomialGeneratingFunction extends OrthogonalPolynomialGeneratingFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(GaussLaguerreOrthogonalPolynomialGeneratingFunction.class);
  private static final double EPS = 1e-6;
  private static final Function1D<Double, Double> LOG_GAMMA_FUNCTION = new NaturalLogGammaFunction();
  private final double _alpha;

  public GaussLaguerreOrthogonalPolynomialGeneratingFunction() {
    this(0);
  }

  public GaussLaguerreOrthogonalPolynomialGeneratingFunction(final double alpha) {
    super();
    _alpha = alpha;
  }

  public GaussLaguerreOrthogonalPolynomialGeneratingFunction(final double alpha, final int maxIter) {
    super(maxIter);
    _alpha = alpha;
  }

  @Override
  public GaussianQuadratureFunction generate(final int n, final Double... parameters) {
    if (parameters != null) {
      s_logger.info("Limits for this integration method are 0 and +infinity; ignoring bounds");
    }
    return generate(n);
  }

  public GaussianQuadratureFunction generate(final int n) {
    ArgumentChecker.notNegativeOrZero(n, "n");

    double z = 0, z1 = 0, p1 = 0, p2 = 0, p3 = 0, pp = 0;
    int ai, j;
    final int max = getMaxIterations();
    final double[] x = new double[n];
    final double[] w = new double[n];
    for (int i = 0; i < n; i++) {
      if (i == 0) {
        z = (1 + _alpha) * (3 + 0.92 * _alpha) / (1 + 2.4 * n + 1.8 * _alpha);
      } else if (i == 1) {
        z += (15 + 6.25 * _alpha) / (1 + 0.9 * _alpha + 2.5 * n);
      } else {
        ai = i - 1;
        z += ((1 + 2.55 * ai) / (1.9 * ai) + 1.26 * ai * _alpha / (1 + 3.5 * ai)) * (z - x[i - 2]) / (1 + 0.3 * _alpha);
      }
      for (j = 0; j < max; j++) {
        p1 = 1.;
        p2 = 0.;
        for (int k = 0; k < n; k++) {
          p3 = p2;
          p2 = p1;
          p1 = ((2 * k + 1 + _alpha - z) * p2 - (k + _alpha) * p3) / (k + 1);
        }
        pp = (n * p1 - (n + _alpha) * p2) / z;
        z1 = z;
        z = z1 - p1 / pp;
        if (Math.abs(z - z1) < EPS) {
          break;
        }
      }
      if (j == max) {
        throw new MathException("Could not converge in " + max + " iterations");
      }
      x[i] = z;
      w[i] = -Math.exp(LOG_GAMMA_FUNCTION.evaluate(_alpha + n) - LOG_GAMMA_FUNCTION.evaluate(Double.valueOf(n))) / (pp * n * p2);
    }
    return new GaussianQuadratureFunction(x, w);
  }
}
