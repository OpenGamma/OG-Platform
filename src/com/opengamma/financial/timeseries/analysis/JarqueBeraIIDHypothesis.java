/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.math.statistics.descriptive.SampleFisherKurtosisCalculator;
import com.opengamma.math.statistics.descriptive.SampleSkewnessCalculator;
import com.opengamma.math.statistics.distribution.ChiSquareDistribution;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class JarqueBeraIIDHypothesis<T extends DoubleTimeSeries<?>> extends IIDHypothesis<T> {
  private static final Logger s_Log = LoggerFactory.getLogger(JarqueBeraIIDHypothesis.class);
  private final DoubleTimeSeriesStatisticsCalculator<T> _skewCalculator = new DoubleTimeSeriesStatisticsCalculator<T>(new SampleSkewnessCalculator());
  private final DoubleTimeSeriesStatisticsCalculator<T> _kurtosisCalculator = new DoubleTimeSeriesStatisticsCalculator<T>(new SampleFisherKurtosisCalculator());
  private final double _criticalValue;

  public JarqueBeraIIDHypothesis(final double level) {
    if (level <= 0 || level > 1)
      throw new IllegalArgumentException("Level must be between 0 and 1");
    _criticalValue = new ChiSquareDistribution(2).getInverseCDF(1 - level);
  }

  @Override
  public boolean testIID(final T ts) {
    if (ts.size() < 1000) {
      s_Log.warn("Use of this test is discouraged for time series with fewer than 1000 elements; the result will be inaccurate");
    }
    final int n = ts.size();
    final double skew = Math.pow(_skewCalculator.evaluate(ts), 2);
    final double kurtosis = Math.pow(_kurtosisCalculator.evaluate(ts), 2);
    final double stat = n * (skew + kurtosis / 4) / 6;
    return stat < _criticalValue;
  }
}
