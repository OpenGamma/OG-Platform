/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.returns;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import javax.time.calendar.TimeZone;

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
  private static final Function<DoubleTimeSeries, DoubleTimeSeries> CALCULATOR = new ContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode.LENIENT);

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
    final DoubleTimeSeries ts = new ArrayDoubleTimeSeries(new long[] { 1 }, new double[] { 4 }, new TimeZone[] { TimeZone.UTC });
    try {
      CALCULATOR.evaluate(new DoubleTimeSeries[] { ts });
      fail();
    } catch (final TimeSeriesException e) {
      // Expected
    }
  }

  @Test
  public void testReturnsWithZeroesInSeries() {
    final int n = 20;
    final long[] times = new long[n];
    final double[] data = new double[n];
    final TimeZone[] zones = new TimeZone[n];
    final double[] returns = new double[n - 3];
    double random;
    for (int i = 0; i < n - 2; i++) {
      times[i] = i;
      random = Math.random();
      data[i] = random;
      if (i > 0) {
        returns[i - 1] = Math.log(random / data[i - 1]);
      }
      zones[i] = TimeZone.UTC;
    }
    times[n - 2] = n - 2;
    data[n - 2] = 0;
    zones[n - 2] = TimeZone.UTC;
    times[n - 1] = n - 1;
    data[n - 1] = Math.random();
    zones[n - 1] = TimeZone.UTC;
    final DoubleTimeSeries priceTS = new ArrayDoubleTimeSeries(times, data, zones);
    final DoubleTimeSeries returnTS = new ArrayDoubleTimeSeries(Arrays.copyOfRange(times, 1, n - 2), returns, Arrays.copyOfRange(zones, 1, n - 2));
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
  public void testReturnsWithoutDividends() {
    final int n = 20;
    final long[] times = new long[n];
    final double[] data = new double[n];
    final TimeZone[] zones = new TimeZone[n];
    final double[] returns = new double[n - 1];
    double random;
    for (int i = 0; i < n; i++) {
      times[i] = i;
      random = Math.random();
      data[i] = random;
      if (i > 0) {
        returns[i - 1] = Math.log(random / data[i - 1]);
      }
      zones[i] = TimeZone.UTC;
    }
    final DoubleTimeSeries priceTS = new ArrayDoubleTimeSeries(times, data, zones);
    final DoubleTimeSeries returnTS = new ArrayDoubleTimeSeries(Arrays.copyOfRange(times, 1, n), returns, Arrays.copyOfRange(zones, 1, n));
    assertTrue(CALCULATOR.evaluate(new DoubleTimeSeries[] { priceTS }).equals(returnTS));
  }

  @Test
  public void testReturnsWithDividendsAtDifferentTimes() {
    final int n = 20;
    final long[] times = new long[n];
    final double[] data = new double[n];
    final TimeZone[] zones = new TimeZone[n];
    final double[] returns = new double[n - 1];
    double random;
    for (int i = 0; i < n; i++) {
      times[i] = i;
      random = Math.random();
      data[i] = random;
      if (i > 0) {
        returns[i - 1] = Math.log(random / data[i - 1]);
      }
      zones[i] = TimeZone.UTC;
    }
    final DoubleTimeSeries dividendTS = new ArrayDoubleTimeSeries(new long[] { 300 }, new double[] { 3 }, new TimeZone[] { TimeZone.UTC });
    final DoubleTimeSeries priceTS = new ArrayDoubleTimeSeries(times, data, zones);
    final DoubleTimeSeries returnTS = new ArrayDoubleTimeSeries(Arrays.copyOfRange(times, 1, n), returns, Arrays.copyOfRange(zones, 1, n));
    assertTrue(CALCULATOR.evaluate(new DoubleTimeSeries[] { priceTS, dividendTS }).equals(returnTS));
  }

  @Test
  public void testReturnsWithDividends() {
    final int n = 20;
    final long[] times = new long[n];
    final double[] data = new double[n];
    final TimeZone[] zones = new TimeZone[n];
    final double[] returns = new double[n - 1];
    double random;
    for (int i = 0; i < n; i++) {
      times[i] = i;
      random = Math.random();
      data[i] = random;
      if (i > 0) {
        returns[i - 1] = Math.log(random / data[i - 1]);
      }
      zones[i] = TimeZone.UTC;
    }
    final DoubleTimeSeries priceTS = new ArrayDoubleTimeSeries(times, data, zones);
    final DoubleTimeSeries returnTS = new ArrayDoubleTimeSeries(Arrays.copyOfRange(times, 1, n), returns, Arrays.copyOfRange(zones, 1, n));
    assertTrue(CALCULATOR.evaluate(new DoubleTimeSeries[] { priceTS }).equals(returnTS));
  }
}
