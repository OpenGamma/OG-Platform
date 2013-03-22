/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.analysis;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.math.statistics.descriptive.SampleFisherKurtosisCalculator;
import com.opengamma.analytics.math.statistics.descriptive.SampleSkewnessCalculator;
import com.opengamma.analytics.math.statistics.distribution.ChiSquareDistribution;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class JarqueBeraIIDHypothesis extends IIDHypothesis {
  private static final Logger s_logger = LoggerFactory.getLogger(JarqueBeraIIDHypothesis.class);
  private final DoubleTimeSeriesStatisticsCalculator _skewCalculator = new DoubleTimeSeriesStatisticsCalculator(new SampleSkewnessCalculator());
  private final DoubleTimeSeriesStatisticsCalculator _kurtosisCalculator = new DoubleTimeSeriesStatisticsCalculator(new SampleFisherKurtosisCalculator());
  private final double _criticalValue;

  public JarqueBeraIIDHypothesis(final double level) {
    if (!ArgumentChecker.isInRangeExcludingLow(0, 1, level)) {
      throw new IllegalArgumentException("Level must be between 0 and 1");
    }
    _criticalValue = new ChiSquareDistribution(2).getInverseCDF(1 - level);
  }

  @Override
  public boolean testIID(final DoubleTimeSeries<?> ts) {
    Validate.notNull(ts);
    if (ts.size() < 1000) {
      s_logger.warn("Use of this test is discouraged for time series with fewer than 1000 elements; the result will be inaccurate");
    }
    final int n = ts.size();
    final double skew = Math.pow(_skewCalculator.evaluate(ts), 2);
    final double kurtosis = Math.pow(_kurtosisCalculator.evaluate(ts), 2);
    final double stat = n * (skew + kurtosis / 4) / 6;
    return stat < _criticalValue;
  }
}
