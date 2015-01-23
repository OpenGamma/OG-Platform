/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.util;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;

/**
 * Operator to obtain the difference or absolute return of a time series.
 * The difference is taken between elements in the time series with a certain lag. 
 * The default lag is 1 element, which means that the difference is between consecutive entries in the series.
 * The series returned has less element than the input series by the lag.
 * The dates of the returned time series are the dates of the end of the period on which the difference is computed.
 */
public class TimeSeriesDifferenceOperator extends Function1D<DateDoubleTimeSeries<?>, DateDoubleTimeSeries<?>> {
  
  /** The default lag: 1 time series element. */
  private static final int DEFAULT_LAG = 1;
  /** The lag between the element of the times series on which the difference is taken. */
  private final int _lag;
  
  /**
   * Constructor with the default lag of 1 element.
   */
  public TimeSeriesDifferenceOperator() {
    this._lag = DEFAULT_LAG;
  }

  /**
   * Constructor with a specified lag.
   * @param lag The lag between element to compute the difference.
   */
  public TimeSeriesDifferenceOperator(int lag) {
    this._lag = lag;
  }
  
  @Override
  public DateDoubleTimeSeries<?> evaluate(final DateDoubleTimeSeries<?> ts) {
    Validate.notNull(ts, "time series");
    Validate.isTrue(ts.size() > 1, "time series length must be > 1");
    final int[] times = ts.timesArrayFast();
    final double[] values = ts.valuesArrayFast();
    final int n = times.length;
    final int[] differenceTimes = new int[n - _lag];
    final double[] differenceValues = new double[n - _lag];
    for (int i = _lag; i < n; i++) {
      differenceTimes[i - _lag] = times[i];
      differenceValues[i - _lag] = values[i] - values[i - _lag];
    }
    return ImmutableLocalDateDoubleTimeSeries.of(differenceTimes, differenceValues);
  }
  
}
