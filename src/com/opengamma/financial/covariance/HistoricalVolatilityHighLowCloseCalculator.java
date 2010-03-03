/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.timeseries.returns.RelativeTimeSeriesReturnCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.util.CalculationMode;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.TimeSeriesException;

/**
 * 
 * @author emcleod
 */
public class HistoricalVolatilityHighLowCloseCalculator<T extends DoubleTimeSeries<?>> extends HistoricalVolatilityCalculator<T> {
  private static final Logger s_Log = LoggerFactory.getLogger(HistoricalVolatilityHighLowCloseCalculator.class);
  private final TimeSeriesReturnCalculator<T> _returnCalculator;
  private final RelativeTimeSeriesReturnCalculator<T> _relativeReturnCalculator;

  public HistoricalVolatilityHighLowCloseCalculator(final TimeSeriesReturnCalculator<T> returnCalculator, final RelativeTimeSeriesReturnCalculator<T> relativeReturnCalculator) {
    super();
    _returnCalculator = returnCalculator;
    _relativeReturnCalculator = relativeReturnCalculator;
  }

  public HistoricalVolatilityHighLowCloseCalculator(final TimeSeriesReturnCalculator<T> returnCalculator, final RelativeTimeSeriesReturnCalculator<T> relativeReturnCalculator,
      final CalculationMode mode) {
    super(mode);
    _returnCalculator = returnCalculator;
    _relativeReturnCalculator = relativeReturnCalculator;
  }

  public HistoricalVolatilityHighLowCloseCalculator(final TimeSeriesReturnCalculator<T> returnCalculator, final RelativeTimeSeriesReturnCalculator<T> relativeReturnCalculator,
      final CalculationMode mode, final double percentBadDataPoints) {
    super(mode, percentBadDataPoints);
    _returnCalculator = returnCalculator;
    _relativeReturnCalculator = relativeReturnCalculator;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Double evaluate(final T... x) {
    testInput(x);
    if (x.length < 3) {
      throw new TimeSeriesException("Need high, low and close time series to calculate high-low-close volatility");
    }
    if (x.length > 3) {
      s_Log.info("Time series array contained more than three series; only using the first three");
    }
    testTimeSeries(x, 2);
    testDatesCoincide(x);
    final T high = x[0];
    final T low = x[1];
    final T close = x[2];
    testHighLowClose(high, low, close);
    final DoubleTimeSeries<Long> closeReturns = _returnCalculator.evaluate(close);
    final DoubleTimeSeries<Long> highLowReturns = _relativeReturnCalculator.evaluate((T[]) new DoubleTimeSeries<?>[] { high, low });
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
    final int n = closeReturns.size();
    return Math.sqrt((0.5 * sumHL - (2 * Math.log(2) - 1) * sum) / n);
  }
}
