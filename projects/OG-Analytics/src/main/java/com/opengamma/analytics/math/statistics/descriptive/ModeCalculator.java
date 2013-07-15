/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.descriptive;

import java.util.Arrays;
import java.util.TreeMap;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * The mode of a series of data is the value that occurs more frequently in the data set.
 */
public class ModeCalculator extends Function1D<double[], Double> {
  private static final double EPS = 1e-16;

  //TODO more than one value can be the mode
  /**
   * @param x The array of data, not null or empty
   * @return The arithmetic mean
   */
  @Override
  public Double evaluate(final double[] x) {
    ArgumentChecker.notNull(x, "x");
    ArgumentChecker.isTrue(x.length > 0, "x cannot be empty");
    if (x.length == 1) {
      return x[0];
    }
    final double[] x1 = Arrays.copyOf(x, x.length);
    Arrays.sort(x1);
    final TreeMap<Integer, Double> counts = new TreeMap<>();
    int count = 1;
    for (int i = 1; i < x1.length; i++) {
      if (Math.abs(x1[i] - x1[i - 1]) < EPS) {
        count++;
      } else {
        counts.put(count, x1[i - 1]);
        count = 1;
      }
    }
    if (counts.lastKey() == 1) {
      throw new MathException("Could not find mode for array; no repeated values");
    }
    return counts.lastEntry().getValue();
  }
}
