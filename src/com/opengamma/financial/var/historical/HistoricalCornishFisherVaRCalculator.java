/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.historical;

import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.math.statistics.distribution.NormalProbabilityDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class HistoricalCornishFisherVaRCalculator extends HistoricalVaRCalculator {
  private final ProbabilityDistribution<Double> _normal = new NormalProbabilityDistribution(0, 1);
  private final DoubleTimeSeriesStatisticsCalculator _mean;
  private final DoubleTimeSeriesStatisticsCalculator _variance;
  private final DoubleTimeSeriesStatisticsCalculator _skewness;
  private final DoubleTimeSeriesStatisticsCalculator _kurtosis;

  public HistoricalCornishFisherVaRCalculator(final DoubleTimeSeriesStatisticsCalculator mean, final DoubleTimeSeriesStatisticsCalculator variance,
      final DoubleTimeSeriesStatisticsCalculator skewness, final DoubleTimeSeriesStatisticsCalculator kurtosis) {
    if (mean == null)
      throw new IllegalArgumentException("Mean calculator was null");
    if (variance == null)
      throw new IllegalArgumentException("Variance calculator was null");
    if (skewness == null)
      throw new IllegalArgumentException("Skewness calculator was null");
    if (kurtosis == null)
      throw new IllegalArgumentException("Kurtosis calculator was null");
    _mean = mean;
    _variance = variance;
    _skewness = skewness;
    _kurtosis = kurtosis;
  }

  @Override
  public Double evaluate(final DoubleTimeSeries ts, final double periods, final double horizon, final double quantile) {
    if (ts == null)
      throw new IllegalArgumentException("Time series was null");
    if (ts.isEmpty())
      throw new IllegalArgumentException("Time series was empty");
    if (periods <= 0)
      throw new IllegalArgumentException("Number of periods must be greater than zero");
    if (horizon <= 0)
      throw new IllegalArgumentException("Horizon must be greater than zero");
    if (quantile <= 0 || quantile >= 1)
      throw new IllegalArgumentException("Quantile must be between 0 and 1");
    final double z = _normal.getInverseCDF(quantile);
    final double zSq = z * z;
    final double mu = _mean.evaluate(ts);
    final double variance = _variance.evaluate(ts);
    final double t = _skewness.evaluate(ts);
    final double k = _kurtosis.evaluate(ts);
    final double x = z + t * (zSq - 1) / 6. + k * z * (zSq - 3) / 24. - t * t * z * (2 * zSq - 5) / 36.;
    final double mult = horizon / periods;
    return x * mu * mult + Math.sqrt(mult * variance);
  }
}
