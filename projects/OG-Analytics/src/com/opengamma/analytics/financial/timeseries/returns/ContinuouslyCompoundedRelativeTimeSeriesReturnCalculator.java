/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.returns;

import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.CalculationMode;
import com.opengamma.util.timeseries.TimeSeriesException;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class ContinuouslyCompoundedRelativeTimeSeriesReturnCalculator extends RelativeTimeSeriesReturnCalculator {
  private static final Logger s_logger = LoggerFactory.getLogger(SimpleNetRelativeTimeSeriesReturnCalculator.class);
  private static final double ZERO = 1e-12;

  public ContinuouslyCompoundedRelativeTimeSeriesReturnCalculator(final CalculationMode mode) {
    super(mode);
  }

  @Override
  public LocalDateDoubleTimeSeries evaluate(final LocalDateDoubleTimeSeries... x) {
    testInputData(x);
    if (x.length > 2) {
      s_logger.info("Have more than two time series in array; only using first two");
    }
    final FastIntDoubleTimeSeries ts1 = (FastIntDoubleTimeSeries) x[0].getFastSeries();
    final FastIntDoubleTimeSeries ts2 = (FastIntDoubleTimeSeries) x[1].getFastSeries();
    final int n = ts1.size();
    final int[] times = new int[n];
    final double[] returns = new double[n];
    final Iterator<Entry<Integer, Double>> iter1 = ts1.iterator();
    Entry<Integer, Double> entry1;
    Double value2;
    Integer t;
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
    return getSeries(x[0], times, returns, i);
  }
}
