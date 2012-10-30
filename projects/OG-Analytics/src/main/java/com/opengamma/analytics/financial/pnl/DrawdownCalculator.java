/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.pnl;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * The drawdown is a measure of the decline from the historic peak of a variable (e.g. the NAV of a fund). This calculator returns a time series
 * of the drawdown at each point in time.
 */
public class DrawdownCalculator extends Function1D<DoubleTimeSeries<?>, DoubleTimeSeries<?>> {

  /**
   * Calculates the drawdown time series, with the drawdown expressed as a decimal
   * @param ts A time series
   * @return The drawdown
   * @throws IllegalArgumentException If the time series is null or empty 
   */
  @Override
  public DoubleTimeSeries<?> evaluate(final DoubleTimeSeries<?> ts) {
    Validate.notNull(ts, "time series");
    Validate.isTrue(ts.size() > 0);
    final FastLongDoubleTimeSeries fastTS = ts.toFastLongDoubleTimeSeries();
    final DateTimeNumericEncoding encoding = fastTS.getEncoding();
    final int n = fastTS.size();
    final long[] t = fastTS.timesArrayFast();
    final double[] drawdown = new double[n];
    t[0] = fastTS.getEarliestTimeFast();
    drawdown[0] = 0;
    double peak = fastTS.getEarliestValueFast();
    double value;
    for (int i = 1; i < n; i++) {
      value = fastTS.getValueAtFast(i);
      if (value > peak) {
        peak = value;
        drawdown[i] = 0;
      } else {
        drawdown[i] = (peak - value) / peak;
      }
    }
    return new FastArrayLongDoubleTimeSeries(encoding, t, drawdown);
  }

}
