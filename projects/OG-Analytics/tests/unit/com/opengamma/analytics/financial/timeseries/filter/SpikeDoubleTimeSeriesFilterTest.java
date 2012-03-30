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
import com.opengamma.analytics.financial.timeseries.filter.SpikeDoubleTimeSeriesFilter;
import com.opengamma.analytics.financial.timeseries.filter.TimeSeriesFilter;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class SpikeDoubleTimeSeriesFilterTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final TimeSeriesFilter FILTER = new SpikeDoubleTimeSeriesFilter(100);
  private static final int N = 100;
  private static final LocalDate[] DATES = new LocalDate[N];
  private static final double[] DATA = new double[N];
  
  private static final LocalDateDoubleTimeSeries EMPTY_SERIES = new ArrayLocalDateDoubleTimeSeries();

  static {
    final double value = 0.5;
    double random;
    for (int i = 0; i < N; i++) {
      random = RANDOM.nextDouble();
      DATES[i] = LocalDate.ofEpochDays(i);
      DATA[i] = value * (random < 0.5 ? 1 - random : 1 + random);
    }
  }

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
  public void testInitialSpike() {
    final double[] data = Arrays.copyOf(DATA, N);
    data[0] = 100.;
    final LocalDateDoubleTimeSeries ts = new ArrayLocalDateDoubleTimeSeries(DATES, data);
    final LocalDateDoubleTimeSeries rejected = FILTER.evaluate(ts).getRejectedTS();
    assertEquals(rejected.size(), 1);
    assertEquals(rejected.getTimeAt(0), ts.getTimeAt(0));
    assertEquals(rejected.getValueAt(0), ts.getValueAt(0));
  }

  @Test
  public void testSpike() {
    final double[] data = Arrays.copyOf(DATA, N);
    data[10] = 100.;
    LocalDateDoubleTimeSeries ts = new ArrayLocalDateDoubleTimeSeries(DATES, data);
    FilteredTimeSeries filtered = FILTER.evaluate(ts);
    assertSeries(ts, filtered, 10);
    data[10] = -100.;
    ts = new ArrayLocalDateDoubleTimeSeries(DATES, data);
    filtered = FILTER.evaluate(ts);
    assertSeries(ts, filtered, 10);
  }

  private void assertSeries(final LocalDateDoubleTimeSeries ts, final FilteredTimeSeries filtered, final int index) {
    final LocalDateDoubleTimeSeries rejected = filtered.getRejectedTS();
    assertEquals(rejected.size(), 1);
    assertEquals(rejected.getTimeAt(0), ts.getTimeAt(index));
    assertEquals(rejected.getValueAt(0), ts.getValueAt(index));
    assertEquals(filtered.getFilteredTS().size(), 99);
  }
}
