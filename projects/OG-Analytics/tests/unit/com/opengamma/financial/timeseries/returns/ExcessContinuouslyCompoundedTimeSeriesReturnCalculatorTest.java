/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.returns;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;

import org.testng.annotations.Test;

import com.opengamma.util.timeseries.TimeSeriesException;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class ExcessContinuouslyCompoundedTimeSeriesReturnCalculatorTest {
  private static final TimeSeriesReturnCalculator CALCULATOR = TimeSeriesReturnCalculatorFactory.getReturnCalculator(TimeSeriesReturnCalculatorFactory.EXCESS_CONTINUOUS_STRICT);
  private static final LocalDateDoubleTimeSeries TS1 = new ArrayLocalDateDoubleTimeSeries(new LocalDate[] {LocalDate.ofEpochDays(1), LocalDate.ofEpochDays(2), 
                                                                                                           LocalDate.ofEpochDays(3), LocalDate.ofEpochDays(4), 
                                                                                                           LocalDate.ofEpochDays(5)}, 
                                                                                          new double[] {1, 2, 3, 4, 5});
  private static final LocalDateDoubleTimeSeries TS2 = new ArrayLocalDateDoubleTimeSeries(new LocalDate[] {LocalDate.ofEpochDays(1), LocalDate.ofEpochDays(2), 
                                                                                                           LocalDate.ofEpochDays(3), LocalDate.ofEpochDays(4), 
                                                                                                           LocalDate.ofEpochDays(5)}, 
                                                                                          new double[] {1, 1, 1, 1, 1});
  private static final LocalDateDoubleTimeSeries EMPTY_SERIES = new ArrayLocalDateDoubleTimeSeries();
  private static final TimeSeriesReturnCalculator RETURNS = TimeSeriesReturnCalculatorFactory.getReturnCalculator(TimeSeriesReturnCalculatorFactory.CONTINUOUS_STRICT);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArray() {
    CALCULATOR.evaluate((LocalDateDoubleTimeSeries[]) null);
  }

  @Test(expectedExceptions = TimeSeriesException.class)
  public void testSmallArray() {
    CALCULATOR.evaluate(new LocalDateDoubleTimeSeries[] {TS1, TS2});
  }

  @Test(expectedExceptions = TimeSeriesException.class)
  public void testDifferentLengths() {
    CALCULATOR.evaluate(new LocalDateDoubleTimeSeries[] {TS1, null, new ArrayLocalDateDoubleTimeSeries(new LocalDate[] { LocalDate.ofEpochDays(1) }, new double[] {1}), null});
  }

  @Test
  public void test() {
    assertEquals(CALCULATOR.evaluate(new LocalDateDoubleTimeSeries[] {TS1, EMPTY_SERIES, TS2, EMPTY_SERIES}), RETURNS
        .evaluate(TS1));
    assertEquals(CALCULATOR.evaluate(new LocalDateDoubleTimeSeries[] {TS1, null, TS2, null}), RETURNS.evaluate(TS1));
  }
}
