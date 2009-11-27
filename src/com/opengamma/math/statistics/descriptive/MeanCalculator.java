/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.descriptive;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 */
public class MeanCalculator extends Function1D<Double[], Double> {

  @Override
  public Double evaluate(final Double[] x) {
    if (x == null)
      throw new IllegalArgumentException("Array was null");
    if (x.length == 0)
      throw new IllegalArgumentException("Array was empty");
    if (x.length == 1)
      return x[0];
    double sum = 0;
    for (final Double d : x) {
      sum += d;
    }
    return sum / x.length;
  }

}
