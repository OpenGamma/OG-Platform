/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class SampleSkewnessCalculator extends Function1D<double[], Double> {
  private final Function1D<double[], Double> _mean = new MeanCalculator();
  private final Function1D<double[], Double> _variance = new PopulationVarianceCalculator();

  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x, "x");
    if (x.length < 3) {
      throw new IllegalArgumentException("Need at least three points to calculate skewness");
    }
    double sum = 0;
    final double mean = _mean.evaluate(x);
    final double variance = _variance.evaluate(x);
    for (final Double d : x) {
      sum += Math.pow(d - mean, 3);
    }
    final int n = x.length;
    return Math.sqrt(n * (n - 1.)) * sum / (Math.pow(variance, 1.5) * n * (n - 2));
  }

}
