/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.analysis;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class SampleAutocorrelationIIDHypothesis extends IIDHypothesis {
  private static final Logger s_logger = LoggerFactory.getLogger(SampleAutocorrelationIIDHypothesis.class);
  private final Function1D<DoubleTimeSeries<?>, double[]> _calculator = new AutocorrelationFunctionCalculator();
  private final double _level;
  private final double _criticalValue;
  private final int _h;

  public SampleAutocorrelationIIDHypothesis(final double level, final int maxLag) {
    if (!ArgumentChecker.isInRangeExcludingLow(0, 1, level)) {
      throw new IllegalArgumentException("Level must be between 0 and 1");
    }
    if (maxLag == 0) {
      throw new IllegalArgumentException("Lag cannot be zero");
    }
    if (maxLag < 0) {
      s_logger.warn("Maximum lag was less than zero; using absolute value");
    }
    _level = level;
    final ProbabilityDistribution<Double> normal = new NormalDistribution(0, 1);
    _criticalValue = normal.getInverseCDF(1 - level / 2.);
    _h = maxLag;
  }

  @Override
  public boolean testIID(final DoubleTimeSeries<?> x) {
    Validate.notNull(x, "x");
    if (x.size() < _h) {
      throw new IllegalArgumentException("Time series must have at least " + _h + " points");
    }
    final double[] autocorrelations = _calculator.evaluate(x);
    final double upper = _criticalValue / Math.sqrt(x.size());
    final double lower = -upper;
    double violations = 0;
    double value;
    for (int i = 1; i < _h; i++) {
      value = autocorrelations[i];
      if (value > upper || value < lower) {
        violations++;
      }
    }
    if (violations / _h > _level) {
      return false;
    }
    return true;
  }
}
