/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.util;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;

/**
 * Calculates a weighted volatility series from a series of absolute or relative returns.
 */
public final class TimeSeriesWeightedVolatilityOperator extends Function1D<DateDoubleTimeSeries<?>, DateDoubleTimeSeries<?>> {

  private static final TimeSeriesPercentageChangeOperator PERCENTAGE_CHANGE = new TimeSeriesPercentageChangeOperator();
  private static final TimeSeriesDifferenceOperator ABSOLUTE_CHANGE = new TimeSeriesDifferenceOperator();
  
  private final Function1D<DateDoubleTimeSeries<?>, DateDoubleTimeSeries<?>> _changeOperator;
  
  private final double _lambda;
  
  private TimeSeriesWeightedVolatilityOperator(Function1D<DateDoubleTimeSeries<?>, DateDoubleTimeSeries<?>> changeOperator, 
                                               double lambda) {
    _changeOperator = changeOperator;
    if (lambda <= 0 || lambda > 1) {
      throw new OpenGammaRuntimeException("lambda must be in the range (0, 1]");
    }
    _lambda = lambda;
  }
  
  @Override
  public DateDoubleTimeSeries<?> evaluate(DateDoubleTimeSeries<?> ts) {
    Validate.notNull(ts, "time series");
    Validate.isTrue(ts.size() > 1, "time series length must be > 1");
    DateDoubleTimeSeries<?> percentageChangeSeries = _changeOperator.evaluate(ts);
    int n = percentageChangeSeries.size();
    double[] weightedVariances = new double[n];
    double[] weightedVolatilities = new double[n];
    double oldestPercentageChange = percentageChangeSeries.getEarliestValueFast();
    weightedVariances[0] = oldestPercentageChange * oldestPercentageChange;
    weightedVolatilities[0] = Math.abs(oldestPercentageChange);
    for (int i = 1; i < n; i++) {
      double percentageChange = percentageChangeSeries.getValueAtIndexFast(i);
      weightedVariances[i] = ((1 - _lambda) * percentageChange * percentageChange) + (_lambda * weightedVariances[i - 1]);
      weightedVolatilities[i] = Math.sqrt(weightedVariances[i]);
    }
    
    return ImmutableLocalDateDoubleTimeSeries.of(percentageChangeSeries.timesArrayFast(), weightedVolatilities);
  }
  
  /**
   * Calculates weighted volatilities using the relative difference series
   * @param lambda lambda value to apply
   * @return a TimeSeriesWeightedVolatilityOperator instance
   */
  public static TimeSeriesWeightedVolatilityOperator relative(double lambda) {
    return new TimeSeriesWeightedVolatilityOperator(PERCENTAGE_CHANGE, lambda);
  }

  /**
   * Calculates weighted volatilities using the absolute difference series
   * @param lambda lambda value to apply
   * @return a TimeSeriesWeightedVolatilityOperator instance
   */
  public static TimeSeriesWeightedVolatilityOperator absolute(double lambda) {
    return new TimeSeriesWeightedVolatilityOperator(ABSOLUTE_CHANGE, lambda);
  }

}
