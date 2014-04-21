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
public class SimpleNetRelativeTimeSeriesReturnCalculatorTest {
  private static final TimeSeriesReturnCalculator STRICT_CALCULATOR = TimeSeriesReturnCalculatorFactory
      .getReturnCalculator(TimeSeriesReturnCalculatorFactory.SIMPLE_NET_RELATIVE_STRICT);
  private static final TimeSeriesReturnCalculator LENIENT_CALCULATOR = TimeSeriesReturnCalculatorFactory
      .getReturnCalculator(TimeSeriesReturnCalculatorFactory.SIMPLE_NET_RELATIVE_LENIENT);
  private static final LocalDateDoubleTimeSeries TS1 = ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.ofEpochDay(1), LocalDate.ofEpochDay(2), 
                                                                                                           LocalDate.ofEpochDay(3), LocalDate.ofEpochDay(4), 
                                                                                                           LocalDate.ofEpochDay(5) }, 
                                                                                          new double[] {1, 2, 3, 4, 5});
  private static final LocalDateDoubleTimeSeries TS2 = ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.ofEpochDay(1), LocalDate.ofEpochDay(2), 
                                                                                                           LocalDate.ofEpochDay(3), LocalDate.ofEpochDay(4), 
                                                                                                           LocalDate.ofEpochDay(5) }, 
                                                                                          new double[] {2, 4, 6, 8, 10});
  private static final LocalDateDoubleTimeSeries TS3 = ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.ofEpochDay(1), LocalDate.ofEpochDay(2), 
                                                                                                           LocalDate.ofEpochDay(3), LocalDate.ofEpochDay(4), 
                                                                                                           LocalDate.ofEpochDay(6) }, 
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
    assertEquals(result, ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] { LocalDate.ofEpochDay(1), LocalDate.ofEpochDay(2), 
                                                                              LocalDate.ofEpochDay(3), LocalDate.ofEpochDay(4), 
                                                                              LocalDate.ofEpochDay(5) }, 
                                                            new double[] {-0.5, -0.5, -0.5, -0.5, -0.5}));
  }

  @Test
  public void testLenient() {
    final LocalDateDoubleTimeSeries result = LENIENT_CALCULATOR.evaluate(TS1, TS3);
    assertEquals(result.size(), 4);
    assertEquals(result, ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] { LocalDate.ofEpochDay(1), LocalDate.ofEpochDay(2), 
                                                                              LocalDate.ofEpochDay(3), LocalDate.ofEpochDay(4)}, 
                                                            new double[] {-0.5, -0.5, -0.5, -0.5}));
  }
}
