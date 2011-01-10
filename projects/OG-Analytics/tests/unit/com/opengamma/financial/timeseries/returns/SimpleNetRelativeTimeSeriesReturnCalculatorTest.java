/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.returns;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.TimeSeriesException;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastArrayIntDoubleTimeSeries;

/**
 * 
 */
public class SimpleNetRelativeTimeSeriesReturnCalculatorTest {
  private static final TimeSeriesReturnCalculator STRICT_CALCULATOR = TimeSeriesReturnCalculatorFactory
      .getReturnCalculator(TimeSeriesReturnCalculatorFactory.SIMPLE_NET_RELATIVE_STRICT);
  private static final TimeSeriesReturnCalculator LENIENT_CALCULATOR = TimeSeriesReturnCalculatorFactory
      .getReturnCalculator(TimeSeriesReturnCalculatorFactory.SIMPLE_NET_RELATIVE_LENIENT);
  private static final DoubleTimeSeries<?> TS1 = new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new int[] {1, 2, 3, 4, 5}, new double[] {1, 2, 3, 4, 5});
  private static final DoubleTimeSeries<?> TS2 = new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new int[] {1, 2, 3, 4, 5}, new double[] {2, 4, 6, 8, 10});
  private static final DoubleTimeSeries<?> TS3 = new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new int[] {1, 2, 3, 4, 6}, new double[] {2, 4, 6, 8, 10});

  @Test(expected = IllegalArgumentException.class)
  public void testNullArray() {
    STRICT_CALCULATOR.evaluate((DoubleTimeSeries<?>[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFirstElement() {
    STRICT_CALCULATOR.evaluate(null, TS2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSecondElement() {
    STRICT_CALCULATOR.evaluate(TS1, null);
  }

  @Test(expected = TimeSeriesException.class)
  public void testDifferentDates() {
    STRICT_CALCULATOR.evaluate(TS1, TS3);
  }

  @Test
  public void testStrict() {
    final DoubleTimeSeries<?> result = STRICT_CALCULATOR.evaluate(TS1, TS2);
    assertEquals(result.size(), 5);
    assertEquals(result, new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new int[] {1, 2, 3, 4, 5}, new double[] {-0.5, -0.5, -0.5, -0.5, -0.5}));
  }

  @Test
  public void testLenient() {
    final DoubleTimeSeries<?> result = LENIENT_CALCULATOR.evaluate(TS1, TS3);
    assertEquals(result.size(), 4);
    assertEquals(result, new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new int[] {1, 2, 3, 4}, new double[] {-0.5, -0.5, -0.5, -0.5}));
  }
}
