/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.filter;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.TimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 * 
 */
public class StandardDeviationDoubleTimeSeriesFilterTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final double LIMIT = 5;
  private static final double DATA1 = 34;
  private static final double DATA2 = 12;
  private static final TimeSeriesFilter FILTER = new StandardDeviationDoubleTimeSeriesFilter(LIMIT);
  private static final int N = 500;
  private static final DateTimeNumericEncoding ENCODING = DateTimeNumericEncoding.TIME_EPOCH_SECONDS;
  private static final long[] DATES = new long[N];
  private static final double[] DATA = new double[N];
  private static final DoubleTimeSeries<Long> TS;
  private static final double EPS = 1e-15;

  static {
    for (int i = 0; i < 500; i++) {
      DATES[i] = i;
      DATA[i] = (RANDOM.nextDouble());
    }
    DATA[0] = DATA1;
    DATA[1] = DATA2;
    TS = new FastArrayLongDoubleTimeSeries(ENCODING, DATES, DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    FILTER.evaluate((DoubleTimeSeries<Long>) null);
  }

  @Test
  public void testEmptyTS() {
    final FilteredTimeSeries filtered = FILTER.evaluate(FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
    assertEquals(filtered.getFilteredTS(), FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
    assertEquals(filtered.getRejectedTS(), FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
  }

  @Test
  public void testMasked() {
    final TimeSeries<Long, Double> subSeries = TS.subSeries(DATES[0], DATES[11]);
    final FilteredTimeSeries result = FILTER.evaluate(new FastArrayLongDoubleTimeSeries(ENCODING, subSeries.timesArray(), subSeries.valuesArray()));
    assertEquals(result.getFilteredTS().size(), 11);
  }

  @Test
  public void test() {
    final FilteredTimeSeries result = FILTER.evaluate(TS);
    assertEquals(result.getFilteredTS().size(), 498);
    final DoubleTimeSeries<Long> rejected = result.getRejectedTS().toFastLongDoubleTimeSeries();
    assertEquals(rejected.getTimeAt(0), 0, EPS);
    assertEquals(rejected.getValueAt(0), DATA1, EPS);
    assertEquals(rejected.getTimeAt(1), 1, EPS);
    assertEquals(rejected.getValueAt(1), DATA2, EPS);
  }
}
