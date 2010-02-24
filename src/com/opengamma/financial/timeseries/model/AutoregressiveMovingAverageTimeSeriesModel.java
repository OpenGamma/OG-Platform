/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.model;

import java.util.List;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeriesOperations;

/**
 * 
 * @author emcleod
 */
public class AutoregressiveMovingAverageTimeSeriesModel {
  private final AutoregressiveTimeSeriesModel _arModel;
  private final MovingAverageTimeSeriesModel _maModel;

  public AutoregressiveMovingAverageTimeSeriesModel(final ProbabilityDistribution<Double> random) {
    if (random == null)
      throw new IllegalArgumentException("Probability distribution was null");
    _maModel = new MovingAverageTimeSeriesModel(random);
    _arModel = new AutoregressiveTimeSeriesModel(random);
  }

  public DoubleTimeSeries getSeries(final Double[] phi, final int p, final Double[] theta, final int q, final List<ZonedDateTime> dates) {
    if (phi == null && p != 0)
      throw new IllegalArgumentException("AR coefficient array was null");
    if (p < 0)
      throw new IllegalArgumentException("p must be positive");
    if (phi != null && phi.length < p + 1)
      throw new IllegalArgumentException("AR coefficient array must contain at least " + (p + 1) + " elements");
    if (theta == null && q != 0)
      throw new IllegalArgumentException("MA coefficient array was null");
    if (q < 0)
      throw new IllegalArgumentException("q must be positive");
    if (theta != null && theta.length < q)
      throw new IllegalArgumentException("MA coefficient array must contain at least " + q + " elements");
    if (dates == null)
      throw new IllegalArgumentException("Dates list was null");
    if (dates.isEmpty())
      throw new IllegalArgumentException("Dates list was empty");
    final Double[] theta1 = theta == null ? null : new Double[theta.length + 1];
    if (theta != null) {
      theta1[0] = 0.;
      for (int i = 0; i < theta.length; i++) {
        theta1[i + 1] = theta[i];
      }
    }
    if (p == 0)
      return _maModel.getSeries(theta1, q, dates);
    if (q == 0)
      return _arModel.getSeries(phi, p, dates);
    return DoubleTimeSeriesOperations.add(_arModel.getSeries(phi, p, dates), _maModel.getSeries(theta1, q, dates));
  }
}
