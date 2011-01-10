/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class SampleCovarianceCalculator implements Function<double[], Double> {
  private final Function1D<double[], Double> _meanCalculator = new MeanCalculator();

  @Override
  public Double evaluate(final double[]... x) {
    Validate.notNull(x, "x");
    Validate.isTrue(x.length > 1);
    final double[] x1 = x[0];
    final double[] x2 = x[1];
    Validate.isTrue(x1.length > 1);
    final int n = x1.length;
    Validate.isTrue(x2.length == n);
    final double mean1 = _meanCalculator.evaluate(x1);
    final double mean2 = _meanCalculator.evaluate(x2);
    double sum = 0;
    for (int i = 0; i < n; i++) {
      sum += (x1[i] - mean1) * (x2[i] - mean2);
    }
    return sum / (n - 1);
  }
}
