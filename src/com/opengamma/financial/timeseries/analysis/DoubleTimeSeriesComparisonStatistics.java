/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.analysis;

import java.util.Map;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 * 
 */
public class DoubleTimeSeriesComparisonStatistics {

  public static double getCovariance(final DoubleTimeSeries a, final DoubleTimeSeries b) {
    // TODO get common data
    if (a.size() != b.size())
      throw new IllegalArgumentException("Time series need to be the same length");
    double sum = 0;
    final double aMu = DoubleTimeSeriesSampleStatistics.getSampleMean(a);
    final double bMu = DoubleTimeSeriesSampleStatistics.getSampleMean(b);
    for (final Map.Entry<ZonedDateTime, Double> entry : a) {
      // TODO getDataPoint is bad inside loop
      sum += (entry.getValue() - aMu) * (b.getDataPoint(entry.getKey()) - bMu);
    }
    return sum / (a.size() - 1);
  }

  public static double getCorrelation(final DoubleTimeSeries a, final DoubleTimeSeries b) {
    if (a.size() != b.size())
      throw new IllegalArgumentException("Time series need to be the same length");
    final double covariance = getCovariance(a, b);
    final double aVariance = DoubleTimeSeriesSampleStatistics.getSampleVariance(a);
    final double bVariance = DoubleTimeSeriesSampleStatistics.getSampleVariance(b);
    return covariance / Math.sqrt(aVariance * bVariance);
  }
}
