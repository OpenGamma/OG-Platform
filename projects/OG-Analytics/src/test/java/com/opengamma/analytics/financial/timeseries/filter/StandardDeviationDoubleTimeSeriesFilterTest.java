/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.filter;

import static org.testng.AssertJUnit.assertEquals;

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
public class StandardDeviationDoubleTimeSeriesFilterTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final double LIMIT = 5;
  private static final double DATA1 = 34;
  private static final double DATA2 = 12;
  private static final TimeSeriesFilter FILTER = new StandardDeviationDoubleTimeSeriesFilter(LIMIT);
  private static final int N = 500;
  private static final LocalDate[] DATES = new LocalDate[N];
  private static final double[] DATA = new double[N];
  private static final LocalDateDoubleTimeSeries TS;
  private static final LocalDateDoubleTimeSeries EMPTY_SERIES = ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES;
  private static final double EPS = 1e-15;

  static {
    for (int i = 0; i < 500; i++) {
      DATES[i] = LocalDate.ofEpochDay(i);
      DATA[i] = (RANDOM.nextDouble());
    }
    DATA[0] = DATA1;
    DATA[1] = DATA2;
    TS = ImmutableLocalDateDoubleTimeSeries.of(DATES, DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    FILTER.evaluate((LocalDateDoubleTimeSeries) null);
  }

  @Test
  public void testEmptyTS() {
    final FilteredTimeSeries filtered = FILTER.evaluate(EMPTY_SERIES);
    assertEquals(filtered.getFilteredTS(), EMPTY_SERIES);
    assertEquals(filtered.getRejectedTS(), EMPTY_SERIES);
  }

  @Test
  public void testMasked() {
    final LocalDateDoubleTimeSeries subSeries = TS.subSeries(DATES[0], DATES[11]);
    final FilteredTimeSeries result = FILTER.evaluate(ImmutableLocalDateDoubleTimeSeries.of(subSeries.timesArray(), subSeries.valuesArrayFast()));
    assertEquals(result.getFilteredTS().size(), 11);
  }

  @Test
  public void test() {
    final FilteredTimeSeries result = FILTER.evaluate(TS);
    assertEquals(result.getFilteredTS().size(), 498);
    final LocalDateDoubleTimeSeries rejected = result.getRejectedTS();
    assertEquals(rejected.getTimeAtIndex(0), LocalDate.ofEpochDay(0));
    assertEquals(rejected.getValueAtIndex(0), DATA1, EPS);
    assertEquals(rejected.getTimeAtIndex(1), LocalDate.ofEpochDay(1));
    assertEquals(rejected.getValueAtIndex(1), DATA2, EPS);
  }
}
