/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.returns;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

import com.opengamma.math.function.Function;
import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.TimeSeriesException;
import com.opengamma.util.CalculationMode;

/**
 * 
 * @author emcleod
 */

public class ContinuouslyCompoundedTimeSeriesReturnCalculatorTest {
  private static final Function<DoubleTimeSeries, DoubleTimeSeries, TimeSeriesException> CALCULATOR = new ContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.LENIENT);

  @Test
  public void testWithBadInputs() {
    try {
      CALCULATOR.evaluate((DoubleTimeSeries[]) null);
      fail();
    } catch (final TimeSeriesException e) {
      // Expected
    }
    try {
      CALCULATOR.evaluate(new DoubleTimeSeries[0]);
      fail();
    } catch (final TimeSeriesException e) {
      // Expected
    }
    final DoubleTimeSeries ts = new ArrayDoubleTimeSeries(new long[] { 1 }, new double[] { 4 });
    try {
      CALCULATOR.evaluate(new DoubleTimeSeries[] { ts });
      fail();
    } catch (final TimeSeriesException e) {
      // Expected
    }
  }

  @Test
  public void testReturnsWithZeroesInSeries() throws TimeSeriesException {
    final int n = 20;
    final long[] times = new long[n];
    final double[] data = new double[n];
    final double[] returns = new double[n - 2];
    double random;
    for (int i = 0; i < n - 2; i++) {
      times[i] = i;
      random = Math.random();
      data[i] = random;
      if (i > 0) {
        returns[i - 1] = Math.log(random / data[i - 1]);
      }
    }
    times[n - 2] = n - 2;
    data[n - 2] = 0;
    times[n - 1] = n - 1;
    data[n - 1] = Math.random();
    final DoubleTimeSeries priceTS = new ArrayDoubleTimeSeries(times, data);
    final DoubleTimeSeries returnTS = new ArrayDoubleTimeSeries(Arrays.copyOfRange(times, 1, n - 2), returns);
    final TimeSeriesReturnCalculator strict = new ContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.STRICT);
    final DoubleTimeSeries[] tsArray = new DoubleTimeSeries[] { priceTS };
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
  public void testReturnsWithoutDividends() throws TimeSeriesException {
    final int n = 20;
    final long[] times = new long[n];
    final double[] data = new double[n];
    final double[] returns = new double[n - 1];
    double random;
    for (int i = 0; i < n; i++) {
      times[i] = i;
      random = Math.random();
      data[i] = random;
      if (i > 0) {
        returns[i - 1] = Math.log(random / data[i - 1]);
      }
    }
    final DoubleTimeSeries priceTS = new ArrayDoubleTimeSeries(times, data);
    final DoubleTimeSeries returnTS = new ArrayDoubleTimeSeries(Arrays.copyOfRange(times, 1, n), returns);
    assertTrue(CALCULATOR.evaluate(new DoubleTimeSeries[] { priceTS }).equals(returnTS));
  }

  @Test
  public void testReturnsWithDividendsAtDifferentTimes() throws TimeSeriesException {
    final int n = 20;
    final long[] times = new long[n];
    final double[] data = new double[n];
    final double[] returns = new double[n - 1];
    double random;
    for (int i = 0; i < n; i++) {
      times[i] = i;
      random = Math.random();
      data[i] = random;
      if (i > 0) {
        returns[i - 1] = Math.log(random / data[i - 1]);
      }
    }
    final DoubleTimeSeries dividendTS = new ArrayDoubleTimeSeries(new long[] { 300 }, new double[] { 3 });
    final DoubleTimeSeries priceTS = new ArrayDoubleTimeSeries(times, data);
    final DoubleTimeSeries returnTS = new ArrayDoubleTimeSeries(Arrays.copyOfRange(times, 1, n), returns);
    assertTrue(CALCULATOR.evaluate(new DoubleTimeSeries[] { priceTS, dividendTS }).equals(returnTS));
  }

  @Test
  public void testReturnsWithDividends() throws TimeSeriesException {
    final int n = 20;
    final long[] times = new long[n];
    final double[] data = new double[n];
    final double[] returns = new double[n - 1];
    double random;
    for (int i = 0; i < n; i++) {
      times[i] = i;
      random = Math.random();
      data[i] = random;
      if (i > 0) {
        returns[i - 1] = Math.log(random / data[i - 1]);
      }
    }
    final DoubleTimeSeries priceTS = new ArrayDoubleTimeSeries(times, data);
    final DoubleTimeSeries returnTS = new ArrayDoubleTimeSeries(Arrays.copyOfRange(times, 1, n), returns);
    assertTrue(CALCULATOR.evaluate(new DoubleTimeSeries[] { priceTS }).equals(returnTS));
  }
}
