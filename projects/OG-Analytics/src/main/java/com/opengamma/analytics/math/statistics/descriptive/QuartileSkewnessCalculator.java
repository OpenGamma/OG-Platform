/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;

/**
 * Calculates the quartile skewness coefficient, which is given by:
 * $$
 * \begin{align*}
 * \text{QS} = \frac{Q_1 - 2Q_2 + Q_3}{Q_3 - Q_1}
 * \end{align*}
 * $$
 * where $Q_1$, $Q_2$ and $Q_3$ are the first, second and third quartiles.
 * <p> 
 * The quartile skewness coefficient is also known as the Bowley skewness.
 */
public class QuartileSkewnessCalculator extends Function1D<double[], Double> {
  private static final Function1D<double[], Double> MEDIAN = new MedianCalculator();

  /**
   * @param x The array of data, not null. Must contain at least three points.
   * @return The quartile skewness.
   */
  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x, "x");
    final int n = x.length;
    Validate.isTrue(n >= 3, "Need at least three points to calculate interquartile range");
    if (n == 3) {
      return (x[2] - 2 * x[1] + x[0]) / 2.;
    }
    final double[] copy = Arrays.copyOf(x, n);
    Arrays.sort(copy);
    double[] lower, upper;
    if (n % 2 == 0) {
      lower = Arrays.copyOfRange(copy, 0, n / 2);
      upper = Arrays.copyOfRange(copy, n / 2, n);
    } else {
      lower = Arrays.copyOfRange(copy, 0, n / 2 + 1);
      upper = Arrays.copyOfRange(copy, n / 2, n);
    }
    final double q1 = MEDIAN.evaluate(lower);
    final double q2 = MEDIAN.evaluate(x);
    final double q3 = MEDIAN.evaluate(upper);
    return (q1 - 2 * q2 + q3) / (q3 - q1);
  }

}
