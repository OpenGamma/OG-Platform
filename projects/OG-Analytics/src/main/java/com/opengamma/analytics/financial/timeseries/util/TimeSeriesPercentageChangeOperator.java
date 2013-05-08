/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.util;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;

/**
 * Calculates a one-day percentage change series from a series of absolute changes.
 */
public class TimeSeriesPercentageChangeOperator extends Function1D<DateDoubleTimeSeries<?>, DateDoubleTimeSeries<?>> {

  @Override
  public DateDoubleTimeSeries<?> evaluate(DateDoubleTimeSeries<?> ts) {
    Validate.notNull(ts, "time series");
    Validate.isTrue(ts.size() > 1, "time series length must be > 1");
    final int[] times = ts.timesArrayFast();
    final double[] values = ts.valuesArrayFast();
    final int n = times.length;
    final int[] resultTimes = new int[n - 1];
    final double[] percentageChanges = new double[n - 1];
    for (int i = 1; i < n; i++) {
      resultTimes[i - 1] = times[i];
      percentageChanges[i - 1] = (values[i] - values[i - 1]) / values[i - 1];
    }
    return ImmutableLocalDateDoubleTimeSeries.of(resultTimes, percentageChanges);
  }

}
