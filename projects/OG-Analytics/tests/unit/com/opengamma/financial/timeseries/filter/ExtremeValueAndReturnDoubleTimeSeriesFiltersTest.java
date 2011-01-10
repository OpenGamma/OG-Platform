/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.filter;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.financial.timeseries.returns.ContinuouslyCompoundedTimeSeriesReturnCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.util.CalculationMode;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastListLongDoubleTimeSeries;

/**
 * 
 */
public class ExtremeValueAndReturnDoubleTimeSeriesFiltersTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final double MAX = 10;
  private static final double MIN = -1;
  private static final TimeSeriesReturnCalculator RETURN_CALCULATOR = new ContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  private static final ExtremeValueDoubleTimeSeriesFilter VALUE_FILTER = new ExtremeValueDoubleTimeSeriesFilter(MIN, MAX);
  private static final ExtremeReturnDoubleTimeSeriesFilter RETURN_FILTER = new ExtremeReturnDoubleTimeSeriesFilter(MIN, MAX, RETURN_CALCULATOR);
  private static final DateTimeNumericEncoding ENCODING = DateTimeNumericEncoding.DATE_EPOCH_DAYS;

  @Test(expected = IllegalArgumentException.class)
  public void testNullTS1() {
    VALUE_FILTER.evaluate((DoubleTimeSeries<Long>) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullTS2() {
    RETURN_FILTER.evaluate((DoubleTimeSeries<Long>) null);
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
  public void testSetMaxValue() {
    VALUE_FILTER.setMaximumValue(MIN - 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetMaxReturn() {
    RETURN_FILTER.setMaximumValue(MIN - 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetMaxEqualsMinValue() {
    VALUE_FILTER.setMaximumValue(MIN);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetMaxEqualsMinReturn() {
    RETURN_FILTER.setMaximumValue(MIN);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetMinValue() {
    VALUE_FILTER.setMinimumValue(MAX + 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetMinReturn() {
    RETURN_FILTER.setMinimumValue(MAX + 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetMinEqualsMaxValue() {
    VALUE_FILTER.setMinimumValue(MAX);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetMinEqualsMaxReturn() {
    RETURN_FILTER.setMinimumValue(MAX);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetCalculator() {
    RETURN_FILTER.setReturnCalculator(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetRangeValue() {
    VALUE_FILTER.setRange(MAX, MIN);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetRangeReturn() {
    RETURN_FILTER.setRange(MAX, MIN);
  }

  @Test
  public void testEmptyTS() {
    final FilteredTimeSeries filtered = VALUE_FILTER.evaluate(FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
    assertEquals(filtered.getFilteredTS(), FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
    assertEquals(filtered.getRejectedTS(), FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
  }

  @Test
  public void test() {
    final List<Long> dates = new ArrayList<Long>();
    final List<Double> data = new ArrayList<Double>();
    final List<Long> filteredDates = new ArrayList<Long>();
    final List<Double> filteredData = new ArrayList<Double>();
    final List<Long> rejectedDates = new ArrayList<Long>();
    final List<Double> rejectedData = new ArrayList<Double>();
    Double d;
    Double value;
    for (int i = 0; i < 100; i++) {
      d = RANDOM.nextDouble();
      dates.add(Long.valueOf(i));
      if (d < 0.25) {
        value = d < 0.1 ? MIN - d : MAX + d;
        data.add(value);
        rejectedDates.add(Long.valueOf(i));
        rejectedData.add(value);
      } else {
        data.add(d);
        filteredDates.add(Long.valueOf(i));
        filteredData.add(d);
      }
    }
    final List<Long> returnFilteredDates = new ArrayList<Long>();
    final List<Double> returnFilteredData = new ArrayList<Double>();
    final List<Long> returnRejectedDates = new ArrayList<Long>();
    final List<Double> returnRejectedData = new ArrayList<Double>();
    final DoubleTimeSeries<Long> ts = new FastListLongDoubleTimeSeries(ENCODING, dates, data);
    final DoubleTimeSeries<Long> returnTS = RETURN_CALCULATOR.evaluate(ts).toFastLongDoubleTimeSeries();
    long date;
    for (int i = 0; i < 99; i++) {
      date = returnTS.getTime(i);
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
    assertEquals(result, new FilteredTimeSeries(new FastListLongDoubleTimeSeries(ENCODING, filteredDates, filteredData), new FastListLongDoubleTimeSeries(ENCODING, rejectedDates,
        rejectedData)));
    result = RETURN_FILTER.evaluate(ts);
    assertEquals(result, new FilteredTimeSeries(new FastListLongDoubleTimeSeries(ENCODING, returnFilteredDates, returnFilteredData), new FastListLongDoubleTimeSeries(ENCODING,
        returnRejectedDates, returnRejectedData)));
  }
}
