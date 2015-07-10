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
 * Function taking a timeseries and a series of weightings, producing
 * a new timeseries where the entries in the timeseries are
 * weighted according to the values in the weighting series.
 */
public class TimeSeriesRelativeWeightedDifferenceOperator extends Function2D<DateDoubleTimeSeries<?>, DateDoubleTimeSeries<?>> {

  /**
   * Operator to compute difference between the entries in a timeseries.
   */
  private static final TimeSeriesDifferenceOperator DIFFERENCE = new TimeSeriesDifferenceOperator();

  /**
   * Function taking a timeseries and a series of weightings, producing
   * a new timeseries where the entries in the timeseries are
   * weighted according to the values in the weighting series.
   * <p>
   * The steps are as follows:
   * <ul>
   * <li>create a new series ds, containing the difference between each
   *    element in the timeseries</li>
   * <li>validate that ds is the same size as the weights series</li>
   * <li>create a new series where each element is the corresponding
   *    element in ds, divided by the corresponding weighting and
   *    multiplied by the final weighting. If any weighting is zero
   *    then a zero entry will be written (see PLT-426).
   * </ul>
   *
   * @param ts  the timeseries to calculate weighted difference for
   * @param weights  the series of weights to be used. This series
   *     must be 1 element shorter than the timeseries.
   * @return a new weighted timeseries, equal in length to the weights
   *     used
   */
  @Override
  public DateDoubleTimeSeries<?> evaluate(DateDoubleTimeSeries<?> ts, DateDoubleTimeSeries<?> weights) {
    DateDoubleTimeSeries<?> differenceSeries = DIFFERENCE.evaluate(ts);
    if (differenceSeries.size() != weights.size()) {
      throw new IllegalArgumentException("Difference series has " + differenceSeries.size() +
                                             " points but weighting series has " + weights.size());
    }
    int n = differenceSeries.size();
    double endWeight = weights.getLatestValueFast();
    double[] weightedDifferences = new double[n];
    for (int i = 0; i < n; i++) {
      double weight = weights.getValueAtIndexFast(i);
      weightedDifferences[i] = weight == 0 ? 0 : differenceSeries.getValueAtIndexFast(i) * endWeight / weight;
    }
    return ImmutableLocalDateDoubleTimeSeries.of(differenceSeries.timesArrayFast(), weightedDifferences);
  }

}
