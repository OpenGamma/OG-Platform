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
public class NegativeValueDoubleTimeSeriesFilterTest {
  private static final DoubleTimeSeriesFilter FILTER = new NegativeValueDoubleTimeSeriesFilter();

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
  public void test() {
    final List<ZonedDateTime> dates = new ArrayList<ZonedDateTime>();
    final List<Double> data = new ArrayList<Double>();
    final List<ZonedDateTime> filteredDates = new ArrayList<ZonedDateTime>();
    final List<Double> filteredData = new ArrayList<Double>();
    final List<ZonedDateTime> rejectedDates = new ArrayList<ZonedDateTime>();
    final List<Double> rejectedData = new ArrayList<Double>();
    Double d;
    ZonedDateTime date;
    for (int i = 0; i < 100; i++) {
      d = Math.random();
      date = ZonedDateTime.fromInstant(Instant.millisInstant(i + 1), TimeZone.UTC);
      dates.add(date);
      if (d < 0.25) {
        data.add(-d);
        rejectedDates.add(date);
        rejectedData.add(-d);
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
