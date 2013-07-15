/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.pnl;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;

/**
 * The drawdown is a measure of the decline from the historic peak of a variable (e.g. the NAV of a fund).
 * This calculator returns a time series of the drawdown at each point in time.
 */
public class DrawdownCalculator extends Function1D<DateDoubleTimeSeries<?>, DateDoubleTimeSeries<?>> {

  /**
   * Calculates the drawdown time series, with the drawdown expressed as a decimal
   * @param ts A time series
   * @return The drawdown
   * @throws IllegalArgumentException If the time series is null or empty 
   */
  @Override
  public DateDoubleTimeSeries<?> evaluate(final DateDoubleTimeSeries<?> ts) {
    Validate.notNull(ts, "time series");
    Validate.isTrue(ts.size() > 0);
    final int n = ts.size();
    final int[] t = ts.timesArrayFast();
    final double[] drawdown = new double[n];
    t[0] = ts.getEarliestTimeFast();
    drawdown[0] = 0;
    double peak = ts.getEarliestValueFast();
    double value;
    for (int i = 1; i < n; i++) {
      value = ts.getValueAtIndexFast(i);
      if (value > peak) {
        peak = value;
        drawdown[i] = 0;
      } else {
        drawdown[i] = (peak - value) / peak;
      }
    }
    return ImmutableLocalDateDoubleTimeSeries.of(t, drawdown);
  }

}
