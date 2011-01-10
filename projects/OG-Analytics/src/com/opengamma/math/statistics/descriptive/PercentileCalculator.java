/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
public class PercentileCalculator extends Function1D<double[], Double> {
  private double _percentile;

  public PercentileCalculator(final double percentile) {
    if (!ArgumentChecker.isInRangeExclusive(0, 1, percentile)) {
      throw new IllegalArgumentException("Percentile must be between 0 and 1");
    }
    _percentile = percentile;
  }

  public void setPercentile(final double percentile) {
    if (!ArgumentChecker.isInRangeExclusive(0, 1, percentile)) {
      throw new IllegalArgumentException("Percentile must be between 0 and 1");
    }
    _percentile = percentile;
  }

  @Override
  public Double evaluate(final double[] x) {
    ArgumentChecker.notNull(x, "x");
    ArgumentChecker.notEmpty(x, "x");
    final int length = x.length;
    final double[] copy = Arrays.copyOf(x, length);
    Arrays.sort(copy);
    final double n = _percentile * (length - 1) + 1;
    if (Math.round(n) == 1) {
      return copy[0];
    }
    if (Math.round(n) == length) {
      return copy[length - 1];
    }
    final double d = n % 1;
    final int k = (int) Math.round(n - d);
    return copy[k - 1] + d * (copy[k] - copy[k - 1]);
  }
}
