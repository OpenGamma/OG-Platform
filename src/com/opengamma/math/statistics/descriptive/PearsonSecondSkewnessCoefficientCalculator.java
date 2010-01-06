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
public class PearsonSecondSkewnessCoefficientCalculator extends Function1D<Double[], Double> {
  private final Function1D<Double[], Double> _mean = new MeanCalculator();
  private final Function1D<Double[], Double> _median = new MedianCalculator();
  private final Function1D<Double[], Double> _stdDev = new SampleStandardDeviationCalculator();

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
    return 3 * (_mean.evaluate(x) - _median.evaluate(x)) / _stdDev.evaluate(x);
  }

}
