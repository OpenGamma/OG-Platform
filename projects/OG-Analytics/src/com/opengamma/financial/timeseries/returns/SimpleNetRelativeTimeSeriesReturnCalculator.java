/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
 */
public class SimpleNetRelativeTimeSeriesReturnCalculator extends RelativeTimeSeriesReturnCalculator {
  private static final Logger s_logger = LoggerFactory.getLogger(SimpleNetRelativeTimeSeriesReturnCalculator.class);
  private static final double ZERO = 1e-12;

  public SimpleNetRelativeTimeSeriesReturnCalculator(final CalculationMode mode) {
    super(mode);
  }

  @Override
  public DoubleTimeSeries<?> evaluate(final DoubleTimeSeries<?>... x) {
    testInputData(x);
    if (x.length > 2) {
      s_logger.info("Have more than two time series in array; only using first two");
    }
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
        returns[i++] = (entry1.getValue() / value2 - 1);
      }
    }
    return getSeries(ts1, times, returns, i);
  }
}
