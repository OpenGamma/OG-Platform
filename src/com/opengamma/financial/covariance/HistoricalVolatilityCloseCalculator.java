/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.util.CalculationMode;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class HistoricalVolatilityCloseCalculator extends HistoricalVolatilityCalculator {
  private static final Logger s_Log = LoggerFactory.getLogger(HistoricalVolatilityCloseCalculator.class);
  private final TimeSeriesReturnCalculator _returnCalculator;

  public HistoricalVolatilityCloseCalculator(final TimeSeriesReturnCalculator returnCalculator) {
    super();
    _returnCalculator = returnCalculator;
  }

  public HistoricalVolatilityCloseCalculator(final TimeSeriesReturnCalculator returnCalculator, final CalculationMode mode) {
    super(mode);
    _returnCalculator = returnCalculator;
  }

  public HistoricalVolatilityCloseCalculator(final TimeSeriesReturnCalculator returnCalculator, final CalculationMode mode, final double percentBadDataPoints) {
    super(mode, percentBadDataPoints);
    _returnCalculator = returnCalculator;
  }

  @Override
  public Double evaluate(final DoubleTimeSeries<?>... x) {
    testInput(x);
    if (x.length > 1) {
      s_Log.info("Time series array contained more than one series; only using the first one");
    }
    testTimeSeries(x, 2);
    final DoubleTimeSeries<?> returnTS = _returnCalculator.evaluate(x);
    final Iterator<Double> iter = returnTS.valuesIterator();
    Double value;
    double sum = 0;
    double sumSq = 0;
    final int n = returnTS.size() - 1;
    while (iter.hasNext()) {
      value = iter.next();
      sum += value;
      sumSq += value * value;
    }
    return Math.sqrt((sumSq - sum * sum / (n + 1)) / n);
  }
}
