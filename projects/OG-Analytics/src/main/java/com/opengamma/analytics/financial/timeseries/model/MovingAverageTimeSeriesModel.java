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
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class MovingAverageTimeSeriesModel {
  private final ProbabilityDistribution<Double> _random;

  public MovingAverageTimeSeriesModel(final ProbabilityDistribution<Double> random) {
    Validate.notNull(random, "random");
    _random = random;
  }

  public LocalDateDoubleTimeSeries getSeries(final double[] theta, final int q, final LocalDate[] dates) {
    Validate.notNull(theta, "theta");
    if (q < 1) {
      throw new IllegalArgumentException("Order must be greater than zero");
    }
    if (theta.length < q) {
      throw new IllegalArgumentException("Coefficient array must contain at least " + q + " elements");
    }
    Validate.notNull(dates, "dates");
    ArgumentChecker.notEmpty(dates, "dates");
    final int n = dates.length;
    final double[] z = new double[n];
    for (int i = 0; i < n; i++) {
      z[i] = _random.nextRandom();
    }
    final double[] data = new double[n];
    data[0] = theta[0];
    double sum;
    for (int i = 1; i < n; i++) {
      sum = theta[0] + z[i];
      for (int j = 1; j < (i < q ? i : q + 1); j++) {
        sum += z[i - j] * theta[j];
      }
      data[i] = sum;
    }
    return ImmutableLocalDateDoubleTimeSeries.of(dates, data);
  }
}
