/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import java.util.Arrays;
import java.util.TreeMap;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 */
public class ModeCalculator extends Function1D<Double[], Double> {
  private final double EPS = 1e-16;

  @Override
  public Double evaluate(final Double[] x) {
    if (x == null)
      throw new IllegalArgumentException("Array was null");
    if (x.length == 0)
      throw new IllegalArgumentException("Array was empty");
    if (x.length == 1)
      return x[0];
    final Double[] x1 = Arrays.copyOf(x, x.length);
    Arrays.sort(x1);
    final TreeMap<Integer, Double> counts = new TreeMap<Integer, Double>();
    int count = 1;
    for (int i = 1; i < x1.length; i++) {
      if (Math.abs(x1[i] - x1[i - 1]) < EPS) {
        count++;
      } else {
        counts.put(count, x1[i - 1]);
        count = 1;
      }
    }
    if (counts.lastKey() == 1)
      throw new MathException("Could not find mode for array; no repeated values");
    return counts.lastEntry().getValue();
  }
}
