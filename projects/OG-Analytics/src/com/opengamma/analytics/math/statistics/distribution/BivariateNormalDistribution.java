/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.distribution;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

/**
 * The bivariate normal distribution is a continuous probability distribution
 * of two variables, $X$ and $Y$, with cdf
 * $$
 * \begin{align*}
 * M(a, b, \rho) = \frac{1}{2\pi\sqrt{1 - \rho^2}}\int_{-\infty}^a\int_{-\infty}^{b} e^{\frac{-(x^2 - 2\rho xy + y^2)}{2(1 - \rho^2)}} dx dy
 * \end{align*}
 * $$
 * where $\rho$ is the correlation between $X$ and $Y$.
 */
public abstract class BivariateNormalDistribution implements ProbabilityDistribution<double[]> {

  private static final double TWO_PI = 2 * Math.PI;


  /**
   * @param x The parameters for the function, $(a, b, \rho$, with $-1 \geq \rho \geq 1$, not null
   * @return The cdf
   */
  @Override
  public final double getCDF(final double[] x) {
    Validate.notNull(x);
    Validate.isTrue(x.length == 3, "Need a, b and rho values");
    Validate.isTrue(x[2] >= -1 && x[2] <= 1, "Correlation must be >= -1 and <= 1");
    final double a = x[0];
    double b = x[1];
    final double rho = x[2];
    return getCDF(a, b, rho);
  }

  protected abstract double getCDF(final double a, final double b, final double rho);

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws NotImplementedException
   */
  @Override
  public double getInverseCDF(final double[] p) {
    throw new NotImplementedException();
  }

  /**
   * @param x The parameters for the function, $(a, b, \rho$, with $-1 \geq \rho \geq 1$, not null
   * @return The pdf
   */
  @Override
  public double getPDF(final double[] x) {
    Validate.notNull(x);
    Validate.isTrue(x.length == 3, "Need a, b and rho values");
    Validate.isTrue(x[2] >= -1 && x[2] <= 1, "Correlation must be >= -1 and <= 1");
    final double denom = 1 - x[2] * x[2];
    return Math.exp(-(x[0] * x[0] - 2 * x[2] * x[0] * x[1] + x[1] * x[1]) / (2 * denom)) / (TWO_PI * Math.sqrt(denom));
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws NotImplementedException
   */
  @Override
  public double nextRandom() {
    throw new NotImplementedException();
  }
}
