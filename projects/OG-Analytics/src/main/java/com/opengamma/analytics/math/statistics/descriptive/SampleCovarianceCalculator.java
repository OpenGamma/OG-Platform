/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;

/**
 * Calculates the sample covariance of two series of data, $x_1, x_2, \dots, x_n$ and $y_1, y_2, \dots, y_n$.
 *
 * <p>
 * The sample covariance is given by:
 * $$
 * \begin{align*}
 * \text{cov} = \frac{1}{n-1}\sum_{i=1}^n (x_i - \overline{x})(y_i - \overline{y})
 * \end{align*}
 * $$
 * where $\overline{x}$ and $\overline{y}$ are the means of the two series.
 */
public class SampleCovarianceCalculator implements Function<double[], Double> {
  private static final Function1D<double[], Double> MEAN_CALCULATOR = new MeanCalculator();

  /**
   * @param x The array of data, not null. The first and second elements must be arrays of data, neither of which is null or has less than two elements.
   * @return The sample covariance
   */
  @Override
  public Double evaluate(final double[]... x) {
    Validate.notNull(x, "x");
    Validate.isTrue(x.length > 1);
    final double[] x1 = x[0];
    final double[] x2 = x[1];
    Validate.isTrue(x1.length > 1);
    final int n = x1.length;
    Validate.isTrue(x2.length == n);
    final double mean1 = MEAN_CALCULATOR.evaluate(x1);
    final double mean2 = MEAN_CALCULATOR.evaluate(x2);
    double sum = 0;
    for (int i = 0; i < n; i++) {
      sum += (x1[i] - mean1) * (x2[i] - mean2);
    }
    return sum / (n - 1);
  }
}
