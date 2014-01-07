/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.model;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;

import com.opengamma.analytics.financial.timeseries.analysis.AutocorrelationFunctionCalculator;
import com.opengamma.analytics.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.analytics.math.statistics.descriptive.MeanCalculator;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class AutoregressiveTimeSeriesModelTest {
  private static final double MEAN = 0;
  private static final double STD = 0.25;
  private static final AutoregressiveTimeSeriesModel MODEL = new AutoregressiveTimeSeriesModel(new NormalDistribution(MEAN, STD, new MersenneTwister64(
      MersenneTwister.DEFAULT_SEED)));
  private static final int ORDER = 2;
  private static final LocalDateDoubleTimeSeries MA;
  private static final double[] PHI;
  @SuppressWarnings("unused")
  private static double LIMIT = 3;

  static {
    final int n = 20000;
    final LocalDate[] dates = new LocalDate[n];
    for (int i = 0; i < n; i++) {
      dates[i] = LocalDate.ofEpochDay(i);
    }
    PHI = new double[ORDER + 1];
    for (int i = 0; i <= ORDER; i++) {
      PHI[i] = (i + 1) / 10.;
    }
    MA = MODEL.getSeries(PHI, ORDER, dates);
    LIMIT /= Math.sqrt(n);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadConstructor() {
    new AutoregressiveTimeSeriesModel(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPhis() {
    MODEL.getSeries(null, 2, new LocalDate[] { LocalDate.ofEpochDay(1) });
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyPhis() {
    MODEL.getSeries(new double[0], 2, new LocalDate[] { LocalDate.ofEpochDay(1) });
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeOrder() {
    MODEL.getSeries(new double[] {0.2}, -3, new LocalDate[] { LocalDate.ofEpochDay(1) });
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInsufficientPhis() {
    MODEL.getSeries(new double[] {0.2}, 4, new LocalDate[] { LocalDate.ofEpochDay(1) });
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDates() {
    MODEL.getSeries(new double[] {0.3, 0.4}, 1, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyDates() {
    MODEL.getSeries(new double[] {0.3, 0.4}, 1, new LocalDate[0]);
  }

  @Test
  public void testACF() {
    final double eps = 1e-2;
    final double[] rho = new AutocorrelationFunctionCalculator().evaluate(MA);
    final double rho1 = PHI[1] / (1 - PHI[2]);
    assertEquals(rho[0], 1, 1e-16);
    assertEquals(rho[1], rho1, eps);
    assertEquals(rho[2], rho1 * PHI[1] + PHI[2], eps);
    final Double mean = new DoubleTimeSeriesStatisticsCalculator(new MeanCalculator()).evaluate(MA);
    assertEquals(mean, PHI[0] / (1 - PHI[1] - PHI[2]), eps);
  }
}
