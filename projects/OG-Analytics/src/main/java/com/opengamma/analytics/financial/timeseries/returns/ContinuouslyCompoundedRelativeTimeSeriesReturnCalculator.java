/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.returns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.timeseries.TimeSeriesException;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleEntryIterator;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.CalculationMode;

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
    final LocalDateDoubleTimeSeries ts1 = x[0];
    final LocalDateDoubleTimeSeries ts2 = x[1];
    final int n = ts1.size();
    final int[] times = new int[n];
    final double[] returns = new double[n];
    final LocalDateDoubleEntryIterator iter1 = ts1.iterator();
    int i = 0;
    while (iter1.hasNext()) {
      final int date = iter1.nextTimeFast();
      final Double value2 = ts2.getValue(date);
      if (value2 == null || Math.abs(value2) < ZERO) {
        if (getMode().equals(CalculationMode.STRICT)) {
          throw new TimeSeriesException("No data in second series for time " + iter1.currentTime());
        }
      } else {
        times[i] = iter1.currentTimeFast();
        returns[i++] = Math.log(iter1.currentValue() / value2);
      }
    }
    return getSeries(times, returns, i);
  }

}
