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

import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class ZeroValueDoubleTimeSeriesFilterTest {
  private static final ZeroValueDoubleTimeSeriesFilter SMALL_ZERO_FILTER = new ZeroValueDoubleTimeSeriesFilter();
  private static final ZeroValueDoubleTimeSeriesFilter LARGE_ZERO_FILTER = new ZeroValueDoubleTimeSeriesFilter(1e-3);

  @Test(expected = IllegalArgumentException.class)
  public void testNullTS() {
    SMALL_ZERO_FILTER.evaluate((DoubleTimeSeries) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeZero() {
    new ZeroValueDoubleTimeSeriesFilter(-1e-12);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetNegativeZero() {
    SMALL_ZERO_FILTER.setZero(-1e-12);
  }

  @Test
  public void testEmptyTS() {
    final FilteredDoubleTimeSeries filtered = SMALL_ZERO_FILTER.evaluate(ArrayDoubleTimeSeries.EMPTY_SERIES);
    assertEquals(filtered.getFilteredTS(), ArrayDoubleTimeSeries.EMPTY_SERIES);
    assertNull(filtered.getRejectedTS());
  }

  @Test
  public void test() {
    final List<ZonedDateTime> dates = new ArrayList<ZonedDateTime>();
    final List<Double> data = new ArrayList<Double>();
    final List<ZonedDateTime> smallZeroFilteredDates = new ArrayList<ZonedDateTime>();
    final List<Double> smallZeroFilteredData = new ArrayList<Double>();
    final List<ZonedDateTime> smallZeroRejectedDates = new ArrayList<ZonedDateTime>();
    final List<Double> smallZeroRejectedData = new ArrayList<Double>();
    final List<ZonedDateTime> largeZeroFilteredDates = new ArrayList<ZonedDateTime>();
    final List<Double> largeZeroFilteredData = new ArrayList<Double>();
    final List<ZonedDateTime> largeZeroRejectedDates = new ArrayList<ZonedDateTime>();
    final List<Double> largeZeroRejectedData = new ArrayList<Double>();
    Double d;
    ZonedDateTime date;
    final Double smallValue = 1e-4;
    for (int i = 0; i < 100; i++) {
      d = Math.random();
      date = ZonedDateTime.fromInstant(Instant.millisInstant(i + 1), TimeZone.UTC);
      dates.add(date);
      if (d < 0.3) {
        if (d > 0.25) {
          data.add(smallValue);
          largeZeroRejectedDates.add(date);
          largeZeroRejectedData.add(smallValue);
          smallZeroFilteredDates.add(date);
          smallZeroFilteredData.add(smallValue);
        } else {
          data.add(0.);
          largeZeroRejectedDates.add(date);
          largeZeroRejectedData.add(0.);
          smallZeroRejectedDates.add(date);
          smallZeroRejectedData.add(0.);
        }
      } else {
        data.add(d);
        smallZeroFilteredDates.add(date);
        smallZeroFilteredData.add(d);
        largeZeroFilteredDates.add(date);
        largeZeroFilteredData.add(d);
      }
    }
    final DoubleTimeSeries ts = new ArrayDoubleTimeSeries(dates, data);
    FilteredDoubleTimeSeries result = SMALL_ZERO_FILTER.evaluate(ts);
    assertEquals(result, new FilteredDoubleTimeSeries(new ArrayDoubleTimeSeries(smallZeroFilteredDates, smallZeroFilteredData), new ArrayDoubleTimeSeries(smallZeroRejectedDates,
        smallZeroRejectedData)));
    result = LARGE_ZERO_FILTER.evaluate(ts);
    assertEquals(result, new FilteredDoubleTimeSeries(new ArrayDoubleTimeSeries(largeZeroFilteredDates, largeZeroFilteredData), new ArrayDoubleTimeSeries(largeZeroRejectedDates,
        largeZeroRejectedData)));
  }
}