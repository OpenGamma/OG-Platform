/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import java.util.Arrays;

import com.opengamma.math.function.Function1D;

/**
 * @author emcleod
 * 
 */
public class QuartileSkewnessCalculator extends Function1D<Double[], Double> {
  private final Function1D<Double[], Double> _median = new MedianCalculator();

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.math.function.Function1D#evaluate(java.lang.Object)
   */
  @Override
  public Double evaluate(final Double[] x) {
    if (x == null)
      throw new IllegalArgumentException("Array was null");
    if (x.length < 3)
      throw new IllegalArgumentException("Need at least three points to calculate IQR");
    final int n = x.length;
    if (n == 3)
      return (x[2] - 2 * x[1] + x[0]) / 2.;
    final Double[] copy = Arrays.copyOf(x, n);
    Arrays.sort(copy);
    Double[] lower, upper;
    if (n % 2 == 0) {
      lower = Arrays.copyOfRange(copy, 0, n / 2);
      upper = Arrays.copyOfRange(copy, n / 2, n);
    } else {
      lower = Arrays.copyOfRange(copy, 0, n / 2 + 1);
      upper = Arrays.copyOfRange(copy, n / 2, n);
    }
    final double q1 = _median.evaluate(lower);
    final double q2 = _median.evaluate(x);
    final double q3 = _median.evaluate(upper);
    return (q1 - 2 * q2 + q3) / (q3 - q1);
  }

}
