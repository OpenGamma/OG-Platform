/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.analysis;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import cern.jet.random.engine.MersenneTwister64;

import com.opengamma.financial.timeseries.model.AutoregressiveTimeSeriesModel;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 * 
 */
public class AutoregressiveTimeSeriesOrderIdentifierTest {
  private static final AutoregressiveTimeSeriesPACFOrderIdentifier PACF_IDENTIFIER = new AutoregressiveTimeSeriesPACFOrderIdentifier(10, 0.05);
  private static final AutoregressiveTimeSeriesModel AR_MODEL =
      new AutoregressiveTimeSeriesModel(new NormalDistribution(0, 1, new MersenneTwister64(MersenneTwister64.DEFAULT_SEED)));
  private static final DoubleTimeSeries<Long> RANDOM;
  private static final DoubleTimeSeries<Long> AR3;
  private static final DoubleTimeSeries<Long> AR5;

  static {
    final int n = 50000;
    final long[] dates = new long[n];
    final double[] random = new double[n];
    final ProbabilityDistribution<Double> normal = new NormalDistribution(2, 1, new MersenneTwister64(MersenneTwister64.DEFAULT_SEED));
    for (int i = 0; i < n; i++) {
      dates[i] = i;
      random[i] = normal.nextRandom();
    }
    final int order = 3;
    final double[] coeffs = new double[order + 1];
    coeffs[0] = 0.1;
    for (int i = 1; i <= order; i++) {
      coeffs[i] = 1. / (i + 5);
    }
    AR3 = AR_MODEL.getSeries(coeffs, order, dates);
    AR5 = AR_MODEL.getSeries(new double[] {coeffs[0], coeffs[1], coeffs[2], 0., 0., 0.1}, 5, dates);
    RANDOM = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS, dates, random);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadOrder() {
    new AutoregressiveTimeSeriesPACFOrderIdentifier(-10, 0.05);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeLevel() {
    new AutoregressiveTimeSeriesPACFOrderIdentifier(20, -0.1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighLevel() {
    new AutoregressiveTimeSeriesPACFOrderIdentifier(20, 1.2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTS() {
    PACF_IDENTIFIER.getOrder(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyTS() {
    PACF_IDENTIFIER.getOrder(FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInsufficientData() {
    PACF_IDENTIFIER.getOrder(new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_NANOS, new long[] {1, 2}, new double[] {0.1, 0.2}));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRandomTS() {
    PACF_IDENTIFIER.getOrder(RANDOM);
  }

  @Test
  public void test() {
    assertEquals(PACF_IDENTIFIER.getOrder(AR3), 3);
    assertEquals(PACF_IDENTIFIER.getOrder(AR5), 5);
  }
}
