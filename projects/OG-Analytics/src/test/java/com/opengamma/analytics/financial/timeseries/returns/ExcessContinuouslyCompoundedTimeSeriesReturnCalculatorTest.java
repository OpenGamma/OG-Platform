/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.returns;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.TimeSeriesException;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ExcessContinuouslyCompoundedTimeSeriesReturnCalculatorTest {
  private static final TimeSeriesReturnCalculator CALCULATOR = TimeSeriesReturnCalculatorFactory.getReturnCalculator(TimeSeriesReturnCalculatorFactory.EXCESS_CONTINUOUS_STRICT);
  private static final LocalDateDoubleTimeSeries TS1 = ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.ofEpochDay(1), LocalDate.ofEpochDay(2), 
                                                                                                           LocalDate.ofEpochDay(3), LocalDate.ofEpochDay(4), 
                                                                                                           LocalDate.ofEpochDay(5)}, 
                                                                                          new double[] {1, 2, 3, 4, 5});
  private static final LocalDateDoubleTimeSeries TS2 = ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.ofEpochDay(1), LocalDate.ofEpochDay(2), 
                                                                                                           LocalDate.ofEpochDay(3), LocalDate.ofEpochDay(4), 
                                                                                                           LocalDate.ofEpochDay(5)}, 
                                                                                          new double[] {1, 1, 1, 1, 1});
  private static final LocalDateDoubleTimeSeries EMPTY_SERIES = ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES;
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
    CALCULATOR.evaluate(new LocalDateDoubleTimeSeries[] {TS1, null, ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] { LocalDate.ofEpochDay(1) }, new double[] {1}), null});
  }

  @Test
  public void test() {
    assertEquals(CALCULATOR.evaluate(new LocalDateDoubleTimeSeries[] {TS1, EMPTY_SERIES, TS2, EMPTY_SERIES}), RETURNS
        .evaluate(TS1));
    assertEquals(CALCULATOR.evaluate(new LocalDateDoubleTimeSeries[] {TS1, null, TS2, null}), RETURNS.evaluate(TS1));
  }
}
