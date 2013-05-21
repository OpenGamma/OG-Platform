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
 * Calculates a weighted volatility series from a series of absolute returns.
 */
public class TimeSeriesWeightedVolatilityOperator extends Function1D<DateDoubleTimeSeries<?>, DateDoubleTimeSeries<?>> {

  private static final TimeSeriesPercentageChangeOperator PERCENTAGE_CHANGE = new TimeSeriesPercentageChangeOperator();
  
  private final double _lambda;
  
  public TimeSeriesWeightedVolatilityOperator(double lambda) {
    if (lambda <= 0 || lambda > 1) {
      throw new OpenGammaRuntimeException("lambda must be in the range (0, 1]");
    }
    _lambda = lambda;
  }
  
  @Override
  public DateDoubleTimeSeries<?> evaluate(DateDoubleTimeSeries<?> ts) {
    Validate.notNull(ts, "time series");
    Validate.isTrue(ts.size() > 1, "time series length must be > 1");
    DateDoubleTimeSeries<?> percentageChangeSeries = PERCENTAGE_CHANGE.evaluate(ts);
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

}
