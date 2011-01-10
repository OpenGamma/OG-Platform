/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.filter;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 * 
 */
public class NegativeValueDoubleTimeSeriesFilterTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final TimeSeriesFilter FILTER = new NegativeValueDoubleTimeSeriesFilter();
  private static final DateTimeNumericEncoding ENCODING = DateTimeNumericEncoding.TIME_EPOCH_NANOS;

  @Test(expected = IllegalArgumentException.class)
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
  public void test() {
    final int n = 100;
    final long[] dates = new long[n];
    final double[] data = new double[n];
    final long[] filteredDates = new long[n];
    final double[] filteredData = new double[n];
    final long[] rejectedDates = new long[n];
    final double[] rejectedData = new double[n];
    Double d;
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      d = RANDOM.nextDouble();
      dates[i] = i;
      if (d < 0.25) {
        data[i] = -d;
        rejectedDates[k] = i;
        rejectedData[k++] = -d;
      } else {
        data[i] = d;
        filteredDates[j] = i;
        filteredData[j++] = d;
      }
    }
    final FilteredTimeSeries result = FILTER.evaluate(new FastArrayLongDoubleTimeSeries(ENCODING, dates, data));
    assertEquals(result, new FilteredTimeSeries(new FastArrayLongDoubleTimeSeries(ENCODING, Arrays.copyOf(filteredDates, j), Arrays.copyOf(filteredData, j)),
        new FastArrayLongDoubleTimeSeries(ENCODING, Arrays.copyOf(rejectedDates, k), Arrays.copyOf(rejectedData, k))));
  }
}
