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
public class DifferenceSignIIDHypothesis extends IIDHypothesis {
  private final double _criticalValue;

  public DifferenceSignIIDHypothesis(final double level) {
    if (!ArgumentChecker.isInRangeExcludingLow(0, 1, level)) {
      throw new IllegalArgumentException("Level must be between 0 and 1");
    }
    _criticalValue = new NormalDistribution(0, 1).getInverseCDF(1 - level / 2.);
  }

  @Override
  public boolean testIID(final DoubleTimeSeries<?> x) {
    Validate.notNull(x, "x");
    final double[] data = x.valuesArrayFast();
    final int n = data.length;
    int t = 0;
    for (int i = 1; i < n; i++) {
      if (data[i] > data[i - 1]) {
        t++;
      }
    }
    final double mean = (n - 1) / 2.;
    final double std = Math.sqrt((n + 1) / 12.);
    return Math.abs(t - mean) / std < _criticalValue;
  }
}
