/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.analysis;

import static org.junit.Assert.fail;

import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class IIDHypothesisTestCase {
  protected static final DoubleTimeSeries<Long> RANDOM;
  protected static final DoubleTimeSeries<Long> SIGNAL;
  protected static final DoubleTimeSeries<Long> INCREASING;
  static {
    final int n = 5000;
    final long[] dates = new long[n];
    final double[] random = new double[n];
    final double[] signal = new double[n];
    final double[] increasing = new double[n];
    final ProbabilityDistribution<Double> normal = new NormalDistribution(0, 0.5);
    for (int i = 0; i < n; i++) {
      dates[i] = i;
      random[i] = normal.nextRandom();
      signal[i] = Math.cos(i / 10.) + normal.nextRandom();
      increasing[i] = i == 0 ? 1 : increasing[i - 1] * 1.0001;
    }
    final DateTimeNumericEncoding encoding = DateTimeNumericEncoding.TIME_EPOCH_NANOS;
    RANDOM = new FastArrayLongDoubleTimeSeries(encoding, dates, random);
    SIGNAL = new FastArrayLongDoubleTimeSeries(encoding, dates, signal);
    INCREASING = new FastArrayLongDoubleTimeSeries(encoding, dates, increasing);
  }

  public void testNullTS(final IIDHypothesis<DoubleTimeSeries<Long>> h) {
    try {
      h.evaluate((DoubleTimeSeries<Long>) null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  public void testEmptyTS(final IIDHypothesis<DoubleTimeSeries<Long>> h) {
    try {
      h.evaluate(FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }
}
