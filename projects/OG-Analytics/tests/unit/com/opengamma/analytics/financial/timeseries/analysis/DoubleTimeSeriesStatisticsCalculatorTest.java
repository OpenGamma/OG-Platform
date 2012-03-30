/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.analysis;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.descriptive.MeanCalculator;
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

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructor() {
    new DoubleTimeSeriesStatisticsCalculator(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTS() {
    CALC.evaluate((DoubleTimeSeries<?>) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyTS() {
    CALC.evaluate(FastArrayIntDoubleTimeSeries.EMPTY_SERIES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTSInArray() {
    CALC.evaluate(TS, null, TS);
  }

  @Test
  public void test() {
    assertEquals(CALC.evaluate(TS), MEAN.evaluate(TS.valuesArrayFast()), 1e-15);
  }
}
