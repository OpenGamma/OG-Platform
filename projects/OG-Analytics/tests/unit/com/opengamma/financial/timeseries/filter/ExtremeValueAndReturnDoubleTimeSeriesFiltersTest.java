/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.filter;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.LocalDate;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.financial.timeseries.returns.ContinuouslyCompoundedTimeSeriesReturnCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.util.CalculationMode;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ListLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class ExtremeValueAndReturnDoubleTimeSeriesFiltersTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final double MAX = 10;
  private static final double MIN = -1;
  private static final TimeSeriesReturnCalculator RETURN_CALCULATOR = new ContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  private static final ExtremeValueDoubleTimeSeriesFilter VALUE_FILTER = new ExtremeValueDoubleTimeSeriesFilter(MIN, MAX);
  private static final ExtremeReturnDoubleTimeSeriesFilter RETURN_FILTER = new ExtremeReturnDoubleTimeSeriesFilter(MIN, MAX, RETURN_CALCULATOR);
  private static final LocalDateDoubleTimeSeries EMPTY_SERIES = new ArrayLocalDateDoubleTimeSeries();
  
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
    final List<LocalDate> dates = new ArrayList<LocalDate>();
    final List<Double> data = new ArrayList<Double>();
    final List<LocalDate> filteredDates = new ArrayList<LocalDate>();
    final List<Double> filteredData = new ArrayList<Double>();
    final List<LocalDate> rejectedDates = new ArrayList<LocalDate>();
    final List<Double> rejectedData = new ArrayList<Double>();
    Double d;
    Double value;
    for (int i = 0; i < 100; i++) {
      d = RANDOM.nextDouble();
      dates.add(LocalDate.ofEpochDays(i));
      if (d < 0.25) {
        value = d < 0.1 ? MIN - d : MAX + d;
        data.add(value);
        rejectedDates.add(LocalDate.ofEpochDays(i));
        rejectedData.add(value);
      } else {
        data.add(d);
        filteredDates.add(LocalDate.ofEpochDays(i));
        filteredData.add(d);
      }
    }
    final List<LocalDate> returnFilteredDates = new ArrayList<LocalDate>();
    final List<Double> returnFilteredData = new ArrayList<Double>();
    final List<LocalDate> returnRejectedDates = new ArrayList<LocalDate>();
    final List<Double> returnRejectedData = new ArrayList<Double>();
    final LocalDateDoubleTimeSeries ts = new ListLocalDateDoubleTimeSeries(dates, data);
    final LocalDateDoubleTimeSeries returnTS = RETURN_CALCULATOR.evaluate(ts);
    LocalDate date;
    for (int i = 0; i < 99; i++) {
      date = returnTS.getTimeAt(i);
      d = returnTS.getValueAt(i);
      if (d > MAX || d < MIN) {
        returnRejectedDates.add(date);
        returnRejectedData.add(d);
      } else {
        returnFilteredDates.add(date);
        returnFilteredData.add(d);
      }
    }
    FilteredTimeSeries result = VALUE_FILTER.evaluate(ts);
    assertEquals(result, new FilteredTimeSeries(new ListLocalDateDoubleTimeSeries(filteredDates, filteredData), new ListLocalDateDoubleTimeSeries(rejectedDates,
        rejectedData)));
    result = RETURN_FILTER.evaluate(ts);
    assertEquals(result, new FilteredTimeSeries(new ListLocalDateDoubleTimeSeries(returnFilteredDates, returnFilteredData), new ListLocalDateDoubleTimeSeries(
        returnRejectedDates, returnRejectedData)));
  }
}
