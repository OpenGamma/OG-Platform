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
    final HistoricalVolatilityHighLowCalculator other = (HistoricalVolatilityHighLowCalculator) obj;
    return ObjectUtils.equals(_returnCalculator, other._returnCalculator);
  }
}
