/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.util;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CompareUtils;

/**
 * Utility class for test that are performed often on time series.
 */
public class TimeSeriesDataTestUtils {

  /**
   * Tests that the time series is not null or empty
   * @param ts The time series
   * @throws IllegalArgumentException If the time series is null or empty
   */
  public static void testNotNullOrEmpty(final DoubleTimeSeries<?> ts) {
    ArgumentChecker.notNull(ts, "time series");
    ArgumentChecker.isTrue(!ts.isEmpty(), "time series");
  }

  /**
   * Tests that the time series has a minimum amount of data
   * @param ts The time series
   * @param minLength The minimum size
   * @throws IllegalArgumentException If the time series is null, empty, or contains fewer elements than the minimum size, if the minimum size is less than zero
   */
  public static void testTimeSeriesSize(final DoubleTimeSeries<?> ts, final int minLength) {
    ArgumentChecker.notNull(ts, "time series");
    ArgumentChecker.isTrue(minLength >= 0, "Minumum length must be greater than zero");
    ArgumentChecker.isTrue(ts.size() >= minLength, "time series must contain at least " + minLength + " values");
  }

  /**
   * Tests that the two time series contain the same dates
   * @param ts1 The first time series
   * @param ts2 The second time series
   * @throws IllegalArgumentException If either time series is: null; empty; contains fewer than two data points; are not the same length; do not contain the same dates
   */
  public static void testTimeSeriesDates(final DoubleTimeSeries<?> ts1, final DoubleTimeSeries<?> ts2) {
    testNotNullOrEmpty(ts1);
    testNotNullOrEmpty(ts2);
    final int n = ts1.size();
    if (n != ts2.size()) {
      throw new IllegalArgumentException("Time series were not the same length; have " + ts1.size() + " and " + ts2.size());
    }
    final Object[] times1 = ts1.timesArray();
    final Object[] times2 = ts2.timesArray();
    for (int i = 0; i < n; i++) {
      if (!times1[i].equals(times2[i])) {
        throw new IllegalArgumentException("Time series did not contain the same dates at index " + i);
      }
    }
  }

  /**
   * Tests that the two time-series contain approximately-equal values.
   * @param ts1  the first time-series, not null
   * @param ts2  the second time-series, not null
   * @param maxDifference The difference above which numbers are not equal
   */
  public static void testCloseEquals(final DoubleTimeSeries<?> ts1, final DoubleTimeSeries<?> ts2, final double maxDifference) {
    testNotNullOrEmpty(ts1);
    testNotNullOrEmpty(ts2);
    final int n = ts1.size();
    if (n != ts2.size()) {
      throw new IllegalArgumentException("Time series were not the same length; have " + ts1.size() + " and " + ts2.size());
    }
    for (int i = 0; i < n; i++) {
      if (!ts1.timesArray()[i].equals(ts2.timesArray()[i])) {
        throw new IllegalArgumentException("Time series did not contain the same dates at index " + i);
      }
      if (!CompareUtils.closeEquals(ts1.valuesArrayFast()[i], ts2.valuesArrayFast()[i], maxDifference)) {
        throw new IllegalArgumentException("Time-series did not contain approximately-equal values at " +
            ts1.timesArray()[i] + ": " + ts1.valuesArrayFast()[i] + " and " + ts2.valuesArrayFast()[i]);
      }
    }
  }

}
