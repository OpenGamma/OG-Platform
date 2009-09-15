package com.opengamma.financial.timeseries.analysis;

import java.util.Map;

import javax.time.InstantProvider;

import com.opengamma.timeseries.DoubleTimeSeries;

public class DoubleTimeSeriesSampleStatistics {

  public static double getSampleMean(DoubleTimeSeries ts) {
    double sum = 0;
    long n = 0L;
    for (Map.Entry<InstantProvider, Double> entry : ts) {
      sum += entry.getValue();
      n++;
    }
    return sum / n;
  }

  public static double getSampleVariance(DoubleTimeSeries ts) {
    double mean = getSampleMean(ts);
    long n = -1L;
    double sum = 0;
    double diff;
    for (Map.Entry<InstantProvider, Double> entry : ts) {
      diff = entry.getValue() - mean;
      sum += diff * diff;
      n++;
    }
    return sum / n;
  }

  public static double getSampleSkewness(DoubleTimeSeries ts) {
    return getSampleNthMoment(ts, 3);
  }

  public static double getSampleKurtosis(DoubleTimeSeries ts) {
    return getSampleNthMoment(ts, 4);
  }

  public static double getSampleNthMoment(DoubleTimeSeries ts, int n) {
    double mean = getSampleMean(ts);
    long m = -1L;
    double sumN = 0;
    double sumSq = 0;
    double diff;
    for (Map.Entry<InstantProvider, Double> entry : ts) {
      diff = entry.getValue() - mean;
      sumN += Math.pow(diff, n);
      sumSq += diff * diff;
      m++;
    }
    double divisor = Math.pow(sumSq / m, n / 2);
    return sumN / (m * divisor);
  }
}
