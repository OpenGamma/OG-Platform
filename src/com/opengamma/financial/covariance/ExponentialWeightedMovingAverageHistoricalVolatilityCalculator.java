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
public class ExponentialWeightedMovingAverageHistoricalVolatilityCalculator extends HistoricalVolatilityCalculator {
  private static final Logger s_logger = LoggerFactory.getLogger(ExponentialWeightedMovingAverageHistoricalVolatilityCalculator.class);
  private final TimeSeriesReturnCalculator _returnCalculator;
  private final double _lambda;
  private final double _lambdaM1;

  public ExponentialWeightedMovingAverageHistoricalVolatilityCalculator(final double lambda, final TimeSeriesReturnCalculator returnCalculator) {
    this(lambda, returnCalculator, getCalculationMode());
  }

  public ExponentialWeightedMovingAverageHistoricalVolatilityCalculator(final double lambda, final TimeSeriesReturnCalculator returnCalculator, final CalculationMode mode) {
    this(lambda, returnCalculator, mode, getDefaultBadDataPoints());
  }

  public ExponentialWeightedMovingAverageHistoricalVolatilityCalculator(final double lambda, final TimeSeriesReturnCalculator returnCalculator, final CalculationMode mode,
      final double percentBadDataPoints) {
    super(mode, percentBadDataPoints);
    checkLambda(lambda);
    _lambda = lambda;
    _lambdaM1 = 1 - lambda;
    _returnCalculator = returnCalculator;
  }

  private void checkLambda(final double lambda) {
    if (lambda < 0) {
      s_logger.warn("Weight for EWMA series is less than zero; this is probably not what was intended");
    }
    if (lambda > 1) {
      s_logger.warn("Weight for EWMA series is greater than one; this is probably not what was intended");
    }
  }

  @Override
  public Double evaluate(final DoubleTimeSeries<?>... x) {
    testInput(x);
    testTimeSeries(x, 3);
    testDatesCoincide(x);
    final DoubleTimeSeries<?> returnTS = _returnCalculator.evaluate(x);
    final Iterator<Double> iter = returnTS.valuesIterator();
    double returnValue = iter.next();
    double variance = returnValue * returnValue;
    while (iter.hasNext()) {
      returnValue = iter.next();
      variance = _lambda * variance + _lambdaM1 * returnValue * returnValue;
    }
    return Math.sqrt(variance);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_lambda);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_lambdaM1);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final ExponentialWeightedMovingAverageHistoricalVolatilityCalculator other = (ExponentialWeightedMovingAverageHistoricalVolatilityCalculator) obj;
    if (Double.doubleToLongBits(_lambda) != Double.doubleToLongBits(other._lambda)) {
      return false;
    }
    if (Double.doubleToLongBits(_lambdaM1) != Double.doubleToLongBits(other._lambdaM1)) {
      return false;
    }
    return ObjectUtils.equals(_returnCalculator, other._returnCalculator);
  }
}
