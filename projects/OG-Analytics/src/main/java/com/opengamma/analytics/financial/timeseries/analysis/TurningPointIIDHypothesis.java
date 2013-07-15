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
public class TurningPointIIDHypothesis extends IIDHypothesis {
  private final double _criticalValue;

  public TurningPointIIDHypothesis(final double level) {
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
    double x0, x1, x2;
    for (int i = 1; i < n - 1; i++) {
      x0 = data[i - 1];
      x1 = data[i];
      x2 = data[i + 1];
      if (x1 > x0 && x1 > x2) {
        t++;
      } else if (x1 < x0 && x1 < x2) {
        t++;
      }
    }
    final double mean = 2 * (n - 2.) / 3.;
    final double std = Math.sqrt((16 * n - 29.) / 90.);
    return Math.abs(t - mean) / std < _criticalValue;
  }
}
