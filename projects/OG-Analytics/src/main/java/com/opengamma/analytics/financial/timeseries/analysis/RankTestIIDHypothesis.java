/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.analysis;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class RankTestIIDHypothesis extends IIDHypothesis {
  private final double _criticalValue;

  public RankTestIIDHypothesis(final double level) {
    if (!ArgumentChecker.isInRangeExcludingLow(0, 1, level)) {
      throw new IllegalArgumentException("Level must be between 0 and 1");
    }
    _criticalValue = new NormalDistribution(0, 1).getInverseCDF(1 - level / 2.);
  }

  @Override
  public boolean testIID(final DoubleTimeSeries<?> x) {
    Validate.notNull(x, "x");
    final double[] data = x.valuesArrayFast();
    int t = 0;
    final int n = x.size();
    double val;
    for (int i = 0; i < n - 1; i++) {
      val = data[i];
      for (int j = i + 1; j < n; j++) {
        if (data[j] > val) {
          t++;
        }
      }
    }
    final double mean = n * (n - 1) / 4.;
    final double std = Math.sqrt(n * (n - 1) * (2 * n + 5.) / 72.);
    return Math.abs(t - mean) / std < _criticalValue;
  }

}
