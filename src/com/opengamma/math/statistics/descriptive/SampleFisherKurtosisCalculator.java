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
public class SampleFisherKurtosisCalculator extends Function1D<Double[], Double> {
  private final Function1D<Double[], Double> _pearson = new SamplePearsonKurtosisCalculator();

  @Override
  public Double evaluate(final Double[] x) {
    if (x == null)
      throw new IllegalArgumentException("Array was null");
    if (x.length < 2)
      throw new IllegalArgumentException("Need at least two points to calculate kurtosis");
    return _pearson.evaluate(x) - 3;
  }

}
