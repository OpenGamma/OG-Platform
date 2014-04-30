/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.filter;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.timeseries.returns.ContinuouslyCompoundedTimeSeriesReturnCalculator;
import com.opengamma.analytics.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.CalculationMode;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ExtremeValueAndReturnDoubleTimeSeriesFiltersTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final double MAX = 10;
  private static final double MIN = -1;
  private static final TimeSeriesReturnCalculator RETURN_CALCULATOR = new ContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  private static final ExtremeValueDoubleTimeSeriesFilter VALUE_FILTER = new ExtremeValueDoubleTimeSeriesFilter(MIN, MAX);
  private static final ExtremeReturnDoubleTimeSeriesFilter RETURN_FILTER = new ExtremeReturnDoubleTimeSeriesFilter(MIN, MAX, RETURN_CALCULATOR);
  private static final LocalDateDoubleTimeSeries EMPTY_SERIES = ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTS1() {
    VALUE_FILTER.evaluate((LocalDateDoubleTimeSeries) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTS2() {
    RETURN_FILTER.evaluate((LocalDateDoubleTimeSeries) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadRange() {
    new ExtremeValueDoubleTimeSeriesFilter(MAX, MIN);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCalculator() {
    new ExtremeReturnDoubleTimeSeriesFilter(MIN, MAX, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSetMaxValue() {
    VALUE_FILTER.setMaximumValue(MIN - 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSetMaxReturn() {
    RETURN_FILTER.setMaximumValue(MIN - 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSetMaxEqualsMinValue() {
    VALUE_FILTER.setMaximumValue(MIN);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSetMaxEqualsMinReturn() {
    RETURN_FILTER.setMaximumValue(MIN);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSetMinValue() {
    VALUE_FILTER.setMinimumValue(MAX + 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSetMinReturn() {
    RETURN_FILTER.setMinimumValue(MAX + 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSetMinEqualsMaxValue() {
    VALUE_FILTER.setMinimumValue(MAX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSetMinEqualsMaxReturn() {
    RETURN_FILTER.setMinimumValue(MAX);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSetCalculator() {
    RETURN_FILTER.setReturnCalculator(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSetRangeValue() {
    VALUE_FILTER.setRange(MAX, MIN);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSetRangeReturn() {
    RETURN_FILTER.setRange(MAX, MIN);
  }

  @Test
  public void testEmptyTS() {
    final FilteredTimeSeries filtered = VALUE_FILTER.evaluate(EMPTY_SERIES);
    assertEquals(filtered.getFilteredTS(), EMPTY_SERIES);
    assertEquals(filtered.getRejectedTS(), EMPTY_SERIES);
  }

  @Test
  public void test() {
    final List<LocalDate> dates = new ArrayList<>();
    final List<Double> data = new ArrayList<>();
    final List<LocalDate> filteredDates = new ArrayList<>();
    final List<Double> filteredData = new ArrayList<>();
    final List<LocalDate> rejectedDates = new ArrayList<>();
    final List<Double> rejectedData = new ArrayList<>();
    Double d;
    Double value;
    for (int i = 0; i < 100; i++) {
      d = RANDOM.nextDouble();
      dates.add(LocalDate.ofEpochDay(i));
      if (d < 0.25) {
        value = d < 0.1 ? MIN - d : MAX + d;
        data.add(value);
        rejectedDates.add(LocalDate.ofEpochDay(i));
        rejectedData.add(value);
      } else {
        data.add(d);
        filteredDates.add(LocalDate.ofEpochDay(i));
        filteredData.add(d);
      }
    }
    final List<LocalDate> returnFilteredDates = new ArrayList<>();
    final List<Double> returnFilteredData = new ArrayList<>();
    final List<LocalDate> returnRejectedDates = new ArrayList<>();
    final List<Double> returnRejectedData = new ArrayList<>();
    final LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.of(dates, data);
    final LocalDateDoubleTimeSeries returnTS = RETURN_CALCULATOR.evaluate(ts);
    LocalDate date;
    for (int i = 0; i < 99; i++) {
      date = returnTS.getTimeAtIndex(i);
      d = returnTS.getValueAtIndex(i);
      if (d > MAX || d < MIN) {
        returnRejectedDates.add(date);
        returnRejectedData.add(d);
      } else {
        returnFilteredDates.add(date);
        returnFilteredData.add(d);
      }
    }
    FilteredTimeSeries result = VALUE_FILTER.evaluate(ts);
    assertEquals(result, new FilteredTimeSeries(ImmutableLocalDateDoubleTimeSeries.of(filteredDates, filteredData), ImmutableLocalDateDoubleTimeSeries.of(rejectedDates,
        rejectedData)));
    result = RETURN_FILTER.evaluate(ts);
    assertEquals(result, new FilteredTimeSeries(ImmutableLocalDateDoubleTimeSeries.of(returnFilteredDates, returnFilteredData), ImmutableLocalDateDoubleTimeSeries.of(
        returnRejectedDates, returnRejectedData)));
  }
}
