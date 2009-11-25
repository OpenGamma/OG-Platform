/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 */
public class SampleSkewnessCalculator extends Function1D<Double[], Double> {
  private final Function1D<Double[], Double> _mean = new MeanCalculator();
  private final Function1D<Double[], Double> _variance = new SampleVarianceCalculator();

  @Override
  public Double evaluate(final Double[] x) {
    if (x == null)
      throw new IllegalArgumentException("Array was null");
    if (x.length < 2)
      throw new IllegalArgumentException("Need at least two points to calculate skewness");
    double sum = 0;
    final double mean = _mean.evaluate(x);
    final double variance = _variance.evaluate(x);
    for (final Double d : x) {
      sum += Math.pow(d - mean, 3);
    }
    return sum / (Math.pow(variance, 1.5) * (x.length - 1));
  }

}
