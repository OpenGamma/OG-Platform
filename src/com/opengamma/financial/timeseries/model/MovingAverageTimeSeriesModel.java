/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.model;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class MovingAverageTimeSeriesModel {
  private final ProbabilityDistribution<Double> _random;

  public MovingAverageTimeSeriesModel(final ProbabilityDistribution<Double> random) {
    if (random == null)
      throw new IllegalArgumentException("Probability distribution was null");
    _random = random;
  }

  public DoubleTimeSeries getSeries(final Double[] theta, final int order, final List<ZonedDateTime> dates) {
    if (theta == null)
      throw new IllegalArgumentException("Coefficient array was null");
    if (order < 1)
      throw new IllegalArgumentException("Order must be greater than zero");
    if (theta.length < order + 1)
      throw new IllegalArgumentException("Coefficient array must contain at least " + (order + 1) + " elements");
    if (dates == null)
      throw new IllegalArgumentException("Dates list was null");
    if (dates.isEmpty())
      throw new IllegalArgumentException("Dates list was empty");
    final int n = dates.size();
    final Double[] z = new Double[n];
    for (int i = 0; i < n; i++) {
      z[i] = _random.nextRandom();
    }
    final List<Double> data = new ArrayList<Double>(n);
    data.add(theta[0]);
    double sum;
    for (int i = 1; i < n; i++) {
      sum = theta[0] + z[i];
      for (int j = 1; j < (i < order ? i : order + 1); j++) {
        sum += z[i - j] * theta[j];
      }
      data.add(sum);
    }
    return new ArrayDoubleTimeSeries(dates, data);
  }
}
