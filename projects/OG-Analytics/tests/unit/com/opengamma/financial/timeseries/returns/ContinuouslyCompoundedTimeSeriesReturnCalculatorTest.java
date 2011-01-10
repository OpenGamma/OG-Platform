/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.returns;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.function.Function;
import com.opengamma.util.CalculationMode;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.TimeSeriesException;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 * 
 */

public class ContinuouslyCompoundedTimeSeriesReturnCalculatorTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final Function<DoubleTimeSeries<?>, DoubleTimeSeries<?>> CALCULATOR = new ContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  private static final DateTimeNumericEncoding ENCODING = DateTimeNumericEncoding.DATE_EPOCH_DAYS;

  @Test(expected = IllegalArgumentException.class)
  public void testNullArray() {
    CALCULATOR.evaluate((DoubleTimeSeries<Long>) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyArray() {
    CALCULATOR.evaluate(new DoubleTimeSeries[0]);
  }

  @Test(expected = TimeSeriesException.class)
  public void testWithBadInputs() {
    final DoubleTimeSeries<Long> ts = new FastArrayLongDoubleTimeSeries(ENCODING, new long[] {1}, new double[] {4});
    CALCULATOR.evaluate(new DoubleTimeSeries[] {ts});
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testReturnsWithZeroesInSeries() {
    final int n = 20;
    final long[] times = new long[n];
    final double[] data = new double[n];
    final double[] returns = new double[n - 3];
    double random;
    for (int i = 0; i < n - 2; i++) {
      times[i] = i;
      random = RANDOM.nextDouble();
      data[i] = random;
      if (i > 0) {
        returns[i - 1] = Math.log(random / data[i - 1]);
      }
    }
    times[n - 2] = n - 2;
    data[n - 2] = 0;
    times[n - 1] = n - 1;
    data[n - 1] = RANDOM.nextDouble();
    final DoubleTimeSeries<Long> priceTS = new FastArrayLongDoubleTimeSeries(ENCODING, times, data);
    final DoubleTimeSeries<Long> returnTS = new FastArrayLongDoubleTimeSeries(ENCODING, Arrays.copyOfRange(times, 1, n - 2), returns);
    final TimeSeriesReturnCalculator strict = new ContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.STRICT);
    final DoubleTimeSeries<Long>[] tsArray = new DoubleTimeSeries[] {priceTS};
    try {
      strict.evaluate(tsArray);
      fail();
    } catch (final TimeSeriesException e) {
      // Expected
    }
    final TimeSeriesReturnCalculator lenient = new ContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.LENIENT);
    assertTrue(lenient.evaluate(tsArray).equals(returnTS));
  }

  @Test
  public void testReturnsWithoutDividends() {
    final int n = 20;
    final long[] times = new long[n];
    final double[] data = new double[n];
    final double[] returns = new double[n - 1];
    double random;
    for (int i = 0; i < n; i++) {
      times[i] = i;
      random = RANDOM.nextDouble();
      data[i] = random;
      if (i > 0) {
        returns[i - 1] = Math.log(random / data[i - 1]);
      }
    }
    final DoubleTimeSeries<Long> priceTS = new FastArrayLongDoubleTimeSeries(ENCODING, times, data);
    final DoubleTimeSeries<Long> returnTS = new FastArrayLongDoubleTimeSeries(ENCODING, Arrays.copyOfRange(times, 1, n), returns);
    assertTrue(CALCULATOR.evaluate(new DoubleTimeSeries[] {priceTS}).equals(returnTS));
  }

  @Test
  public void testReturnsWithDividendsAtDifferentTimes() {
    final int n = 20;
    final long[] times = new long[n];
    final double[] data = new double[n];
    final double[] returns = new double[n - 1];
    double random;
    for (int i = 0; i < n; i++) {
      times[i] = i;
      random = RANDOM.nextDouble();
      data[i] = random;
      if (i > 0) {
        returns[i - 1] = Math.log(random / data[i - 1]);
      }
    }
    final DoubleTimeSeries<Long> dividendTS = new FastArrayLongDoubleTimeSeries(ENCODING, new long[] {300}, new double[] {3});
    final DoubleTimeSeries<Long> priceTS = new FastArrayLongDoubleTimeSeries(ENCODING, times, data);
    final DoubleTimeSeries<Long> returnTS = new FastArrayLongDoubleTimeSeries(ENCODING, Arrays.copyOfRange(times, 1, n), returns);
    assertTrue(CALCULATOR.evaluate(new DoubleTimeSeries[] {priceTS, dividendTS}).equals(returnTS));
  }

  @Test
  public void testReturnsWithDividends() {
    final int n = 20;
    final long[] times = new long[n];
    final double[] data = new double[n];
    final double[] returns = new double[n - 1];
    double random;
    for (int i = 0; i < n; i++) {
      times[i] = i;
      random = RANDOM.nextDouble();
      data[i] = random;
      if (i > 0) {
        returns[i - 1] = Math.log(random / data[i - 1]);
      }
    }
    final DoubleTimeSeries<Long> priceTS = new FastArrayLongDoubleTimeSeries(ENCODING, times, data);
    final DoubleTimeSeries<Long> returnTS = new FastArrayLongDoubleTimeSeries(ENCODING, Arrays.copyOfRange(times, 1, n), returns);
    assertTrue(CALCULATOR.evaluate(new DoubleTimeSeries[] {priceTS}).equals(returnTS));
  }
}
