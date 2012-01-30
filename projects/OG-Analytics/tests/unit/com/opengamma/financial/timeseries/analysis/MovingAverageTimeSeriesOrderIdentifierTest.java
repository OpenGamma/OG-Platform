/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.analysis;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;

import com.opengamma.financial.timeseries.model.MovingAverageTimeSeriesModel;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class MovingAverageTimeSeriesOrderIdentifierTest {
  private static final MovingAverageTimeSeriesOrderIdentifier MA_IDENTIFIER = new MovingAverageTimeSeriesOrderIdentifier(10, 0.05);
  private static final MovingAverageTimeSeriesModel MA_MODEL =
      new MovingAverageTimeSeriesModel(new NormalDistribution(0, 1, new MersenneTwister64(MersenneTwister.DEFAULT_SEED)));
  private static final LocalDateDoubleTimeSeries RANDOM;
  private static final LocalDateDoubleTimeSeries MA3;

  static {
    final int n = 50000;
    final LocalDate[] dates = new LocalDate[n];
    final double[] random = new double[n];
    final ProbabilityDistribution<Double> normal = new NormalDistribution(0, 1, new MersenneTwister64(MersenneTwister.DEFAULT_SEED));
    for (int i = 0; i < n; i++) {
      dates[i] = LocalDate.ofEpochDays(i);
      random[i] = normal.nextRandom();
    }
    final int order = 3;
    final double[] coeffs = new double[order + 1];
    coeffs[0] = 0.1;
    for (int i = 1; i <= order; i++) {
      coeffs[i] = 1. / (i + 5);
    }
    MA3 = MA_MODEL.getSeries(coeffs, order, dates);
    RANDOM = new ArrayLocalDateDoubleTimeSeries(dates, random);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadOrder() {
    new MovingAverageTimeSeriesOrderIdentifier(-10, 0.05);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeLevel() {
    new MovingAverageTimeSeriesOrderIdentifier(20, -0.1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighLevel() {
    new MovingAverageTimeSeriesOrderIdentifier(20, 1.2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTS() {
    MA_IDENTIFIER.getOrder(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyTS() {
    MA_IDENTIFIER.getOrder(FastArrayLongDoubleTimeSeries.EMPTY_SERIES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInsufficientData() {
    MA_IDENTIFIER.getOrder(new ArrayLocalDateDoubleTimeSeries(new LocalDate[] {LocalDate.ofEpochDays(1), LocalDate.ofEpochDays(2)}, new double[] {0.1, 0.2}));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRandomTS() {
    MA_IDENTIFIER.getOrder(RANDOM);
  }

  @Test
  public void test() {
    assertEquals(MA_IDENTIFIER.getOrder(MA3), 3);
  }
}
