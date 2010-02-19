/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import com.opengamma.math.function.Function1D;

/**
 * @author emcleod
 * 
 */
public class NormalStatistics<T> {
  private final Double _mean;
  private final double _standardDeviation;

  // TODO data shouldn't go here - need to have ability to change and
  // recalculate
  public NormalStatistics(final Function1D<T, Double> meanCalculator, final Function1D<T, Double> stdCalculator, final T data) {
    if (meanCalculator == null)
      throw new IllegalArgumentException("Standard deviation calculator was null");
    if (stdCalculator == null)
      throw new IllegalArgumentException("Standard deviation calculator was null");
    if (data == null)
      throw new IllegalArgumentException("Data were null");
    _mean = meanCalculator.evaluate(data);
    _standardDeviation = stdCalculator.evaluate(data);
  }

  public Double getMean() {
    return _mean;
  }

  public double getStandardDeviation() {
    return _standardDeviation;
  }
}
