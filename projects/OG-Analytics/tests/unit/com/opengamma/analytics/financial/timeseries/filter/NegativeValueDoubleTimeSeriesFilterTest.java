/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.filter;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import javax.time.calendar.LocalDate;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.timeseries.filter.FilteredTimeSeries;
import com.opengamma.analytics.financial.timeseries.filter.NegativeValueDoubleTimeSeriesFilter;
import com.opengamma.analytics.financial.timeseries.filter.TimeSeriesFilter;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class NegativeValueDoubleTimeSeriesFilterTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final TimeSeriesFilter FILTER = new NegativeValueDoubleTimeSeriesFilter();
  private static final LocalDateDoubleTimeSeries EMPTY_SERIES = new ArrayLocalDateDoubleTimeSeries();

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
      dates[i] = LocalDate.ofEpochDays(i);
      if (d < 0.25) {
        data[i] = -d;
        rejectedDates[k] = LocalDate.ofEpochDays(i);
        rejectedData[k++] = -d;
      } else {
        data[i] = d;
        filteredDates[j] = LocalDate.ofEpochDays(i);
        filteredData[j++] = d;
      }
    }
    final FilteredTimeSeries result = FILTER.evaluate(new ArrayLocalDateDoubleTimeSeries(dates, data));
    assertEquals(result, new FilteredTimeSeries(new ArrayLocalDateDoubleTimeSeries(Arrays.copyOf(filteredDates, j), Arrays.copyOf(filteredData, j)),
                                                new ArrayLocalDateDoubleTimeSeries(Arrays.copyOf(rejectedDates, k), Arrays.copyOf(rejectedData, k))));
  }
}
