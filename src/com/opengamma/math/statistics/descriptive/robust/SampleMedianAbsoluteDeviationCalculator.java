/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive.robust;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.descriptive.MedianCalculator;

/**
 * 
 * @author emcleod
 */
public class SampleMedianAbsoluteDeviationCalculator extends Function1D<Double[], Double> {
  private final Function1D<Double[], Double> _median = new MedianCalculator();

  @Override
  public Double evaluate(final Double[] x) {
    if (x == null)
      throw new IllegalArgumentException("Array was null");
    if (x.length < 2)
      throw new IllegalArgumentException("Need at least two data points to calculate MAD");
    final double median = _median.evaluate(x);
    final int n = x.length;
    final Double[] diff = new Double[n];
    for (int i = 0; i < n; i++) {
      diff[i] = Math.abs(x[i] - median);
    }
    return _median.evaluate(diff);
  }

}
