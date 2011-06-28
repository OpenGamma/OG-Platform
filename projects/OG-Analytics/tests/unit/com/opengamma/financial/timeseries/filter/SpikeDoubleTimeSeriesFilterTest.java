/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.filter;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 * 
 */
public class SpikeDoubleTimeSeriesFilterTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final TimeSeriesFilter FILTER = new SpikeDoubleTimeSeriesFilter(100);
  private static final DateTimeNumericEncoding ENCODING = DateTimeNumericEncoding.TIME_EPOCH_MILLIS;
  private static final int N = 100;
  private static final long[] DATES = new long[N];
  private static final double[] DATA = new double[N];

  static {
    final double value = 0.5;
    double random;
    for (int i = 0; i < N; i++) {
      random = RANDOM.nextDouble();
      DATES[i] = i;
      DATA[i] = value * (random < 0.5 ? 1 - random : 1 + random);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTS() {
    FILTER.evaluate((DoubleTimeSeries<Long>) null);
  }

  @Test
  public void testEmptyTS() {
    final FilteredTimeSeries filtered = FILTER.evaluate(FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
    assertEquals(filtered.getFilteredTS(), FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
    assertEquals(filtered.getRejectedTS(), FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
  }

  @Test
  public void testInitialSpike() {
    final double[] data = Arrays.copyOf(DATA, N);
    data[0] = 100.;
    final DoubleTimeSeries<Long> ts = new FastArrayLongDoubleTimeSeries(ENCODING, DATES, data);
    final DoubleTimeSeries<Long> rejected = FILTER.evaluate(ts).getRejectedTS().toFastLongDoubleTimeSeries();
    assertEquals(rejected.size(), 1);
    assertEquals(rejected.getTime(0), ts.getTime(0));
    assertEquals(rejected.getValueAt(0), ts.getValueAt(0));
  }

  @Test
  public void testSpike() {
    final double[] data = Arrays.copyOf(DATA, N);
    data[10] = 100.;
    DoubleTimeSeries<Long> ts = new FastArrayLongDoubleTimeSeries(ENCODING, DATES, data);
    FilteredTimeSeries filtered = FILTER.evaluate(ts);
    assertSeries(ts, filtered, 10);
    data[10] = -100.;
    ts = new FastArrayLongDoubleTimeSeries(ENCODING, DATES, data);
    filtered = FILTER.evaluate(ts);
    assertSeries(ts, filtered, 10);
  }

  private void assertSeries(final DoubleTimeSeries<Long> ts, final FilteredTimeSeries filtered, final int index) {
    final DoubleTimeSeries<Long> rejected = filtered.getRejectedTS().toFastLongDoubleTimeSeries();
    assertEquals(rejected.size(), 1);
    assertEquals(rejected.getTime(0), ts.getTime(index));
    assertEquals(rejected.getValueAt(0), ts.getValueAt(index));
    assertEquals(filtered.getFilteredTS().size(), 99);
  }
}
