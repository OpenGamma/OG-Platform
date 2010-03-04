/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.returns;

import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.CalculationMode;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.TimeSeriesException;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class ContinuouslyCompoundedRelativeTimeSeriesReturnCalculator extends RelativeTimeSeriesReturnCalculator {
  private static final Logger s_Log = LoggerFactory.getLogger(SimpleNetRelativeTimeSeriesReturnCalculator.class);
  private final double ZERO = 1e-12;

  public ContinuouslyCompoundedRelativeTimeSeriesReturnCalculator(final CalculationMode mode) {
    super(mode);
  }

  @Override
  public DoubleTimeSeries<?> evaluate(final DoubleTimeSeries<?>... x) {
    if (x == null)
      throw new TimeSeriesException("Time series array was null");
    if (x.length > 2) {
      s_Log.info("Have more than two time series in array; only using first two");
    }
    if (x[0] == null)
      throw new TimeSeriesException("First time series was null");
    if (x[1] == null)
      throw new TimeSeriesException("Second time series was null");
    final FastLongDoubleTimeSeries ts1 = x[0].toFastLongDoubleTimeSeries();
    final FastLongDoubleTimeSeries ts2 = x[1].toFastLongDoubleTimeSeries();
    final int n = ts1.size();
    final long[] times = new long[n];
    final double[] returns = new double[n];
    final Iterator<Entry<Long, Double>> iter1 = ts1.iterator();
    Entry<Long, Double> entry1;
    Double value2;
    long t;
    int i = 0;
    while (iter1.hasNext()) {
      entry1 = iter1.next();
      t = entry1.getKey();
      value2 = ts2.getValue(t);
      if (value2 == null || Math.abs(value2) < ZERO) {
        if (getMode().equals(CalculationMode.STRICT)) {
          throw new TimeSeriesException("No data in second series for time " + t);
        }
      } else {
        times[i] = entry1.getKey();
        returns[i++] = Math.log(entry1.getValue() / value2);
      }
    }
    return getSeries(ts1, times, returns, i);
  }
}
