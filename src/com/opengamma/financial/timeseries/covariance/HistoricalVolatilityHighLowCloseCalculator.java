/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.covariance;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.timeseries.returns.RelativeTimeSeriesReturnCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.TimeSeriesException;

/**
 * 
 * @author emcleod
 */
public class HistoricalVolatilityHighLowCloseCalculator extends HistoricalVolatilityCalculator {
  private static final Logger s_Log = LoggerFactory.getLogger(HistoricalVolatilityHighLowCloseCalculator.class);
  private final TimeSeriesReturnCalculator _returnCalculator;
  private final RelativeTimeSeriesReturnCalculator _relativeReturnCalculator;

  public HistoricalVolatilityHighLowCloseCalculator(final TimeSeriesReturnCalculator returnCalculator, final RelativeTimeSeriesReturnCalculator relativeReturnCalculator) {
    _returnCalculator = returnCalculator;
    _relativeReturnCalculator = relativeReturnCalculator;
  }

  @Override
  public Double evaluate(final DoubleTimeSeries... x) {
    testInput(x);
    if (x.length < 3)
      throw new TimeSeriesException("Need high, low and close time series to calculate high-low-close volatility");
    if (x.length > 3) {
      s_Log.info("Time series array contained more than three series; only using the first three");
    }
    testTimeSeries(x, 2);
    testDatesCoincide(x);
    final DoubleTimeSeries high = x[0];
    final DoubleTimeSeries low = x[1];
    final DoubleTimeSeries close = x[2];
    final DoubleTimeSeries closeReturns = _returnCalculator.evaluate(close);
    final DoubleTimeSeries highLowReturns = _relativeReturnCalculator.evaluate(new DoubleTimeSeries[] { high, low });
    final Iterator<Double> highLowIterator = highLowReturns.valuesIterator();
    final Iterator<Double> closeReturnIterator = closeReturns.valuesIterator();
    double value, highLowValue;
    double sumHL = 0;
    double sum = 0;
    highLowIterator.next();
    while (closeReturnIterator.hasNext()) {
      value = closeReturnIterator.next();
      highLowValue = highLowIterator.next();
      sum += value * value;
      sumHL += highLowValue * highLowValue;
    }
    final int n = close.size() - 1;
    return Math.sqrt((0.5 * sumHL - (2 * Math.log(2) - 1) * sum) / n);
  }
}
