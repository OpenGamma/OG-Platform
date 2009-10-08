/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import java.util.Iterator;

import javax.time.InstantProvider;

import com.opengamma.math.function.Function2D;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.TimeSeriesException;

/**
 * 
 * @author emcleod
 */
public abstract class CovarianceCalculator extends Function2D<DoubleTimeSeries, Double> {

  protected void testTimeSeries(final DoubleTimeSeries ts1, final DoubleTimeSeries ts2) {
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
    final Iterator<InstantProvider> iter = ts1.timeIterator();
    while (iter.hasNext()) {
      try {
        ts2.getDataPoint(iter.next());
      } catch (final ArrayIndexOutOfBoundsException e) {
        throw new TimeSeriesException("Time sereies did not containt the same dates: " + e);
      }
    }
  }
}
