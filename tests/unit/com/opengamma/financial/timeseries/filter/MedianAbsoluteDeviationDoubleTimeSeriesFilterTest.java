/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.filter;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.time.Instant;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class MedianAbsoluteDeviationDoubleTimeSeriesFilterTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final double LIMIT = 5;
  private static final double DATA1 = 29;
  private static final double DATA2 = 16;
  private static final DoubleTimeSeriesFilter FILTER = new MedianAbsoluteDeviationDoubleTimeSeriesFilter(LIMIT);
  private static final List<ZonedDateTime> DATES = new ArrayList<ZonedDateTime>();
  private static final List<Double> DATA = new ArrayList<Double>();
  private static final DoubleTimeSeries TS;
  private static final double EPS = 1e-15;

  static {
    for (int i = 0; i < 500; i++) {
      DATES.add(ZonedDateTime.fromInstant(Instant.instant(i + 1), TimeZone.UTC));
      DATA.add(RANDOM.nextDouble());
    }
    DATA.set(0, DATA1);
    DATA.set(1, DATA2);
    TS = new ArrayDoubleTimeSeries(DATES, DATA);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNull() {
    FILTER.evaluate((DoubleTimeSeries) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyTS() {
    FILTER.evaluate(ArrayDoubleTimeSeries.EMPTY_SERIES);
  }

  @Test
  public void testMasked() {
    final DoubleTimeSeries subSeries = TS.subSeries(DATES.get(0), DATES.get(10));
    final FilteredDoubleTimeSeries result = FILTER.evaluate(subSeries);
    test(result, 9);
  }

  @Test
  public void test() {
    test(FILTER.evaluate(TS), 498);
  }

  private void test(final FilteredDoubleTimeSeries result, final int size) {
    assertEquals(result.getFilteredTS().size(), size);
    final DoubleTimeSeries rejected = result.getRejectedTS();
    assertEquals(rejected.getTime(0), ZonedDateTime.fromInstant(Instant.instant(1), TimeZone.UTC));
    assertEquals(rejected.getValue(0), DATA1, EPS);
    assertEquals(rejected.getTime(1), ZonedDateTime.fromInstant(Instant.instant(2), TimeZone.UTC));
    assertEquals(rejected.getValue(1), DATA2, EPS);
  }
}
