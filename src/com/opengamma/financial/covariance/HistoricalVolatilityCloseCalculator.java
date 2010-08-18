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

import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.util.CalculationMode;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class HistoricalVolatilityCloseCalculator extends HistoricalVolatilityCalculator {
  private static final Logger s_logger = LoggerFactory.getLogger(HistoricalVolatilityCloseCalculator.class);
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
      s_logger.info("Time series array contained more than one series; only using the first one");
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
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
    final HistoricalVolatilityCloseCalculator other = (HistoricalVolatilityCloseCalculator) obj;
    return ObjectUtils.equals(_returnCalculator, other._returnCalculator);
  }

}
