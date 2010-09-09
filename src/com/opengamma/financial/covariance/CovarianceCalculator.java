/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.TimeSeriesException;

/**
 * 
 * Base class for calculating the covariance of two time series.
 */
public abstract class CovarianceCalculator implements Function<DoubleTimeSeries<?>, Double> {

  /**
   * 
   * @param ts1 The first time series
   * @param ts2 The second time series
   * @throws IllegalArgumentException If either time series is: null; empty; contains fewer than two data points; are not the same length; do not contain the same dates 
   */
  protected void testTimeSeries(final DoubleTimeSeries<?> ts1, final DoubleTimeSeries<?> ts2) {
    Validate.notNull(ts1, "ts1");
    Validate.notNull(ts2, "ts2");
    if (ts1.isEmpty()) {
      throw new IllegalArgumentException("First time series was empty");
    }
    if (ts2.isEmpty()) {
      throw new IllegalArgumentException("Second time series was empty");
    }
    if (ts1.size() < 2) {
      throw new IllegalArgumentException("First time series had fewer than two points");
    }
    if (ts2.size() < 2) {
      throw new IllegalArgumentException("Second time series had fewer than two points");
    }
    if (ts1.size() != ts2.size()) {
      throw new IllegalArgumentException("Time series were not the same length");
    }
    final List<?> times1 = ts1.times();
    final List<?> times2 = ts2.times();
    for (final Object t : times1) {
      if (!times2.contains(t)) {
        throw new TimeSeriesException("Time series did not contain the same dates");
      }
    }
  }
}
