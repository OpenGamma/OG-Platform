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
import com.opengamma.util.CalculationMode;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.TimeSeriesException;

/**
 * 
 * @author emcleod
 */
public class HistoricalVolatilityHighLowCalculator<T extends DoubleTimeSeries<?>> extends HistoricalVolatilityCalculator<T> {
  private static final Logger s_Log = LoggerFactory.getLogger(HistoricalVolatilityHighLowCalculator.class);
  private final RelativeTimeSeriesReturnCalculator<T> _returnCalculator;

  public HistoricalVolatilityHighLowCalculator(final RelativeTimeSeriesReturnCalculator<T> returnCalculator) {
    super();
    _returnCalculator = returnCalculator;
  }

  public HistoricalVolatilityHighLowCalculator(final RelativeTimeSeriesReturnCalculator<T> returnCalculator, final CalculationMode mode) {
    super(mode);
    _returnCalculator = returnCalculator;
  }

  public HistoricalVolatilityHighLowCalculator(final RelativeTimeSeriesReturnCalculator<T> returnCalculator, final CalculationMode mode, final double percentBadDataPoints) {
    super(mode, percentBadDataPoints);
    _returnCalculator = returnCalculator;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Double evaluate(final T... x) {
    testInput(x);
    if (x.length < 2) {
      throw new TimeSeriesException("Need high and low time series to calculate high-low volatility");
    }
    if (x.length > 2) {
      s_Log.info("Time series array contained more than two series; only using the first two");
    }
    testTimeSeries(x, 1);
    testDatesCoincide(x);
    final T high = x[0];
    final T low = x[1];
    testHighLow(high, low);
    final DoubleTimeSeries<Long> returnTS = _returnCalculator.evaluate((T[]) new DoubleTimeSeries[] { high, low });
    final int n = returnTS.size();
    final Iterator<Double> iter = returnTS.valuesIterator();
    double sum = 0;
    while (iter.hasNext()) {
      sum += iter.next();
    }
    return sum / (2 * n * Math.sqrt(Math.log(2.)));
  }
}
