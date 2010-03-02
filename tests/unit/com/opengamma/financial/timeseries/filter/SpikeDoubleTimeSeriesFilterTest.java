/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
public class SpikeDoubleTimeSeriesFilterTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final DoubleTimeSeriesFilter FILTER = new SpikeDoubleTimeSeriesFilter(100);
  private static final List<ZonedDateTime> DATES = new ArrayList<ZonedDateTime>();
  private static final List<Double> DATA = new ArrayList<Double>();

  static {
    final double value = 0.5;
    double random;
    for (int i = 0; i < 100; i++) {
      random = RANDOM.nextDouble();
      DATES.add(ZonedDateTime.fromInstant(Instant.instant(i + 1), TimeZone.UTC));
      DATA.add(value * (random < 0.5 ? 1 - random : 1 + random));
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullTS() {
    FILTER.evaluate((DoubleTimeSeries) null);
  }

  @Test
  public void testEmptyTS() {
    final FilteredDoubleTimeSeries filtered = FILTER.evaluate(ArrayDoubleTimeSeries.EMPTY_SERIES);
    assertEquals(filtered.getFilteredTS(), ArrayDoubleTimeSeries.EMPTY_SERIES);
    assertNull(filtered.getRejectedTS());
  }

  @Test
  public void testInitialSpike() {
    final List<Double> data = new ArrayList<Double>(DATA);
    data.set(0, 100.);
    final DoubleTimeSeries ts = new ArrayDoubleTimeSeries(DATES, data);
    final DoubleTimeSeries rejected = FILTER.evaluate(ts).getRejectedTS();
    assertEquals(rejected.size(), 1);
    assertEquals(rejected.getTime(0), ts.getTime(0));
    assertEquals(rejected.getValue(0), ts.getValue(0));
  }

  @Test
  public void testSpike() {
    final List<Double> data = new ArrayList<Double>(DATA);
    data.set(10, 100.);
    DoubleTimeSeries ts = new ArrayDoubleTimeSeries(DATES, data);
    FilteredDoubleTimeSeries filtered = FILTER.evaluate(ts);
    testSeries(ts, filtered, 10);
    data.set(10, -100.);
    ts = new ArrayDoubleTimeSeries(DATES, data);
    filtered = FILTER.evaluate(ts);
    testSeries(ts, filtered, 10);
  }

  private void testSeries(final DoubleTimeSeries ts, final FilteredDoubleTimeSeries filtered, final int index) {
    final DoubleTimeSeries rejected = filtered.getRejectedTS();
    assertEquals(rejected.size(), 1);
    assertEquals(rejected.getTime(0), ts.getTime(index));
    assertEquals(rejected.getValue(0), ts.getValue(index));
    assertEquals(filtered.getFilteredTS().size(), 99);
  }
}
