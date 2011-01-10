/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.filter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cern.colt.Arrays;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 * 
 */
public class ZeroValueDoubleTimeSeriesFilterTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final ZeroValueDoubleTimeSeriesFilter SMALL_ZERO_FILTER = new ZeroValueDoubleTimeSeriesFilter();
  private static final ZeroValueDoubleTimeSeriesFilter LARGE_ZERO_FILTER = new ZeroValueDoubleTimeSeriesFilter(1e-3);

  @Test(expected = IllegalArgumentException.class)
  public void testNullTS() {
    SMALL_ZERO_FILTER.evaluate((DoubleTimeSeries<Long>) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeZero() {
    new ZeroValueDoubleTimeSeriesFilter(-1e-12);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetNegativeZero() {
    SMALL_ZERO_FILTER.setZero(-1e-12);
  }

  @Test
  public void testEmptyTS() {
    final FilteredTimeSeries filtered = SMALL_ZERO_FILTER.evaluate(FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
    assertEquals(filtered.getFilteredTS(), FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
    assertEquals(filtered.getRejectedTS(), FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
  }

  @Test
  public void test() {
    final DateTimeNumericEncoding encoding = DateTimeNumericEncoding.DATE_EPOCH_DAYS;
    final int n = 100;
    final long[] dates = new long[n];
    final double[] data = new double[n];
    long[] smallZeroFilteredDates = new long[n];
    double[] smallZeroFilteredData = new double[n];
    long[] smallZeroRejectedDates = new long[n];
    double[] smallZeroRejectedData = new double[n];
    long[] largeZeroFilteredDates = new long[n];
    double[] largeZeroFilteredData = new double[n];
    long[] largeZeroRejectedDates = new long[n];
    double[] largeZeroRejectedData = new double[n];
    double d;
    final Double smallValue = 1e-4;
    int h, j = 0, k = 0, l = 0, m = 0;
    for (int i = 0; i < 100; i++) {
      d = RANDOM.nextDouble();
      h = i + 1;
      dates[i] = h;
      if (d < 0.3) {
        if (d > 0.25) {
          data[i] = smallValue;
          largeZeroRejectedDates[j] = h;
          largeZeroRejectedData[j++] = smallValue;
          smallZeroFilteredDates[k] = h;
          smallZeroFilteredData[k++] = smallValue;
        } else {
          data[i] = 0;
          largeZeroRejectedDates[j] = h;
          largeZeroRejectedData[j++] = 0;
          smallZeroRejectedDates[l] = h;
          smallZeroRejectedData[l++] = 0;
        }
      } else {
        data[i] = d;
        smallZeroFilteredDates[k] = h;
        smallZeroFilteredData[k++] = d;
        largeZeroFilteredDates[m] = h;
        largeZeroFilteredData[m++] = d;
      }
    }
    smallZeroFilteredDates = Arrays.trimToCapacity(smallZeroFilteredDates, k);
    smallZeroFilteredData = Arrays.trimToCapacity(smallZeroFilteredData, k);
    smallZeroRejectedDates = Arrays.trimToCapacity(smallZeroRejectedDates, l);
    smallZeroRejectedData = Arrays.trimToCapacity(smallZeroRejectedData, l);
    largeZeroFilteredDates = Arrays.trimToCapacity(largeZeroFilteredDates, m);
    largeZeroFilteredData = Arrays.trimToCapacity(largeZeroFilteredData, m);
    largeZeroRejectedDates = Arrays.trimToCapacity(largeZeroRejectedDates, j);
    largeZeroRejectedData = Arrays.trimToCapacity(largeZeroRejectedData, j);
    final DoubleTimeSeries<Long> ts = new FastArrayLongDoubleTimeSeries(encoding, dates, data);
    FilteredTimeSeries result = SMALL_ZERO_FILTER.evaluate(ts);
    assertEquals(result, new FilteredTimeSeries(new FastArrayLongDoubleTimeSeries(encoding, smallZeroFilteredDates, smallZeroFilteredData), new FastArrayLongDoubleTimeSeries(
        encoding, smallZeroRejectedDates, smallZeroRejectedData)));
    result = LARGE_ZERO_FILTER.evaluate(ts);
    assertEquals(result, new FilteredTimeSeries(new FastArrayLongDoubleTimeSeries(encoding, largeZeroFilteredDates, largeZeroFilteredData), new FastArrayLongDoubleTimeSeries(
        encoding, largeZeroRejectedDates, largeZeroRejectedData)));
  }
}
