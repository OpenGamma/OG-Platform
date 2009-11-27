/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import java.util.Arrays;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 */
public class MedianCalculator extends Function1D<Double[], Double> {

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
    final int mid = x1.length / 2;
    if (x1.length % 2 == 1)
      return x1[mid];
    return (x1[mid] + x1[mid - 1]) / 2.;
  }

}
