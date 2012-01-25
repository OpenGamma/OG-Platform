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
public class SimpleNetRelativeTimeSeriesReturnCalculatorTest {
  private static final TimeSeriesReturnCalculator STRICT_CALCULATOR = TimeSeriesReturnCalculatorFactory
      .getReturnCalculator(TimeSeriesReturnCalculatorFactory.SIMPLE_NET_RELATIVE_STRICT);
  private static final TimeSeriesReturnCalculator LENIENT_CALCULATOR = TimeSeriesReturnCalculatorFactory
      .getReturnCalculator(TimeSeriesReturnCalculatorFactory.SIMPLE_NET_RELATIVE_LENIENT);
  private static final LocalDateDoubleTimeSeries TS1 = new ArrayLocalDateDoubleTimeSeries(new LocalDate[] {LocalDate.ofEpochDays(1), LocalDate.ofEpochDays(2), 
                                                                                                           LocalDate.ofEpochDays(3), LocalDate.ofEpochDays(4), 
                                                                                                           LocalDate.ofEpochDays(5) }, 
                                                                                          new double[] {1, 2, 3, 4, 5});
  private static final LocalDateDoubleTimeSeries TS2 = new ArrayLocalDateDoubleTimeSeries(new LocalDate[] {LocalDate.ofEpochDays(1), LocalDate.ofEpochDays(2), 
                                                                                                           LocalDate.ofEpochDays(3), LocalDate.ofEpochDays(4), 
                                                                                                           LocalDate.ofEpochDays(5) }, 
                                                                                          new double[] {2, 4, 6, 8, 10});
  private static final LocalDateDoubleTimeSeries TS3 = new ArrayLocalDateDoubleTimeSeries(new LocalDate[] {LocalDate.ofEpochDays(1), LocalDate.ofEpochDays(2), 
                                                                                                           LocalDate.ofEpochDays(3), LocalDate.ofEpochDays(4), 
                                                                                                           LocalDate.ofEpochDays(6) }, 
                                                                                          new double[] {2, 4, 6, 8, 10});

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArray() {
    STRICT_CALCULATOR.evaluate((LocalDateDoubleTimeSeries[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFirstElement() {
    STRICT_CALCULATOR.evaluate(null, TS2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSecondElement() {
    STRICT_CALCULATOR.evaluate(TS1, null);
  }

  @Test(expectedExceptions = TimeSeriesException.class)
  public void testDifferentDates() {
    STRICT_CALCULATOR.evaluate(TS1, TS3);
  }

  @Test
  public void testStrict() {
    final LocalDateDoubleTimeSeries result = STRICT_CALCULATOR.evaluate(TS1, TS2);
    assertEquals(result.size(), 5);
    assertEquals(result, new ArrayLocalDateDoubleTimeSeries(new LocalDate[] { LocalDate.ofEpochDays(1), LocalDate.ofEpochDays(2), 
                                                                              LocalDate.ofEpochDays(3), LocalDate.ofEpochDays(4), 
                                                                              LocalDate.ofEpochDays(5) }, 
                                                            new double[] {-0.5, -0.5, -0.5, -0.5, -0.5}));
  }

  @Test
  public void testLenient() {
    final LocalDateDoubleTimeSeries result = LENIENT_CALCULATOR.evaluate(TS1, TS3);
    assertEquals(result.size(), 4);
    assertEquals(result, new ArrayLocalDateDoubleTimeSeries(new LocalDate[] { LocalDate.ofEpochDays(1), LocalDate.ofEpochDays(2), 
                                                                              LocalDate.ofEpochDays(3), LocalDate.ofEpochDays(4)}, 
                                                            new double[] {-0.5, -0.5, -0.5, -0.5}));
  }
}
