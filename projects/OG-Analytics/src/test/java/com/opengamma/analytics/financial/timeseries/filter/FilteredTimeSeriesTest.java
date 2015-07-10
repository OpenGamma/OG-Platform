/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.filter;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class FilteredTimeSeriesTest {
  private static final LocalDateDoubleTimeSeries FILTERED = ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.ofEpochDay(1), LocalDate.ofEpochDay(2), 
                                                                                                                LocalDate.ofEpochDay(4), LocalDate.ofEpochDay(5), 
                                                                                                                LocalDate.ofEpochDay(10)}, 
                                                                                               new double[] {1, 2, 3, 4, 5});
  private static final LocalDateDoubleTimeSeries REJECTED = ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.ofEpochDay(3), LocalDate.ofEpochDay(6), 
                                                                                                                   LocalDate.ofEpochDay(7), LocalDate.ofEpochDay(8), 
                                                                                                                   LocalDate.ofEpochDay(9)},
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
