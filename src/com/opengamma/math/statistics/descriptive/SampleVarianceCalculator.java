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
public class SampleVarianceCalculator extends Function1D<Double[], Double> {
  private final Function1D<Double[], Double> _meanCalculator = new MeanCalculator();

  @Override
  public Double evaluate(final Double[] x) {
    if (x == null)
      throw new IllegalArgumentException("Time series was null");
    if (x.length < 2)
      throw new IllegalArgumentException("Need at least two points to calculate the sample variance");
    final Double mean = _meanCalculator.evaluate(x);
    double diff, sum = 0;
    for (final Double value : x) {
      diff = value - mean;
      sum += diff * diff;
    }
    final int n = x.length;
    return sum / (n - 1);
  }

}
