/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.returns;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.TimeSeriesException;
import com.opengamma.timeseries.localdate.LocalDateDoubleIterator;
import com.opengamma.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.CalculationMode;

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
    final LocalDateDoubleIterator iter1 = ts1.iterator();
    int i = 0;
    while (iter1.hasNext()) {
      LocalDate t = iter1.nextLocalDate();
      Double value2 = ts2.getValue(t);
      if (value2 == null || Math.abs(value2) < ZERO) {
        if (getMode().equals(CalculationMode.STRICT)) {
          throw new TimeSeriesException("No data in second series for time " + t);
        }
      } else {
        times[i] = iter1.currentDate();
        returns[i++] = (iter1.currentValue() / value2 - 1);
      }
    }
    return getSeries(x[0], times, returns, i);
  }

}
