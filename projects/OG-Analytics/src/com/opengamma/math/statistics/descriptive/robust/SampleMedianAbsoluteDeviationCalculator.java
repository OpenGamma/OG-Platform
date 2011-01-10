/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive.robust;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.descriptive.MedianCalculator;

/**
 * 
 */
public class SampleMedianAbsoluteDeviationCalculator extends Function1D<double[], Double> {
  private final Function1D<double[], Double> _median = new MedianCalculator();

  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x, "x");
    if (x.length < 2) {
      throw new IllegalArgumentException("Need at least two data points to calculate MAD");
    }
    final double median = _median.evaluate(x);
    final int n = x.length;
    final double[] diff = new double[n];
    for (int i = 0; i < n; i++) {
      diff[i] = Math.abs(x[i] - median);
    }
    return _median.evaluate(diff);
  }

}
