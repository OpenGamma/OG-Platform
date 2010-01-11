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
public class SamplePearsonKurtosisCalculator extends Function1D<Double[], Double> {
  private final Function1D<Double[], Double> _kurtosis = new SampleFisherKurtosisCalculator();

  @Override
  public Double evaluate(final Double[] x) {
    if (x == null)
      throw new IllegalArgumentException("Array was null");
    if (x.length < 2)
      throw new IllegalArgumentException("Need at least two points to calculate kurtosis");
    return _kurtosis.evaluate(x) + 3;
  }
}
