package com.opengamma.financial.timeseries.analysis;

import java.util.Map;

import javax.time.InstantProvider;

import com.opengamma.timeseries.DoubleTimeSeries;

public class DoubleTimeSeriesComparisonStatistics {

  public static double getCovariance(DoubleTimeSeries a, DoubleTimeSeries b) {
    // TODO get common data
    if (a.size() != b.size())
      throw new IllegalArgumentException("Time series need to be the same length");
    double sum = 0;
    double aMu = DoubleTimeSeriesSampleStatistics.getSampleMean(a);
    double bMu = DoubleTimeSeriesSampleStatistics.getSampleMean(b);
    for (Map.Entry<InstantProvider, Double> entry : a) {
      // TODO getDataPoint is bad inside loop
      sum += (entry.getValue() - aMu) * (b.getDataPoint(entry.getKey()) - bMu);
    }
    return sum / (a.size() - 1);
  }

  public static double getCorrelation(DoubleTimeSeries a, DoubleTimeSeries b) {
    if (a.size() != b.size())
      throw new IllegalArgumentException("Time series need to be the same length");
    double covariance = getCovariance(a, b);
    double aVariance = DoubleTimeSeriesSampleStatistics.getSampleVariance(a);
    double bVariance = DoubleTimeSeriesSampleStatistics.getSampleVariance(b);
    return covariance / Math.sqrt(aVariance * bVariance);
  }
}
