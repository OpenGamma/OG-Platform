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
import com.opengamma.analytics.math.statistics.distribution.ChiSquareDistribution;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class LiMcLeodPortmanteauIIDHypothesis extends IIDHypothesis {
  private static final Logger s_logger = LoggerFactory.getLogger(LiMcLeodPortmanteauIIDHypothesis.class);
  private final Function1D<DoubleTimeSeries<?>, double[]> _calculator = new AutocorrelationFunctionCalculator();
  private final double _criticalValue;
  private final int _h;

  public LiMcLeodPortmanteauIIDHypothesis(final double level, final int maxLag) {
    if (!ArgumentChecker.isInRangeExcludingLow(0, 1, level)) {
      throw new IllegalArgumentException("Level must be between 0 and 1");
    }
    if (maxLag == 0) {
      throw new IllegalArgumentException("Lag cannot be zero");
    }
    if (maxLag < 0) {
      s_logger.info("Lag was negative; using absolute value");
    }
    _h = Math.abs(maxLag);
    _criticalValue = new ChiSquareDistribution(_h).getInverseCDF(1 - level);
  }

  @Override
  public boolean testIID(final DoubleTimeSeries<?> x) {
    Validate.notNull(x, "x");
    if (x.size() < _h) {
      throw new IllegalArgumentException("Time series must have at least " + _h + " points");
    }
    final DoubleTimeSeries<?> tsSq = x.multiply(x);
    final double[] autocorrelation = _calculator.evaluate(tsSq);
    double q = 0;
    final int n = x.size();
    for (int i = 1; i < _h; i++) {
      q += autocorrelation[i] * autocorrelation[i] / (n - i);
    }
    q *= n * (n + 2);
    return q < _criticalValue;
  }

}
