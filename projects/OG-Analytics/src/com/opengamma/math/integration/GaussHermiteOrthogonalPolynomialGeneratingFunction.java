/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.math.MathException;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class GaussHermiteOrthogonalPolynomialGeneratingFunction extends OrthogonalPolynomialGeneratingFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(GaussHermiteOrthogonalPolynomialGeneratingFunction.class);
  private static final double EPS = 1e-12;
  private static final double POWER_OF_PI = Math.pow(Math.PI, -0.25);

  public GaussHermiteOrthogonalPolynomialGeneratingFunction() {
    super();
  }

  public GaussHermiteOrthogonalPolynomialGeneratingFunction(final int maxIter) {
    super(maxIter);
  }

  @Override
  public GaussianQuadratureFunction generate(final int n, final Double... parameters) {
    if (parameters != null) {
      s_logger.info("Limits for this integration method are +/-infinity; ignoring bounds");
    }
    return generate(n);
  }

  private GaussianQuadratureFunction generate(final int n) {
    ArgumentChecker.notNegativeOrZero(n, "n");
    final double[] x = new double[n];
    final double[] w = new double[n];
    int m, j = 0;
    final int max = getMaxIterations();
    double p1, p2, p3, pp = 0, z = 0, z1;
    m = (n + 1) / 2;
    for (int i = 0; i < m; i++) {
      if (i == 0) {
        z = Math.sqrt(2 * n + 1.) - 1.85575 * Math.pow(2 * n + 1., -0.16667);
      } else if (i == 1) {
        z -= 1.14 * Math.pow(n, 0.426) / z;
      } else if (i == 2) {
        z = 1.86 * z + 0.86 * x[0];
      } else if (i == 3) {
        z = 1.91 * z + 0.91 * x[1];
      } else {
        z = 2 * z + x[i - 2];
      }
      for (j = 0; j < max; j++) {
        p1 = POWER_OF_PI;
        p2 = 0;
        for (int k = 0; k < n; k++) {
          p3 = p2;
          p2 = p1;
          p1 = z * Math.sqrt(2. / (k + 1)) * p2 - Math.sqrt(Double.valueOf(k) / (k + 1)) * p3;
        }
        pp = Math.sqrt(2. * n) * p2;
        z1 = z;
        z = z1 - p1 / pp;
        if (Math.abs(z - z1) < EPS) {
          break;
        }
      }
      if (j == max) {
        throw new MathException("Could not converge in " + max + " iterations");
      }
      x[i] = -z;
      x[n - 1 - i] = z;
      w[i] = 2. / (pp * pp);
      w[n - 1 - i] = w[i];
    }
    return new GaussianQuadratureFunction(x, w);
  }
}
