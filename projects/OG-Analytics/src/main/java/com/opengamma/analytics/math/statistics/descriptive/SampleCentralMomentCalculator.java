/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;

/**
 * Calculates the $n^th$ sample central moment of a series of data.
 * <p>
 * The sample central moment $\mu_n$ of a series of data $x_1, x_2, \dots, x_s$ is given by:
 * $$
 * \begin{align*}
 * \mu_n = \frac{1}{s}\sum_{i=1}^s (x_i - \overline{x})^n
 * \end{align*}
 * $$
 * where $\overline{x}$ is the mean.
 */
public class SampleCentralMomentCalculator extends Function1D<double[], Double> {
  private static final Function1D<double[], Double> MEAN = new MeanCalculator();
  private final int _n;

  /**
   * @param n The degree of the moment to calculate, cannot be negative
   */
  public SampleCentralMomentCalculator(final int n) {
    Validate.isTrue(n >= 0, "n must be >= 0");
    _n = n;
  }

  /**
   * @param x The array of data, not null. Must contain at least two data points.
   * @return The sample central moment.
   */
  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x, "x");
    Validate.isTrue(x.length >= 2, "Need at least 2 data points to calculate central moment");
    if (_n == 0) {
      return 1.;
    }
    final double mu = MEAN.evaluate(x);
    double sum = 0;
    for (final Double d : x) {
      sum += Math.pow(d - mu, _n);
    }
    return sum / (x.length - 1);
  }
}
