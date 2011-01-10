/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastArrayIntDoubleTimeSeries;

/**
 * 
 */
public class FilteredTimeSeriesTest {
  private static final DoubleTimeSeries<?> FILTERED = new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new int[] {1, 2, 4, 5, 10}, new double[] {1, 2, 3,
      4, 5});
  private static final DoubleTimeSeries<?> REJECTED = new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new int[] {3, 6, 7, 8, 9}, new double[] {1, 2, 3,
      4, 5});

  @Test(expected = IllegalArgumentException.class)
  public void testNullFilteredTS() {
    new FilteredTimeSeries(null, REJECTED);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullRejectedTS() {
    new FilteredTimeSeries(FILTERED, null);
  }

  @Test
  public void testEqualsAndHashCode() {
    final FilteredTimeSeries f1 = new FilteredTimeSeries(FILTERED, REJECTED);
    final FilteredTimeSeries f2 = new FilteredTimeSeries(FILTERED, REJECTED);
    final FilteredTimeSeries f3 = new FilteredTimeSeries(REJECTED, FILTERED);
    assertEquals(f1.getFilteredTS(), FILTERED);
    assertEquals(f2.getRejectedTS(), REJECTED);
    assertEquals(f1, f2);
    assertEquals(f1.hashCode(), f2.hashCode());
    assertFalse(f1.equals(f3));
  }
}
