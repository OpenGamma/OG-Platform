/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;

/**
 * Calculates the geometric mean of a series of data. 
 * <p>
 * The geometric mean $\mu$ of a series of elements $x_1, x_2, \dots, x_n$ is given by:
 * $$
 * \begin{align*}
 * \mu = \left({\prod\limits_{i=1}^n x_i}\right)^{\frac{1}{n}}
 * \end{align*}
 * $$
 * 
 */
public class GeometricMeanCalculator extends Function1D<double[], Double> {

  /**
   * @param x The array of data, not null or empty
   * @return The geometric mean
   */
  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x, "x");
    Validate.isTrue(x.length > 0, "x cannot be empty");
    final int n = x.length;
    double mult = x[0];
    for (int i = 1; i < n; i++) {
      mult *= x[i];
    }
    return Math.pow(mult, 1. / n);
  }

}
