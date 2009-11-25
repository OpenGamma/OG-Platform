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
public class ExtremeValueDoubleTimeSeriesFilterTest {
  private static final double MAX = 10;
  private static final double MIN = -1;
  private static final ExtremeValueDoubleTimeSeriesFilter FILTER = new ExtremeValueDoubleTimeSeriesFilter(MIN, MAX);

  @Test(expected = IllegalArgumentException.class)
  public void testNullTS() {
    FILTER.evaluate((DoubleTimeSeries) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadRange() {
    new ExtremeValueDoubleTimeSeriesFilter(MAX, MIN);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetMax() {
    FILTER.setMaximumValue(MIN - 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetMaxEqualsMin() {
    FILTER.setMaximumValue(MIN);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetMin() {
    FILTER.setMinimumValue(MAX + 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetMinEqualsMax() {
    FILTER.setMinimumValue(MAX);
  }

  @Test
  public void testEmptyTS() {
    final FilteredDoubleTimeSeries filtered = FILTER.evaluate(ArrayDoubleTimeSeries.EMPTY_SERIES);
    assertEquals(filtered.getFilteredTS(), ArrayDoubleTimeSeries.EMPTY_SERIES);
    assertNull(filtered.getRejectedTS());
  }

  @Test
  public void test() {
    final List<ZonedDateTime> dates = new ArrayList<ZonedDateTime>();
    final List<Double> data = new ArrayList<Double>();
    final List<ZonedDateTime> filteredDates = new ArrayList<ZonedDateTime>();
    final List<Double> filteredData = new ArrayList<Double>();
    final List<ZonedDateTime> rejectedDates = new ArrayList<ZonedDateTime>();
    final List<Double> rejectedData = new ArrayList<Double>();
    Double d;
    ZonedDateTime date;
    Double value;
    for (int i = 0; i < 100; i++) {
      d = Math.random();
      date = ZonedDateTime.fromInstant(Instant.millisInstant(i + 1), TimeZone.UTC);
      dates.add(date);
      if (d < 0.25) {
        value = d < 0.1 ? MIN - d : MAX + d;
        data.add(value);
        rejectedDates.add(date);
        rejectedData.add(value);
      } else {
        data.add(d);
        filteredDates.add(date);
        filteredData.add(d);
      }
    }
    final FilteredDoubleTimeSeries result = FILTER.evaluate(new ArrayDoubleTimeSeries(dates, data));
    assertEquals(result, new FilteredDoubleTimeSeries(new ArrayDoubleTimeSeries(filteredDates, filteredData), new ArrayDoubleTimeSeries(rejectedDates, rejectedData)));
  }
}
