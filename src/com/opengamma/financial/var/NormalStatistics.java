/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.math.function.Function1D;

/**
 * @author emcleod
 * 
 */
public class NormalStatistics<T> {
  private static final Logger s_Log = LoggerFactory.getLogger(NormalStatistics.class);
  private final Double _mean;
  private final double _standardDeviation;

  public NormalStatistics(final Function1D<T, Double> meanCalculator, final Function1D<T, Double> stdCalculator, final T data) {
    if (meanCalculator == null)
      s_Log.info("Mean calculator not provided - assuming that portfolio return is at risk-free rate");
    if (stdCalculator == null)
      throw new IllegalArgumentException("Standard deviation calculator was null");
    if (data == null)
      throw new IllegalArgumentException("Data were null");
    _mean = meanCalculator == null ? null : meanCalculator.evaluate(data);
    _standardDeviation = stdCalculator.evaluate(data);
  }

  public Double getMean() {
    return _mean;
  }

  public double getStandardDeviation() {
    return _standardDeviation;
  }
}
