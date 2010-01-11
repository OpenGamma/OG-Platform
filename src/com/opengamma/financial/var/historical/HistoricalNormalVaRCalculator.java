/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var.historical;

import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class HistoricalNormalVaRCalculator extends HistoricalVaRCalculator {
  private final ProbabilityDistribution<Double> _normal = new NormalDistribution(0, 1);
  private final DoubleTimeSeriesStatisticsCalculator _stdDev;

  public HistoricalNormalVaRCalculator(final DoubleTimeSeriesStatisticsCalculator stdDev) {
    if (stdDev == null)
      throw new IllegalArgumentException("Standard deviation calculator was null");
    _stdDev = stdDev;
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
    final double sigma = _stdDev.evaluate(ts);
    return _normal.getInverseCDF(quantile) * sigma * Math.sqrt(horizon / periods);
  }
}
