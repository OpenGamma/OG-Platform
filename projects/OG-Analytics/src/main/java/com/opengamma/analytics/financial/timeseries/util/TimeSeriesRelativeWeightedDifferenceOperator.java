/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.util;

import com.opengamma.analytics.math.function.Function2D;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;

/**
 * 
 */
public class TimeSeriesRelativeWeightedDifferenceOperator extends Function2D<DateDoubleTimeSeries<?>, DateDoubleTimeSeries<?>> {

  private static final TimeSeriesDifferenceOperator DIFFERENCE = new TimeSeriesDifferenceOperator();
  
  @Override
  public DateDoubleTimeSeries<?> evaluate(DateDoubleTimeSeries<?> ts, DateDoubleTimeSeries<?> weights) {
    DateDoubleTimeSeries<?> differenceSeries = DIFFERENCE.evaluate(ts);
    if (differenceSeries.size() != weights.size()) {
      throw new IllegalArgumentException("Difference series has " + differenceSeries.size() + " points but weighting series has " + weights.size());
    }
    int n = differenceSeries.size();
    double endWeight = weights.getLatestValueFast();
    double[] weightedDifferences = new double[n];
    for (int i = 0; i < n; i++) {
      weightedDifferences[i] = differenceSeries.getValueAtIndexFast(i) * endWeight / weights.getValueAtIndexFast(i); 
    }
    return ImmutableLocalDateDoubleTimeSeries.of(differenceSeries.timesArrayFast(), weightedDifferences);
  }

}
