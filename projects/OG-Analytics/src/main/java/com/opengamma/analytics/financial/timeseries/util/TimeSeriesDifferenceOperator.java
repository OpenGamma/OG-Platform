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
 * 
 */
public class TimeSeriesDifferenceOperator extends Function1D<DateDoubleTimeSeries<?>, DateDoubleTimeSeries<?>> {

  @Override
  public DateDoubleTimeSeries<?> evaluate(final DateDoubleTimeSeries<?> ts) {
    Validate.notNull(ts, "time series");
    Validate.isTrue(ts.size() > 1, "time series length must be > 1");
    final int[] times = ts.timesArrayFast();
    final double[] values = ts.valuesArrayFast();
    final int n = times.length;
    final int[] differenceTimes = new int[n - 1];
    final double[] differenceValues = new double[n - 1];
    for (int i = 1; i < n; i++) {
      differenceTimes[i - 1] = times[i];
      differenceValues[i - 1] = values[i] - values[i - 1];
    }
    return ImmutableLocalDateDoubleTimeSeries.of(differenceTimes, differenceValues);
  }
  
}
