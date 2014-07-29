/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.returns;

import static org.testng.Assert.assertTrue;

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
/**
 * 
 */
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ContinuouslyCompoundedTimeSeriesReturnCalculatorTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final Function<LocalDateDoubleTimeSeries, LocalDateDoubleTimeSeries> CALCULATOR = new ContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.LENIENT);
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArray() {
    CALCULATOR.evaluate((LocalDateDoubleTimeSeries) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyArray() {
    CALCULATOR.evaluate(new LocalDateDoubleTimeSeries[0]);
  }

  @Test(expectedExceptions = TimeSeriesException.class)
  public void testWithBadInputs() {
    final LocalDateDoubleTimeSeries ts = ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] { LocalDate.ofEpochDay(1) }, new double[] {4});
    CALCULATOR.evaluate(new LocalDateDoubleTimeSeries[] {ts});
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
        returns[i - 1] = Math.log(random / data[i - 1]);
      }
    }
    times[n - 2] = LocalDate.ofEpochDay(n - 2);
    data[n - 2] = 0;
    times[n - 1] = LocalDate.ofEpochDay(n - 1);
    data[n - 1] = RANDOM.nextDouble();
    final LocalDateDoubleTimeSeries priceTS = ImmutableLocalDateDoubleTimeSeries.of(times, data);
    final LocalDateDoubleTimeSeries returnTS = ImmutableLocalDateDoubleTimeSeries.of(Arrays.copyOfRange(times, 1, n - 2), returns);
    final TimeSeriesReturnCalculator strict = new ContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.STRICT);
    final LocalDateDoubleTimeSeries[] tsArray = new LocalDateDoubleTimeSeries[] {priceTS};
    try {
      strict.evaluate(tsArray);
      Assert.fail();
    } catch (final TimeSeriesException e) {
      // Expected
    }
    final TimeSeriesReturnCalculator lenient = new ContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.LENIENT);
    assertTrue(lenient.evaluate(tsArray).equals(returnTS));
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
        returns[i - 1] = Math.log(random / data[i - 1]);
      }
    }
    final LocalDateDoubleTimeSeries priceTS = ImmutableLocalDateDoubleTimeSeries.of(times, data);
    final LocalDateDoubleTimeSeries returnTS = ImmutableLocalDateDoubleTimeSeries.of(Arrays.copyOfRange(times, 1, n), returns);
    assertTrue(CALCULATOR.evaluate(new LocalDateDoubleTimeSeries[] {priceTS}).equals(returnTS));
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
        returns[i - 1] = Math.log(random / data[i - 1]);
      }
    }
    final LocalDateDoubleTimeSeries dividendTS = ImmutableLocalDateDoubleTimeSeries.of(new LocalDate[] {LocalDate.ofEpochDay(300)}, new double[] {3});
    final LocalDateDoubleTimeSeries priceTS = ImmutableLocalDateDoubleTimeSeries.of(times, data);
    final LocalDateDoubleTimeSeries returnTS = ImmutableLocalDateDoubleTimeSeries.of(Arrays.copyOfRange(times, 1, n), returns);
    assertTrue(CALCULATOR.evaluate(new LocalDateDoubleTimeSeries[] {priceTS, dividendTS}).equals(returnTS));
  }

  @Test
  public void testReturnsWithDividends() {
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
        returns[i - 1] = Math.log(random / data[i - 1]);
      }
    }
    final LocalDateDoubleTimeSeries priceTS = ImmutableLocalDateDoubleTimeSeries.of(times, data);
    final LocalDateDoubleTimeSeries returnTS = ImmutableLocalDateDoubleTimeSeries.of(Arrays.copyOfRange(times, 1, n), returns);
    assertTrue(CALCULATOR.evaluate(new LocalDateDoubleTimeSeries[] {priceTS}).equals(returnTS));
  }
}
