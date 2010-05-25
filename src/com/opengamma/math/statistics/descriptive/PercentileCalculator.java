/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import java.util.Arrays;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class PercentileCalculator extends Function1D<Double[], Double> {
  private double _percentile;

  public PercentileCalculator(final double percentile) {
    if (percentile <= 0 || percentile >= 1)
      throw new IllegalArgumentException("Percentile must be between 0 and 1");
    _percentile = percentile;
  }

  public void setPercentile(final double percentile) {
    if (percentile <= 0 || percentile >= 1)
      throw new IllegalArgumentException("Percentile must be between 0 and 1");
    _percentile = percentile;
  }

  @Override
  public Double evaluate(final Double[] x) {
    ArgumentChecker.notNull(x, "x");
    ArgumentChecker.notEmpty(x, "x");
    final int length = x.length;
    final Double[] copy = Arrays.copyOf(x, length);
    Arrays.sort(copy);
    final double n = _percentile * (length - 1) + 1;
    if (Math.round(n) == 1)
      return copy[0];
    if (Math.round(n) == length)
      return copy[length - 1];
    final double d = n % 1;
    final int k = (int) Math.round(n - d);
    return copy[k - 1] + d * (copy[k] - copy[k - 1]);
  }
}
