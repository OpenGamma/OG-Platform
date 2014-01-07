/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.analysis;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;

import com.opengamma.analytics.financial.timeseries.model.AutoregressiveTimeSeriesModel;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.precise.instant.ImmutableInstantDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class AutoregressiveTimeSeriesOrderIdentifierTest {
  private static final AutoregressiveTimeSeriesPACFOrderIdentifier PACF_IDENTIFIER = new AutoregressiveTimeSeriesPACFOrderIdentifier(10, 0.05);
  private static final AutoregressiveTimeSeriesModel AR_MODEL =
      new AutoregressiveTimeSeriesModel(new NormalDistribution(0, 1, new MersenneTwister64(MersenneTwister.DEFAULT_SEED)));
  private static final LocalDateDoubleTimeSeries RANDOM;
  private static final LocalDateDoubleTimeSeries AR3;
  private static final LocalDateDoubleTimeSeries AR5;

  static {
    final int n = 50000;
    final LocalDate[] dates = new LocalDate[n];
    final double[] random = new double[n];
    final ProbabilityDistribution<Double> normal = new NormalDistribution(2, 1, new MersenneTwister64(MersenneTwister.DEFAULT_SEED));
    for (int i = 0; i < n; i++) {
      dates[i] = LocalDate.ofEpochDay(i);
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
    RANDOM = ImmutableLocalDateDoubleTimeSeries.of(dates, random);
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
    PACF_IDENTIFIER.getOrder(ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInsufficientData() {
    PACF_IDENTIFIER.getOrder(ImmutableInstantDoubleTimeSeries.of(new long[] {1, 2}, new double[] {0.1, 0.2}));
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
