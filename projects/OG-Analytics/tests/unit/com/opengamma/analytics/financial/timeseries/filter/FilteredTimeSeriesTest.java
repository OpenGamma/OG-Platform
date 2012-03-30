/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.filter;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import javax.time.calendar.LocalDate;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.timeseries.filter.FilteredTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class FilteredTimeSeriesTest {
  private static final LocalDateDoubleTimeSeries FILTERED = new ArrayLocalDateDoubleTimeSeries(new LocalDate[] {LocalDate.ofEpochDays(1), LocalDate.ofEpochDays(2), 
                                                                                                                LocalDate.ofEpochDays(4), LocalDate.ofEpochDays(5), 
                                                                                                                LocalDate.ofEpochDays(10)}, 
                                                                                               new double[] {1, 2, 3, 4, 5});
  private static final LocalDateDoubleTimeSeries REJECTED = new ArrayLocalDateDoubleTimeSeries(new LocalDate[] {LocalDate.ofEpochDays(3), LocalDate.ofEpochDays(6), 
                                                                                                                   LocalDate.ofEpochDays(7), LocalDate.ofEpochDays(8), 
                                                                                                                   LocalDate.ofEpochDays(9)},
                                                                                                  new double[] {1, 2, 3, 4, 5});

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFilteredTS() {
    new FilteredTimeSeries(null, REJECTED);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
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
