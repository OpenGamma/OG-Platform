/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.model;

import java.util.Arrays;
import java.util.List;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class AutoregressiveTimeSeriesModel {
  private final ProbabilityDistribution<Double> _random;

  public AutoregressiveTimeSeriesModel(final ProbabilityDistribution<Double> random) {
    if (random == null)
      throw new IllegalArgumentException("Probability distribution was null");
    _random = random;
  }

  public DoubleTimeSeries getSeries(final Double[] phi, final int q, final List<ZonedDateTime> dates) {
    if (phi == null)
      throw new IllegalArgumentException("Coefficient array was null");
    if (q < 1)
      throw new IllegalArgumentException("Order must be greater than zero");
    if (phi.length < q + 1)
      throw new IllegalArgumentException("Coefficient array must contain at least " + (q + 1) + " elements");
    if (dates == null)
      throw new IllegalArgumentException("Dates list was null");
    if (dates.isEmpty())
      throw new IllegalArgumentException("Dates list was empty");
    final int n = dates.size();
    final Double[] data = new Double[n];
    data[0] = phi[0] + _random.nextRandom();
    double sum;
    for (int i = 1; i < n; i++) {
      sum = phi[0] + _random.nextRandom();
      for (int j = 1; j < (i < q ? i : q + 1); j++) {
        sum += phi[j] * data[i - j];
      }
      data[i] = sum;
    }
    return new ArrayDoubleTimeSeries(dates, Arrays.asList(data));
  }
}
