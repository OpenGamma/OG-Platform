/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.analysis;

import org.testng.Assert;
import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;

import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.instant.ImmutableInstantDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Abstract test.
 */
@Test(groups = TestGroup.UNIT)
public abstract class IIDHypothesisTestCase {
  protected static final ImmutableInstantDoubleTimeSeries RANDOM;
  protected static final ImmutableInstantDoubleTimeSeries SIGNAL;
  protected static final ImmutableInstantDoubleTimeSeries INCREASING;
  static {
    final int n = 5000;
    final long[] dates = new long[n];
    final double[] random = new double[n];
    final double[] signal = new double[n];
    final double[] increasing = new double[n];
    final ProbabilityDistribution<Double> normal = new NormalDistribution(0, 0.5, new MersenneTwister64(MersenneTwister.DEFAULT_SEED));
    for (int i = 0; i < n; i++) {
      dates[i] = i;
      random[i] = normal.nextRandom();
      signal[i] = Math.cos(i / 10.) + normal.nextRandom();
      increasing[i] = i == 0 ? 1 : increasing[i - 1] * 1.0001;
    }
    RANDOM = ImmutableInstantDoubleTimeSeries.of(dates, random);
    SIGNAL = ImmutableInstantDoubleTimeSeries.of(dates, signal);
    INCREASING = ImmutableInstantDoubleTimeSeries.of(dates, increasing);
  }

  public void assertNullTS(final IIDHypothesis h) {
    try {
      h.evaluate((DoubleTimeSeries<Long>) null);
      Assert.fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  public void assertEmptyTS(final IIDHypothesis h) {
    try {
      h.evaluate(ImmutableInstantDoubleTimeSeries.EMPTY_SERIES);
      Assert.fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }
}
