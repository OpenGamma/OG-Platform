/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.covariance;

import static com.opengamma.analytics.financial.timeseries.util.TimeSeriesDataTestUtils.testTimeSeriesDates;
import static com.opengamma.analytics.financial.timeseries.util.TimeSeriesDataTestUtils.testTimeSeriesSize;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
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
    testTimeSeriesSize(ts1, 2);
    testTimeSeriesSize(ts2, 2);
    testTimeSeriesDates(ts1, ts2);
  }
}
