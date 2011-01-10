/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.analysis;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.function.Function;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.descriptive.MeanCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastArrayIntDoubleTimeSeries;

/**
 * 
 */
public class DoubleTimeSeriesStatisticsCalculatorTest {
  private static final Function1D<double[], Double> MEAN = new MeanCalculator();
  private static final Function<DoubleTimeSeries<?>, Double> CALC = new DoubleTimeSeriesStatisticsCalculator(MEAN);
  private static final double X = 1.23;
  private static final DoubleTimeSeries<?> TS = new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new int[] {1, 2, 3, 4, 5}, new double[] {X, X, X, X, X});

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor() {
    new DoubleTimeSeriesStatisticsCalculator(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullTS() {
    CALC.evaluate((DoubleTimeSeries<?>) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyTS() {
    CALC.evaluate(FastArrayIntDoubleTimeSeries.EMPTY_SERIES);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullTSInArray() {
    CALC.evaluate(TS, null, TS);
  }

  @Test
  public void test() {
    assertEquals(CALC.evaluate(TS), MEAN.evaluate(TS.toFastLongDoubleTimeSeries().valuesArrayFast()), 1e-15);
  }
}
