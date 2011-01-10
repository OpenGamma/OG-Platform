/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
public class ExcessContinuouslyCompoundedTimeSeriesReturnCalculatorTest {
  private static final TimeSeriesReturnCalculator CALCULATOR = TimeSeriesReturnCalculatorFactory.getReturnCalculator(TimeSeriesReturnCalculatorFactory.EXCESS_CONTINUOUS_STRICT);
  private static final DoubleTimeSeries<?> TS1 = new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new int[] {1, 2, 3, 4, 5}, new double[] {1, 2, 3, 4, 5});
  private static final DoubleTimeSeries<?> TS2 = new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new int[] {1, 2, 3, 4, 5}, new double[] {1, 1, 1, 1, 1});
  private static final TimeSeriesReturnCalculator RETURNS = TimeSeriesReturnCalculatorFactory.getReturnCalculator(TimeSeriesReturnCalculatorFactory.CONTINUOUS_STRICT);

  @Test(expected = IllegalArgumentException.class)
  public void testNullArray() {
    CALCULATOR.evaluate((DoubleTimeSeries<?>[]) null);
  }

  @Test(expected = TimeSeriesException.class)
  public void testSmallArray() {
    CALCULATOR.evaluate(new DoubleTimeSeries<?>[] {TS1, TS2});
  }

  @Test(expected = TimeSeriesException.class)
  public void testDifferentLengths() {
    CALCULATOR.evaluate(new DoubleTimeSeries<?>[] {TS1, null, new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new int[] {1}, new double[] {1}), null});
  }

  @Test
  public void test() {
    assertEquals(CALCULATOR.evaluate(new DoubleTimeSeries<?>[] {TS1, FastArrayIntDoubleTimeSeries.EMPTY_SERIES, TS2, FastArrayIntDoubleTimeSeries.EMPTY_SERIES}), RETURNS
        .evaluate(TS1));
    assertEquals(CALCULATOR.evaluate(new DoubleTimeSeries<?>[] {TS1, null, TS2, null}), RETURNS.evaluate(TS1));
  }
}
