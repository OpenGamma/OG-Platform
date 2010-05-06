/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import java.util.List;

import com.opengamma.math.function.Function2D;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.TimeSeriesException;

public abstract class CovarianceCalculator extends Function2D<DoubleTimeSeries<?>, Double> {

  protected void testTimeSeries(final DoubleTimeSeries<?> ts1, final DoubleTimeSeries<?> ts2) {
    if (ts1 == null)
      throw new IllegalArgumentException("First time series was null");
    if (ts2 == null)
      throw new IllegalArgumentException("Second time series was null");
    if (ts1.isEmpty())
      throw new IllegalArgumentException("First time series was empty");
    if (ts2.isEmpty())
      throw new IllegalArgumentException("Second time series was empty");
    if (ts1.size() < 2)
      throw new IllegalArgumentException("First time series had fewer than two points");
    if (ts2.size() < 2)
      throw new IllegalArgumentException("Second time series had fewer than two points");
    if (ts1.size() != ts2.size())
      throw new IllegalArgumentException("Time series were not the same length");
    final List<?> times1 = ts1.times();
    final List<?> times2 = ts2.times();
    for (final Object t : times1) {
      if (!times2.contains(t))
        throw new TimeSeriesException("Time series did not contain the same dates");
    }
  }
}
