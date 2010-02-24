/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.analysis;

import static org.junit.Assert.fail;

import javax.time.calendar.TimeZone;

import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class IIDHypothesisTestCase {
  protected static final DoubleTimeSeries RANDOM;
  protected static final DoubleTimeSeries SIGNAL;
  protected static final DoubleTimeSeries INCREASING;
  static {
    final int n = 5000;
    final long[] dates = new long[n];
    final double[] random = new double[n];
    final double[] signal = new double[n];
    final double[] increasing = new double[n];
    final TimeZone[] zones = new TimeZone[n];
    final ProbabilityDistribution<Double> normal = new NormalDistribution(0, 0.5);
    for (int i = 0; i < n; i++) {
      dates[i] = i;
      random[i] = normal.nextRandom();
      signal[i] = Math.cos(i / 10.) + normal.nextRandom();
      increasing[i] = i == 0 ? 1 : increasing[i - 1] * 1.0001;
      zones[i] = TimeZone.UTC;
    }
    RANDOM = new ArrayDoubleTimeSeries(dates, random, zones);
    SIGNAL = new ArrayDoubleTimeSeries(dates, signal, zones);
    INCREASING = new ArrayDoubleTimeSeries(dates, increasing, zones);
  }

  public void testNullTS(final IIDHypothesis h) {
    try {
      h.evaluate((DoubleTimeSeries) null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  public void testEmptyTS(final IIDHypothesis h) {
    try {
      h.evaluate(ArrayDoubleTimeSeries.EMPTY_SERIES);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }
}
