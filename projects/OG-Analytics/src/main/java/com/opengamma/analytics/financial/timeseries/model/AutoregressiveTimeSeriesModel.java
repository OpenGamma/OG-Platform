/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.model;

import org.apache.commons.lang.Validate;
import org.threeten.bp.LocalDate;

import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class AutoregressiveTimeSeriesModel {
  private final ProbabilityDistribution<Double> _random;

  public AutoregressiveTimeSeriesModel(final ProbabilityDistribution<Double> random) {
    Validate.notNull(random, "random");
    _random = random;
  }

  public LocalDateDoubleTimeSeries getSeries(final double[] phi, final int p, final LocalDate[] dates) {
    Validate.notNull(phi, "phi");
    if (p < 1) {
      throw new IllegalArgumentException("Order must be greater than zero");
    }
    if (phi.length < p + 1) {
      throw new IllegalArgumentException("Coefficient array must contain at least " + (p + 1) + " elements");
    }
    Validate.notNull(dates, "dates");
    if (dates.length == 0) {
      throw new IllegalArgumentException("Dates array was empty");
    }
    final int n = dates.length;
    final double[] data = new double[n];
    data[0] = phi[0] + _random.nextRandom();
    double sum;
    for (int i = 1; i < n; i++) {
      sum = phi[0] + _random.nextRandom();
      for (int j = 1; j < (i < p ? i : p + 1); j++) {
        sum += phi[j] * data[i - j];
      }
      data[i] = sum;
    }
    return ImmutableLocalDateDoubleTimeSeries.of(dates, data);
  }
}
