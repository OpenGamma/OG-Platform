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

import com.opengamma.financial.timeseries.returns.ContinuouslyCompoundedTimeSeriesReturnCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.CalculationMode;

/**
 * 
 * @author emcleod
 */
public class ExtremeValueAndReturnDoubleTimeSeriesFiltersTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final double MAX = 10;
  private static final double MIN = -1;
  private static final TimeSeriesReturnCalculator RETURN_CALCULATOR = new ContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  private static final ExtremeValueDoubleTimeSeriesFilter VALUE_FILTER = new ExtremeValueDoubleTimeSeriesFilter(MIN, MAX);
  private static final ExtremeReturnDoubleTimeSeriesFilter RETURN_FILTER = new ExtremeReturnDoubleTimeSeriesFilter(MIN, MAX, RETURN_CALCULATOR);

  @Test(expected = IllegalArgumentException.class)
  public void testNullTS1() {
    VALUE_FILTER.evaluate((DoubleTimeSeries) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullTS2() {
    RETURN_FILTER.evaluate((DoubleTimeSeries) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadRange() {
    new ExtremeValueDoubleTimeSeriesFilter(MAX, MIN);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCalculator() {
    new ExtremeReturnDoubleTimeSeriesFilter(MIN, MAX, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetMax() {
    VALUE_FILTER.setMaximumValue(MIN - 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetMaxEqualsMin() {
    VALUE_FILTER.setMaximumValue(MIN);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetMin() {
    VALUE_FILTER.setMinimumValue(MAX + 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetMinEqualsMax() {
    VALUE_FILTER.setMinimumValue(MAX);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetCalculator() {
    RETURN_FILTER.setReturnCalculator(null);
  }

  @Test
  public void testEmptyTS() {
    final FilteredDoubleTimeSeries filtered = VALUE_FILTER.evaluate(ArrayDoubleTimeSeries.EMPTY_SERIES);
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
      d = RANDOM.nextDouble();
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
    final List<ZonedDateTime> returnFilteredDates = new ArrayList<ZonedDateTime>();
    final List<Double> returnFilteredData = new ArrayList<Double>();
    final List<ZonedDateTime> returnRejectedDates = new ArrayList<ZonedDateTime>();
    final List<Double> returnRejectedData = new ArrayList<Double>();
    final DoubleTimeSeries ts = new ArrayDoubleTimeSeries(dates, data);
    final DoubleTimeSeries returnTS = RETURN_CALCULATOR.evaluate(ts);
    for (int i = 0; i < 99; i++) {
      date = returnTS.getTime(i);
      d = returnTS.getValue(i);
      if (d > MAX || d < MIN) {
        returnRejectedDates.add(date);
        returnRejectedData.add(d);
      } else {
        returnFilteredDates.add(date);
        returnFilteredData.add(d);
      }
    }
    FilteredDoubleTimeSeries result = VALUE_FILTER.evaluate(ts);
    assertEquals(result, new FilteredDoubleTimeSeries(new ArrayDoubleTimeSeries(filteredDates, filteredData), new ArrayDoubleTimeSeries(rejectedDates, rejectedData)));
    result = RETURN_FILTER.evaluate(ts);
    assertEquals(result, new FilteredDoubleTimeSeries(new ArrayDoubleTimeSeries(returnFilteredDates, returnFilteredData), new ArrayDoubleTimeSeries(returnRejectedDates,
        returnRejectedData)));
  }
}
