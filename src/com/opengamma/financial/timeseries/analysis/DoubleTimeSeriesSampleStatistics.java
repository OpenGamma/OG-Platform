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
public class DoubleTimeSeriesSampleStatistics {

  public static double getSampleMean(final DoubleTimeSeries ts) {
    double sum = 0;
    long n = 0L;
    for (final Map.Entry<ZonedDateTime, Double> entry : ts) {
      sum += entry.getValue();
      n++;
    }
    return sum / n;
  }

  public static double getSampleVariance(final DoubleTimeSeries ts) {
    final double mean = getSampleMean(ts);
    long n = -1L;
    double sum = 0;
    double diff;
    for (final Map.Entry<ZonedDateTime, Double> entry : ts) {
      diff = entry.getValue() - mean;
      sum += diff * diff;
      n++;
    }
    return sum / n;
  }

  public static double getSampleSkewness(final DoubleTimeSeries ts) {
    return getSampleNthMoment(ts, 3);
  }

  public static double getSampleKurtosis(final DoubleTimeSeries ts) {
    return getSampleNthMoment(ts, 4);
  }

  public static double getSampleNthMoment(final DoubleTimeSeries ts, final int n) {
    final double mean = getSampleMean(ts);
    long m = -1L;
    double sumN = 0;
    double sumSq = 0;
    double diff;
    for (final Map.Entry<ZonedDateTime, Double> entry : ts) {
      diff = entry.getValue() - mean;
      sumN += Math.pow(diff, n);
      sumSq += diff * diff;
      m++;
    }
    final double divisor = Math.pow(sumSq / m, n / 2);
    return sumN / (m * divisor);
  }
}
