/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.model;

import org.apache.commons.lang.Validate;
import org.threeten.bp.LocalDate;

import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class AutoregressiveMovingAverageTimeSeriesModel {
  private final AutoregressiveTimeSeriesModel _arModel;
  private final MovingAverageTimeSeriesModel _maModel;

  public AutoregressiveMovingAverageTimeSeriesModel(final ProbabilityDistribution<Double> random) {
    Validate.notNull(random, "random");
    _maModel = new MovingAverageTimeSeriesModel(random);
    _arModel = new AutoregressiveTimeSeriesModel(random);
  }

  public LocalDateDoubleTimeSeries getSeries(final double[] phi, final int p, final double[] theta, final int q, final LocalDate[] dates) {
    if (phi == null && p != 0) {
      throw new IllegalArgumentException("AR coefficient array was null");
    }
    if (p < 0) {
      throw new IllegalArgumentException("p must be positive");
    }
    if (phi != null && phi.length < p + 1) {
      throw new IllegalArgumentException("AR coefficient array must contain at least " + (p + 1) + " elements");
    }
    if (theta == null && q != 0) {
      throw new IllegalArgumentException("MA coefficient array was null");
    }
    if (q < 0) {
      throw new IllegalArgumentException("q must be positive");
    }
    if (theta != null && theta.length < q) {
      throw new IllegalArgumentException("MA coefficient array must contain at least " + q + " elements");
    }
    Validate.notNull(dates, "dates");
    ArgumentChecker.notEmpty(dates, "dates");
    if (theta != null) {
      final double[] theta1 = new double[theta.length + 1];
      theta1[0] = 0.;
      for (int i = 0; i < theta.length; i++) {
        theta1[i + 1] = theta[i];
      }
      if (p == 0) {
        return _maModel.getSeries(theta1, q, dates);
      }
      if (q == 0) {
        return _arModel.getSeries(phi, p, dates);
      }
      return _arModel.getSeries(phi, p, dates).add(_maModel.getSeries(theta1, q, dates));
    }
    return _arModel.getSeries(phi, p, dates);
  }

}
