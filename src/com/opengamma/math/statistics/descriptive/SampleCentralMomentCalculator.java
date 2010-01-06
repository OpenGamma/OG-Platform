/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import com.opengamma.math.function.Function1D;

/**
 * @author emcleod
 * 
 */
public class SampleCentralMomentCalculator extends Function1D<Double[], Double> {
  private final int _n;
  private final Function1D<Double[], Double> _mean = new MeanCalculator();

  public SampleCentralMomentCalculator(final int n) {
    if (n < 0)
      throw new IllegalArgumentException("N must be greater than or equal to zero");
    _n = n;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.math.function.Function1D#evaluate(java.lang.Object)
   */
  @Override
  public Double evaluate(final Double[] x) {
    if (x == null)
      throw new IllegalArgumentException("Array was null");
    if (x.length < 2)
      throw new IllegalArgumentException("Need at least 2 data points to calculate moment");
    if (_n == 0)
      return 1.;
    final double mu = _mean.evaluate(x);
    double sum = 0;
    for (final Double d : x) {
      sum += Math.pow(d - mu, _n);
    }
    return sum / (x.length - 1);
  }
}
