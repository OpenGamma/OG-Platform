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

public class SimpleGrossTimeSeriesReturnCalculatorTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final Function<DoubleTimeSeries<?>, DoubleTimeSeries<?>> CALCULATOR = new SimpleGrossTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  private static final DateTimeNumericEncoding ENCODING = DateTimeNumericEncoding.DATE_EPOCH_DAYS;

  @Test(expected = IllegalArgumentException.class)
  public void testNullArray() {
    CALCULATOR.evaluate((DoubleTimeSeries[]) null);
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
        returns[i - 1] = random / data[i - 1];
      }
    }
    final DoubleTimeSeries<Long> priceTS = new FastArrayLongDoubleTimeSeries(ENCODING, times, data);
    final DoubleTimeSeries<Long> returnTS = new FastArrayLongDoubleTimeSeries(ENCODING, Arrays.copyOfRange(times, 1, n), returns);
    assertTrue(CALCULATOR.evaluate(new DoubleTimeSeries[] {priceTS}).equals(returnTS));
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
        returns[i - 1] = random / data[i - 1];
      }
    }
    times[n - 2] = n - 2;
    data[n - 2] = 0;
    times[n - 1] = n - 1;
    data[n - 1] = RANDOM.nextDouble();
    final DoubleTimeSeries<Long> priceTS = new FastArrayLongDoubleTimeSeries(ENCODING, times, data);
    final DoubleTimeSeries<Long> returnTS = new FastArrayLongDoubleTimeSeries(ENCODING, Arrays.copyOfRange(times, 1, n - 2), returns);
    final TimeSeriesReturnCalculator strict = new SimpleGrossTimeSeriesReturnCalculator(CalculationMode.STRICT);
    final DoubleTimeSeries<Long>[] tsArray = new DoubleTimeSeries[] {priceTS};
    try {
      strict.evaluate(tsArray);
      fail();
    } catch (final TimeSeriesException e) {
      // Expected
    }
    assertTrue(CALCULATOR.evaluate(tsArray).equals(returnTS));
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
        returns[i - 1] = random / data[i - 1];
      }
    }
    final DoubleTimeSeries<Long> dividendTS = new FastArrayLongDoubleTimeSeries(ENCODING, new long[] {300}, new double[] {3});
    final DoubleTimeSeries<Long> priceTS = new FastArrayLongDoubleTimeSeries(ENCODING, times, data);
    final DoubleTimeSeries<Long> returnTS = new FastArrayLongDoubleTimeSeries(ENCODING, Arrays.copyOfRange(times, 1, n), returns);
    assertTrue(CALCULATOR.evaluate(new DoubleTimeSeries[] {priceTS, dividendTS}).equals(returnTS));
  }

  @Test
  public void testReturnsWithDividend() {
    final int n = 20;
    final long[] times = new long[n];
    final double[] data = new double[n];
    final double[] returns = new double[n - 1];
    final long[] dividendTimes = new long[] {1, 4};
    final double[] dividendData = new double[] {0.4, 0.6};
    double random;
    for (int i = 0; i < n; i++) {
      times[i] = i;
      random = RANDOM.nextDouble();
      data[i] = random;
      if (i > 0) {
        if (i == 1) {
          returns[i - 1] = (random + dividendData[0]) / data[i - 1];
        } else if (i == 4) {
          returns[i - 1] = (random + dividendData[1]) / data[i - 1];
        } else {
          returns[i - 1] = random / data[i - 1];
        }
      }
    }
    final DoubleTimeSeries<Long> dividendTS = new FastArrayLongDoubleTimeSeries(ENCODING, dividendTimes, dividendData);
    final DoubleTimeSeries<Long> priceTS = new FastArrayLongDoubleTimeSeries(ENCODING, times, data);
    final DoubleTimeSeries<Long> returnTS = new FastArrayLongDoubleTimeSeries(ENCODING, Arrays.copyOfRange(times, 1, n), returns);
    assertTrue(CALCULATOR.evaluate(new DoubleTimeSeries[] {priceTS, dividendTS}).equals(returnTS));
  }
}
