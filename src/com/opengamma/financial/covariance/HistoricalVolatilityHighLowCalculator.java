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

/**
 * 
 */
public class HistoricalVolatilityHighLowCalculator extends HistoricalVolatilityCalculator {
  private static final Logger s_logger = LoggerFactory.getLogger(HistoricalVolatilityHighLowCalculator.class);
  private final RelativeTimeSeriesReturnCalculator _returnCalculator;

  public HistoricalVolatilityHighLowCalculator(final RelativeTimeSeriesReturnCalculator returnCalculator) {
    super();
    _returnCalculator = returnCalculator;
  }

  public HistoricalVolatilityHighLowCalculator(final RelativeTimeSeriesReturnCalculator returnCalculator, final CalculationMode mode) {
    super(mode);
    _returnCalculator = returnCalculator;
  }

  public HistoricalVolatilityHighLowCalculator(final RelativeTimeSeriesReturnCalculator returnCalculator, final CalculationMode mode, final double percentBadDataPoints) {
    super(mode, percentBadDataPoints);
    _returnCalculator = returnCalculator;
  }

  @Override
  public Double evaluate(final DoubleTimeSeries<?>... x) {
    testInput(x);
    if (x.length < 2) {
      throw new IllegalArgumentException("Need high and low time series to calculate high-low volatility");
    }
    if (x.length > 2) {
      s_logger.info("Time series array contained more than two series; only using the first two");
    }
    testTimeSeries(x, 1);
    testDatesCoincide(x);
    final DoubleTimeSeries<?> high = x[0];
    final DoubleTimeSeries<?> low = x[1];
    testHighLow(high, low);
    final DoubleTimeSeries<?> returnTS = _returnCalculator.evaluate(new DoubleTimeSeries<?>[] {high, low});
    final int n = returnTS.size();
    final Iterator<Double> iter = returnTS.valuesIterator();
    double sum = 0;
    while (iter.hasNext()) {
      sum += iter.next();
    }
    return sum / (2 * n * Math.sqrt(Math.log(2.)));
  }
}
