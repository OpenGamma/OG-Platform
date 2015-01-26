/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.util;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Operator to calculate the ratio or relative return of a time series: ratio = (V(end) - V(start)) / V(start)
 * The ratio is taken between elements in the time series with a certain lag. 
 * The default lag is 1 element, which means that the difference is between consecutive entries in the series.
 * The series returned has less element than the input series by the lag.
 * The dates of the returned time series are the dates of the end of the period on which the difference is computed.
 */
public class TimeSeriesPercentageChangeOperator extends Function1D<DateDoubleTimeSeries<?>, DateDoubleTimeSeries<?>> {
  
  /** The default lag: 1 time series element. */
  private static final int DEFAULT_LAG = 1;
  /** The lag between the element of the times series on which the difference is taken. */
  private final int _lag;
  
  /**
   * Constructor with the default lag of 1 element.
   */
  public TimeSeriesPercentageChangeOperator() {
    this._lag = DEFAULT_LAG;
  }

  /**
   * Constructor with a specified lag.
   * @param lag The lag between element to compute the difference.
   */
  public TimeSeriesPercentageChangeOperator(int lag) {
    this._lag = lag;
  }

  @Override
  public DateDoubleTimeSeries<?> evaluate(DateDoubleTimeSeries<?> ts) {
    ArgumentChecker.notNull(ts, "time series");
    ArgumentChecker.isTrue(ts.size() > _lag, "time series length must be > lag");
    final int[] times = ts.timesArrayFast();
    final double[] values = ts.valuesArrayFast();
    final int n = times.length;
    final int[] resultTimes = new int[n - _lag];
    final double[] percentageChanges = new double[n - _lag];
    for (int i = _lag; i < n; i++) {
      ArgumentChecker.isTrue(values[i - _lag] != 0.0d,
          "value equal to 0 at date {}, no relative change can be computed", times[i - _lag]);
      resultTimes[i - _lag] = times[i];
      percentageChanges[i - _lag] = (values[i] - values[i - _lag]) / values[i - _lag];
    }
    return ImmutableLocalDateDoubleTimeSeries.of(resultTimes, percentageChanges);
  }

}
