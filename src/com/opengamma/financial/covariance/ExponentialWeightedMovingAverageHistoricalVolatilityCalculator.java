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
public class ExponentialWeightedMovingAverageHistoricalVolatilityCalculator<T extends DoubleTimeSeries<?>> extends HistoricalVolatilityCalculator<T> {
  private static final Logger s_Log = LoggerFactory.getLogger(ExponentialWeightedMovingAverageHistoricalVolatilityCalculator.class);
  private final TimeSeriesReturnCalculator<T> _returnCalculator;
  private final double _lambda;
  private final double _lambdaM1;

  public ExponentialWeightedMovingAverageHistoricalVolatilityCalculator(final double lambda, final TimeSeriesReturnCalculator<T> returnCalculator) {
    super();
    checkLambda(lambda);
    _lambda = lambda;
    _lambdaM1 = 1 - lambda;
    _returnCalculator = returnCalculator;
  }

  public ExponentialWeightedMovingAverageHistoricalVolatilityCalculator(final double lambda, final TimeSeriesReturnCalculator<T> returnCalculator, final CalculationMode mode) {
    super(mode);
    checkLambda(lambda);
    _lambda = lambda;
    _lambdaM1 = 1 - lambda;
    _returnCalculator = returnCalculator;
  }

  public ExponentialWeightedMovingAverageHistoricalVolatilityCalculator(final double lambda, final TimeSeriesReturnCalculator<T> returnCalculator, final CalculationMode mode,
      final double percentBadDataPoints) {
    super(mode, percentBadDataPoints);
    checkLambda(lambda);
    _lambda = lambda;
    _lambdaM1 = 1 - lambda;
    _returnCalculator = returnCalculator;
  }

  private void checkLambda(final double lambda) {
    if (lambda < 0) {
      s_Log.warn("Weight for EWMA series is less than zero; this is probably not what was intended");
    }
    if (lambda > 1) {
      s_Log.warn("Weight for EWMA series is greater than one; this is probably not what was intended");
    }
  }

  @Override
  public Double evaluate(final T... x) {
    testInput(x);
    testTimeSeries(x, 3);
    testDatesCoincide(x);
    final DoubleTimeSeries<Long> returnTS = _returnCalculator.evaluate(x);
    final Iterator<Double> iter = returnTS.valuesIterator();
    double returnValue = iter.next();
    double variance = returnValue * returnValue;
    while (iter.hasNext()) {
      returnValue = iter.next();
      variance = _lambda * variance + _lambdaM1 * returnValue * returnValue;
    }
    return Math.sqrt(variance);
  }
}
