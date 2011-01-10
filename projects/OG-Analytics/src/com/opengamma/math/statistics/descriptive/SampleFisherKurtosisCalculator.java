/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class SampleFisherKurtosisCalculator extends Function1D<double[], Double> {
  private final Function1D<double[], Double> _mean = new MeanCalculator();
  private final Function1D<double[], Double> _variance = new PopulationVarianceCalculator();

  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x, "x");
    if (x.length < 4) {
      throw new IllegalArgumentException("Need at least four points to calculate kurtosis");
    }
    double sum = 0;
    final double mean = _mean.evaluate(x);
    final double variance = _variance.evaluate(x);
    for (final Double d : x) {
      sum += Math.pow(d - mean, 4);
    }
    final int n = x.length;
    return n * (n + 1.) * sum / ((n - 1.) * (n - 2.) * (n - 3.) * variance * variance) - 3 * (n - 1.) * (n - 1.) / ((n - 2.) * (n - 3.));
  }
}
