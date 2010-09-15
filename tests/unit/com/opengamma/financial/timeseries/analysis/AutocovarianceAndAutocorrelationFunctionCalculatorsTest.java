/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.analysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 * 
 */
public class AutocovarianceAndAutocorrelationFunctionCalculatorsTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final Function1D<DoubleTimeSeries<?>, double[]> COVARIANCE = new AutocovarianceFunctionCalculator();
  private static final Function1D<DoubleTimeSeries<?>, double[]> CORRELATION = new AutocorrelationFunctionCalculator();

  @Test(expected = IllegalArgumentException.class)
  public void testCovarianceWithNull() {
    COVARIANCE.evaluate((DoubleTimeSeries<?>) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCovarianceWithEmpty() {
    COVARIANCE.evaluate(FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCorrelationWithNull() {
    CORRELATION.evaluate((DoubleTimeSeries<?>) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCorrelationWithEmpty() {
    CORRELATION.evaluate(FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
  }

  @Test
  public void test() {
    final int n = 20000;
    final long[] dates = new long[n];
    final double[] data = new double[n];
    for (int i = 0; i < n; i++) {
      dates[i] = i;
      data[i] = RANDOM.nextDouble();
    }
    final double[] result = CORRELATION.evaluate(new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, dates, data));
    assertEquals(result[0], 1, 1e-16);
    final double level = 0.05;
    final double criticalValue = new NormalDistribution(0, 1).getInverseCDF(1 - level / 2.) / Math.sqrt(n);
    final int m = 500;
    final double expectedViolations = level * m;
    int sum = 0;
    for (int i = 1; i < m; i++) {
      if (Math.abs(result[i]) > criticalValue) {
        sum++;
      }
    }
    assertTrue(sum > expectedViolations - 10);
    assertTrue(sum < expectedViolations + 10);
  }
}
