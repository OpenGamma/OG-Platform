/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.returns;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.timeseries.TimeSeriesException;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.CalculationMode;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SimpleGrossTimeSeriesReturnCalculatorTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final Function<LocalDateDoubleTimeSeries, LocalDateDoubleTimeSeries> CALCULATOR = new SimpleGrossTimeSeriesReturnCalculator(CalculationMode.LENIENT);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArray() {
    CALCULATOR.evaluate((LocalDateDoubleTimeSeries[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyArray() {
    CALCULATOR.evaluate(new LocalDateDoubleTimeSeries[0]);
  }

  @Test(expectedExceptions = TimeSeriesException.class)
  public void testWithBadInputs() {
    final LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.ofEpochDay(1) }, new double[] {4});
    CALCULATOR.evaluate(new LocalDateDoubleTimeSeries[] {ts});
  }

  @Test
  public void testReturnsWithoutDividends() {
    final int n = 20;
    final LocalDate[] times = new LocalDate[n];
    final double[] data = new double[n];
    final double[] returns = new double[n - 1];
    double random;
    for (int i = 0; i < n; i++) {
      times[i] = LocalDate.ofEpochDay(i);
      random = RANDOM.nextDouble();
      data[i] = random;
      if (i > 0) {
        returns[i - 1] = random / data[i - 1];
      }
    }
    final LocalDateDoubleTimeSeries priceTS = ImmutableLocalDateDoubleTimeSeries.of(times, data);
    final LocalDateDoubleTimeSeries returnTS = ImmutableLocalDateDoubleTimeSeries.of(Arrays.copyOfRange(times, 1, n), returns);
    assertTrue(CALCULATOR.evaluate(new LocalDateDoubleTimeSeries[] {priceTS}).equals(returnTS));
  }

  @Test
  public void testReturnsWithZeroesInSeries() {
    final int n = 20;
    final LocalDate[] times = new LocalDate[n];
    final double[] data = new double[n];
    final double[] returns = new double[n - 3];
    double random;
    for (int i = 0; i < n - 2; i++) {
      times[i] = LocalDate.ofEpochDay(i);
      random = RANDOM.nextDouble();
      data[i] = random;
      if (i > 0) {
        returns[i - 1] = random / data[i - 1];
      }
    }
    times[n - 2] = LocalDate.ofEpochDay(n - 2);
    data[n - 2] = 0;
    times[n - 1] = LocalDate.ofEpochDay(n - 1);
    data[n - 1] = RANDOM.nextDouble();
    final LocalDateDoubleTimeSeries priceTS = ImmutableLocalDateDoubleTimeSeries.of(times, data);
    final LocalDateDoubleTimeSeries returnTS = ImmutableLocalDateDoubleTimeSeries.of(Arrays.copyOfRange(times, 1, n - 2), returns);
    final TimeSeriesReturnCalculator strict = new SimpleGrossTimeSeriesReturnCalculator(CalculationMode.STRICT);
    final LocalDateDoubleTimeSeries[] tsArray = new LocalDateDoubleTimeSeries[] {priceTS};
    try {
      strict.evaluate(tsArray);
      Assert.fail();
    } catch (final TimeSeriesException e) {
      // Expected
    }
    assertTrue(CALCULATOR.evaluate(tsArray).equals(returnTS));
  }

  @Test
  public void testReturnsWithDividendsAtDifferentTimes() {
    final int n = 20;
    final LocalDate[] times = new LocalDate[n];
    final double[] data = new double[n];
    final double[] returns = new double[n - 1];
    double random;
    for (int i = 0; i < n; i++) {
      times[i] = LocalDate.ofEpochDay(i);
      random = RANDOM.nextDouble();
      data[i] = random;
      if (i > 0) {
        returns[i - 1] = random / data[i - 1];
      }
    }
    final LocalDateDoubleTimeSeries dividendTS = ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] { LocalDate.ofEpochDay(300) }, new double[] {3});
    final LocalDateDoubleTimeSeries priceTS = ImmutableLocalDateDoubleTimeSeries.of(times, data);
    final LocalDateDoubleTimeSeries returnTS = ImmutableLocalDateDoubleTimeSeries.of(Arrays.copyOfRange(times, 1, n), returns);
    assertTrue(CALCULATOR.evaluate(new LocalDateDoubleTimeSeries[] {priceTS, dividendTS}).equals(returnTS));
  }

  @Test
  public void testReturnsWithDividend() {
    final int n = 20;
    final LocalDate[] times = new LocalDate[n];
    final double[] data = new double[n];
    final double[] returns = new double[n - 1];
    final LocalDate[] dividendTimes = new LocalDate[] { LocalDate.ofEpochDay(1), LocalDate.ofEpochDay(4) };
    final double[] dividendData = new double[] {0.4, 0.6};
    double random;
    for (int i = 0; i < n; i++) {
      times[i] = LocalDate.ofEpochDay(i);
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
    final LocalDateDoubleTimeSeries dividendTS = ImmutableLocalDateDoubleTimeSeries.of(dividendTimes, dividendData);
    final LocalDateDoubleTimeSeries priceTS = ImmutableLocalDateDoubleTimeSeries.of(times, data);
    final LocalDateDoubleTimeSeries returnTS = ImmutableLocalDateDoubleTimeSeries.of(Arrays.copyOfRange(times, 1, n), returns);
    assertTrue(CALCULATOR.evaluate(new LocalDateDoubleTimeSeries[] {priceTS, dividendTS}).equals(returnTS));
  }
}
