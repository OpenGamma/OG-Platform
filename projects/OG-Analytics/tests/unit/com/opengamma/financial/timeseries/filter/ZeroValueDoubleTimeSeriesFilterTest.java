/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.filter;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;

import org.testng.annotations.Test;

import cern.colt.Arrays;
import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class ZeroValueDoubleTimeSeriesFilterTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final ZeroValueDoubleTimeSeriesFilter SMALL_ZERO_FILTER = new ZeroValueDoubleTimeSeriesFilter();
  private static final ZeroValueDoubleTimeSeriesFilter LARGE_ZERO_FILTER = new ZeroValueDoubleTimeSeriesFilter(1e-3);
  private static final LocalDateDoubleTimeSeries EMPTY_SERIES = new ArrayLocalDateDoubleTimeSeries();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTS() {
    SMALL_ZERO_FILTER.evaluate((LocalDateDoubleTimeSeries) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeZero() {
    new ZeroValueDoubleTimeSeriesFilter(-1e-12);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSetNegativeZero() {
    SMALL_ZERO_FILTER.setZero(-1e-12);
  }

  @Test
  public void testEmptyTS() {
    final FilteredTimeSeries filtered = SMALL_ZERO_FILTER.evaluate(EMPTY_SERIES);
    assertEquals(filtered.getFilteredTS(), EMPTY_SERIES);
    assertEquals(filtered.getRejectedTS(), EMPTY_SERIES);
  }

  @Test
  public void test() {
    final int n = 100;
    final LocalDate[] dates = new LocalDate[n];
    final double[] data = new double[n];
    LocalDate[] smallZeroFilteredDates = new LocalDate[n];
    double[] smallZeroFilteredData = new double[n];
    LocalDate[] smallZeroRejectedDates = new LocalDate[n];
    double[] smallZeroRejectedData = new double[n];
    LocalDate[] largeZeroFilteredDates = new LocalDate[n];
    double[] largeZeroFilteredData = new double[n];
    LocalDate[] largeZeroRejectedDates = new LocalDate[n];
    double[] largeZeroRejectedData = new double[n];
    double d;
    final Double smallValue = 1e-4;
    int h, j = 0, k = 0, l = 0, m = 0;
    for (int i = 0; i < 100; i++) {
      d = RANDOM.nextDouble();
      h = i + 1;
      dates[i] = LocalDate.ofEpochDays(h);
      if (d < 0.3) {
        if (d > 0.25) {
          data[i] = smallValue;
          largeZeroRejectedDates[j] = LocalDate.ofEpochDays(h);
          largeZeroRejectedData[j++] = smallValue;
          smallZeroFilteredDates[k] = LocalDate.ofEpochDays(h);
          smallZeroFilteredData[k++] = smallValue;
        } else {
          data[i] = 0;
          largeZeroRejectedDates[j] = LocalDate.ofEpochDays(h);
          largeZeroRejectedData[j++] = 0;
          smallZeroRejectedDates[l] = LocalDate.ofEpochDays(h);
          smallZeroRejectedData[l++] = 0;
        }
      } else {
        data[i] = d;
        smallZeroFilteredDates[k] = LocalDate.ofEpochDays(h);
        smallZeroFilteredData[k++] = d;
        largeZeroFilteredDates[m] = LocalDate.ofEpochDays(h);
        largeZeroFilteredData[m++] = d;
      }
    }
    smallZeroFilteredDates = (LocalDate[]) trimToCapacity(smallZeroFilteredDates, k);
    smallZeroFilteredData = Arrays.trimToCapacity(smallZeroFilteredData, k);
    smallZeroRejectedDates = (LocalDate[]) trimToCapacity(smallZeroRejectedDates, l);
    smallZeroRejectedData = Arrays.trimToCapacity(smallZeroRejectedData, l);
    largeZeroFilteredDates = (LocalDate[]) trimToCapacity(largeZeroFilteredDates, m);
    largeZeroFilteredData = Arrays.trimToCapacity(largeZeroFilteredData, m);
    largeZeroRejectedDates = (LocalDate[]) trimToCapacity(largeZeroRejectedDates, j);
    largeZeroRejectedData = Arrays.trimToCapacity(largeZeroRejectedData, j);
    final LocalDateDoubleTimeSeries ts = new ArrayLocalDateDoubleTimeSeries(dates, data);
    FilteredTimeSeries result = SMALL_ZERO_FILTER.evaluate(ts);
    assertEquals(result, new FilteredTimeSeries(new ArrayLocalDateDoubleTimeSeries(smallZeroFilteredDates, smallZeroFilteredData), 
                                                new ArrayLocalDateDoubleTimeSeries(smallZeroRejectedDates, smallZeroRejectedData)));
    result = LARGE_ZERO_FILTER.evaluate(ts);
    assertEquals(result, new FilteredTimeSeries(new ArrayLocalDateDoubleTimeSeries(largeZeroFilteredDates, largeZeroFilteredData), 
                                                new ArrayLocalDateDoubleTimeSeries(largeZeroRejectedDates, largeZeroRejectedData)));
  }
  
  private LocalDate[] trimToCapacity(final LocalDate[] source, final int k) {
    LocalDate[] result = new LocalDate[k];
    System.arraycopy(source, 0, result, 0, k);
    return result;
  }
}
