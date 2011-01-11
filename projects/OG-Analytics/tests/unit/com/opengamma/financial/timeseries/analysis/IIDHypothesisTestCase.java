/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.analysis;

import static org.junit.Assert.fail;
import cern.jet.random.engine.MersenneTwister64;

import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 * 
 */
public class IIDHypothesisTestCase {
  protected static final DoubleTimeSeries<Long> RANDOM;
  protected static final DoubleTimeSeries<Long> SIGNAL;
  protected static final DoubleTimeSeries<Long> INCREASING;
  protected static final DateTimeNumericEncoding ENCODING = DateTimeNumericEncoding.TIME_EPOCH_NANOS;
  static {
    final int n = 5000;
    final long[] dates = new long[n];
    final double[] random = new double[n];
    final double[] signal = new double[n];
    final double[] increasing = new double[n];
    final ProbabilityDistribution<Double> normal = new NormalDistribution(0, 0.5, new MersenneTwister64(MersenneTwister64.DEFAULT_SEED));
    for (int i = 0; i < n; i++) {
      dates[i] = i;
      random[i] = normal.nextRandom();
      signal[i] = Math.cos(i / 10.) + normal.nextRandom();
      increasing[i] = i == 0 ? 1 : increasing[i - 1] * 1.0001;
    }
    RANDOM = new FastArrayLongDoubleTimeSeries(ENCODING, dates, random);
    SIGNAL = new FastArrayLongDoubleTimeSeries(ENCODING, dates, signal);
    INCREASING = new FastArrayLongDoubleTimeSeries(ENCODING, dates, increasing);
  }

  public void testNullTS(final IIDHypothesis h) {
    try {
      h.evaluate((DoubleTimeSeries<Long>) null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  public void testEmptyTS(final IIDHypothesis h) {
    try {
      h.evaluate(FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }
}
