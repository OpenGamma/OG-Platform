/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.covariance;

import java.util.Iterator;

import javax.time.InstantProvider;

import com.opengamma.math.function.Function;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.TimeSeriesException;

/**
 * 
 * @author emcleod
 */
public abstract class HistoricalVolatilityCalculator implements Function<DoubleTimeSeries, Double> {

  @Override
  public abstract Double evaluate(final DoubleTimeSeries... x);

  protected void testInput(final DoubleTimeSeries[] x) {
    if (x == null)
      throw new TimeSeriesException("Array of time series was null");
    if (x.length == 0)
      throw new TimeSeriesException("Length of time series was null");
  }

  protected void testTimeSeries(final DoubleTimeSeries[] x, final int minLength) {
    for (final DoubleTimeSeries ts : x) {
      if (ts.size() < minLength)
        throw new TimeSeriesException("Need at least two data points to calculate volatility");
    }
  }

  protected void testDatesCoincide(final DoubleTimeSeries[] x) {
    final int size = x[0].size();
    for (int i = 1; i < x.length; i++) {
      if (x[i].size() != size)
        throw new TimeSeriesException("Time series were not all the same length");
    }
    final Iterator<InstantProvider> iter = x[0].timeIterator();
    while (iter.hasNext()) {
      final InstantProvider instant = iter.next();
      for (int i = 1; i < x.length; i++) {
        try {
          x[i].getDataPoint(instant);
        } catch (final ArrayIndexOutOfBoundsException e) {
          throw new TimeSeriesException("Time series did not all contain the same dates; " + e);
        }
      }
    }
  }
}
