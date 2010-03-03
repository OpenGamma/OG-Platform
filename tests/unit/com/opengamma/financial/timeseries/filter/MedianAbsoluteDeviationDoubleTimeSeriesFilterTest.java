/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class MedianAbsoluteDeviationDoubleTimeSeriesFilterTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final double LIMIT = 5;
  private static final double DATA1 = 29;
  private static final double DATA2 = 16;
  private static final TimeSeriesFilter<DoubleTimeSeries<Long>> FILTER = new MedianAbsoluteDeviationDoubleTimeSeriesFilter<DoubleTimeSeries<Long>>(LIMIT);
  private static final int N = 500;
  private static final long[] DATES = new long[N];
  private static final double[] DATA = new double[N];
  private static final DoubleTimeSeries<Long> TS;
  private static final DateTimeNumericEncoding ENCODING = DateTimeNumericEncoding.TIME_EPOCH_SECONDS;
  private static final double EPS = 1e-15;

  static {
    for (int i = 0; i < N; i++) {
      DATES[i] = i;
      DATA[i] = RANDOM.nextDouble();
    }
    DATA[0] = DATA1;
    DATA[1] = DATA2;
    TS = new FastArrayLongDoubleTimeSeries(ENCODING, DATES, DATA);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull() {
    FILTER.evaluate((DoubleTimeSeries<Long>) null);
  }

  @Test
  public void testEmptyTS() {
    final FilteredTimeSeries<DoubleTimeSeries<Long>> filtered = FILTER.evaluate(FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
    assertEquals(filtered.getFilteredTS(), FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
    assertNull(filtered.getRejectedTS());
  }

  @Test
  public void testMasked() {
    final DoubleTimeSeries<Long> subSeries = (DoubleTimeSeries<Long>) TS.subSeries(DATES[0], DATES[10]);
    final FilteredTimeSeries<DoubleTimeSeries<Long>> result = FILTER.evaluate(subSeries);
    test(result, 9);
  }

  @Test
  public void test() {
    test(FILTER.evaluate(TS), 498);
  }

  private void test(final FilteredTimeSeries<DoubleTimeSeries<Long>> result, final int size) {
    assertEquals(result.getFilteredTS().size(), size);
    final DoubleTimeSeries<Long> rejected = result.getRejectedTS();
    assertEquals(rejected.getTime(0), 0, EPS);
    assertEquals(rejected.getValueAt(0), DATA1, EPS);
    assertEquals(rejected.getTime(1), 1, EPS);
    assertEquals(rejected.getValueAt(1), DATA2, EPS);
  }
}
