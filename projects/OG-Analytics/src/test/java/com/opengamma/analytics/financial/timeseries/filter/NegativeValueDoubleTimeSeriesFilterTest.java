/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.filter;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NegativeValueDoubleTimeSeriesFilterTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final TimeSeriesFilter FILTER = new NegativeValueDoubleTimeSeriesFilter();
  private static final LocalDateDoubleTimeSeries EMPTY_SERIES = ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTS() {
    FILTER.evaluate((LocalDateDoubleTimeSeries) null);
  }

  @Test
  public void testEmptyTS() {
    final FilteredTimeSeries filtered = FILTER.evaluate(EMPTY_SERIES);
    assertEquals(filtered.getFilteredTS(), EMPTY_SERIES);
    assertEquals(filtered.getRejectedTS(), EMPTY_SERIES);
  }

  @Test
  public void test() {
    final int n = 100;
    final LocalDate[] dates = new LocalDate[n];
    final double[] data = new double[n];
    final LocalDate[] filteredDates = new LocalDate[n];
    final double[] filteredData = new double[n];
    final LocalDate[] rejectedDates = new LocalDate[n];
    final double[] rejectedData = new double[n];
    Double d;
    int j = 0, k = 0;
    for (int i = 0; i < n; i++) {
      d = RANDOM.nextDouble();
      dates[i] = LocalDate.ofEpochDay(i);
      if (d < 0.25) {
        data[i] = -d;
        rejectedDates[k] = LocalDate.ofEpochDay(i);
        rejectedData[k++] = -d;
      } else {
        data[i] = d;
        filteredDates[j] = LocalDate.ofEpochDay(i);
        filteredData[j++] = d;
      }
    }
    final FilteredTimeSeries result = FILTER.evaluate(ImmutableLocalDateDoubleTimeSeries.of(dates, data));
    assertEquals(result, new FilteredTimeSeries(ImmutableLocalDateDoubleTimeSeries.of(Arrays.copyOf(filteredDates, j), Arrays.copyOf(filteredData, j)),
                                                ImmutableLocalDateDoubleTimeSeries.of(Arrays.copyOf(rejectedDates, k), Arrays.copyOf(rejectedData, k))));
  }
}
