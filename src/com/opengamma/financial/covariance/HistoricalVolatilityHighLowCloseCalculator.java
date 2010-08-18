/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import java.util.Iterator;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.timeseries.returns.RelativeTimeSeriesReturnCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.util.CalculationMode;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class HistoricalVolatilityHighLowCloseCalculator extends HistoricalVolatilityCalculator {
  private static final Logger s_logger = LoggerFactory.getLogger(HistoricalVolatilityHighLowCloseCalculator.class);
  private final TimeSeriesReturnCalculator _returnCalculator;
  private final RelativeTimeSeriesReturnCalculator _relativeReturnCalculator;

  public HistoricalVolatilityHighLowCloseCalculator(final TimeSeriesReturnCalculator returnCalculator, final RelativeTimeSeriesReturnCalculator relativeReturnCalculator) {
    super();
    _returnCalculator = returnCalculator;
    _relativeReturnCalculator = relativeReturnCalculator;
  }

  public HistoricalVolatilityHighLowCloseCalculator(final TimeSeriesReturnCalculator returnCalculator, final RelativeTimeSeriesReturnCalculator relativeReturnCalculator, final CalculationMode mode) {
    super(mode);
    _returnCalculator = returnCalculator;
    _relativeReturnCalculator = relativeReturnCalculator;
  }

  public HistoricalVolatilityHighLowCloseCalculator(final TimeSeriesReturnCalculator returnCalculator, final RelativeTimeSeriesReturnCalculator relativeReturnCalculator, final CalculationMode mode,
      final double percentBadDataPoints) {
    super(mode, percentBadDataPoints);
    _returnCalculator = returnCalculator;
    _relativeReturnCalculator = relativeReturnCalculator;
  }

  @Override
  public Double evaluate(final DoubleTimeSeries<?>... x) {
    testInput(x);
    if (x.length < 3) {
      throw new IllegalArgumentException("Need high, low and close time series to calculate high-low-close volatility");
    }
    if (x.length > 3) {
      s_logger.info("Time series array contained more than three series; only using the first three");
    }
    testTimeSeries(x, 2);
    testDatesCoincide(x);
    final DoubleTimeSeries<?> high = x[0];
    final DoubleTimeSeries<?> low = x[1];
    final DoubleTimeSeries<?> close = x[2];
    testHighLowClose(high, low, close);
    final DoubleTimeSeries<?> closeReturns = _returnCalculator.evaluate(close);
    final DoubleTimeSeries<?> highLowReturns = _relativeReturnCalculator.evaluate(new DoubleTimeSeries<?>[] {high, low});
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_relativeReturnCalculator == null) ? 0 : _relativeReturnCalculator.hashCode());
    result = prime * result + ((_returnCalculator == null) ? 0 : _returnCalculator.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final HistoricalVolatilityHighLowCloseCalculator other = (HistoricalVolatilityHighLowCloseCalculator) obj;
    if (!ObjectUtils.equals(_relativeReturnCalculator, other._relativeReturnCalculator)) {
      return false;
    }
    return ObjectUtils.equals(_returnCalculator, other._returnCalculator);
  }

}
