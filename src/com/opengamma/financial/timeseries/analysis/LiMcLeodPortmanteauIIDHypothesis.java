/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.ChiSquareDistribution;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class LiMcLeodPortmanteauIIDHypothesis extends IIDHypothesis {
  private static final Logger s_Log = LoggerFactory.getLogger(LiMcLeodPortmanteauIIDHypothesis.class);
  private final Function1D<DoubleTimeSeries<?>, Double[]> _calculator = new AutocorrelationFunctionCalculator();
  private final double _criticalValue;
  private final int _h;

  public LiMcLeodPortmanteauIIDHypothesis(final double level, final int maxLag) {
    if (level <= 0 || level > 1)
      throw new IllegalArgumentException("Level must be between 0 and 1");
    if (maxLag == 0)
      throw new IllegalArgumentException("Lag cannot be zero");
    if (maxLag < 0) {
      s_Log.info("Lag was negative; using absolute value");
    }
    _h = Math.abs(maxLag);
    _criticalValue = new ChiSquareDistribution(_h).getInverseCDF(1 - level);
  }

  @Override
  public boolean testIID(final DoubleTimeSeries<?> x) {
    if (x.size() < _h)
      throw new IllegalArgumentException("Time series must have at least " + _h + " points");
    final FastLongDoubleTimeSeries ts = x.toFastLongDoubleTimeSeries();
    final DoubleTimeSeries<Long> tsSq = ts.multiply(ts);
    final Double[] autocorrelation = _calculator.evaluate(tsSq);
    double q = 0;
    final int n = x.size();
    for (int i = 1; i < _h; i++) {
      q += autocorrelation[i] * autocorrelation[i] / (n - i);
    }
    q *= n * (n + 2);
    return q < _criticalValue;
  }
}
