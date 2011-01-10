/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class SampleCentralMomentCalculator extends Function1D<double[], Double> {
  private final int _n;
  private final Function1D<double[], Double> _mean = new MeanCalculator();

  public SampleCentralMomentCalculator(final int n) {
    ArgumentChecker.notNegative(n, "n");
    if (n < 0) {
      throw new IllegalArgumentException("N must be greater than or equal to zero");
    }
    _n = n;
  }

  @Override
  public Double evaluate(final double[] x) {
    Validate.notNull(x, "x");
    if (x.length < 2) {
      throw new IllegalArgumentException("Need at least 2 data points to calculate moment");
    }
    if (_n == 0) {
      return 1.;
    }
    final double mu = _mean.evaluate(x);
    double sum = 0;
    for (final Double d : x) {
      sum += Math.pow(d - mu, _n);
    }
    return sum / (x.length - 1);
  }
}
