/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.model;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;

import com.opengamma.analytics.financial.timeseries.analysis.AutocorrelationFunctionCalculator;
import com.opengamma.analytics.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.analytics.math.statistics.descriptive.MeanCalculator;
import com.opengamma.analytics.math.statistics.descriptive.SampleVarianceCalculator;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MovingAverageTimeSeriesModelTest {
  private static final double MEAN = 0;
  private static final double STD = 0.25;
  private static final MovingAverageTimeSeriesModel MODEL = new MovingAverageTimeSeriesModel(new NormalDistribution(MEAN, STD,
      new MersenneTwister64(MersenneTwister.DEFAULT_SEED)));
  private static final int ORDER = 2;
  private static final LocalDateDoubleTimeSeries MA;
  private static final double[] THETA;
  private static double LIMIT = 3;

  static {
    final int n = 20000;
    final LocalDate[] dates = new LocalDate[n];
    for (int i = 0; i < n; i++) {
      dates[i] = LocalDate.ofEpochDay(i);
    }
    THETA = new double[ORDER + 1];
    THETA[0] = 0.;
    for (int i = 1; i <= ORDER; i++) {
      THETA[i] = (i + 1) / 10.;
    }
    MA = MODEL.getSeries(THETA, ORDER, dates);
    LIMIT /= Math.sqrt(n);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadConstructor() {
    new MovingAverageTimeSeriesModel(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullThetas() {
    MODEL.getSeries(null, 2, new LocalDate[] {LocalDate.ofEpochDay(1)});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyThetas() {
    MODEL.getSeries(new double[0], 2, new LocalDate[] {LocalDate.ofEpochDay(1)});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeOrder() {
    MODEL.getSeries(new double[] {0.2}, -3, new LocalDate[] {LocalDate.ofEpochDay(1)});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInsufficientThetas() {
    MODEL.getSeries(new double[] {0.2}, 4, new LocalDate[] {LocalDate.ofEpochDay(1)});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDates() {
    MODEL.getSeries(new double[] {0.3}, 1, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyDates() {
    MODEL.getSeries(new double[] {0.3}, 1, new LocalDate[0]);
  }

  @Test
  public void testACF() {
    final double eps = 1e-2;
    final double[] rho = new AutocorrelationFunctionCalculator().evaluate(MA);
    assertEquals(rho[0], 1, 1e-16);
    final double denom = 1 + THETA[1] * THETA[1] + THETA[2] * THETA[2];
    assertEquals(rho[1], (THETA[1] * THETA[2] + THETA[1]) / denom, eps);
    assertEquals(rho[2], THETA[2] / denom, eps);
    for (int i = 1; i <= 20; i++) {
      if (i < ORDER + 1) {
        assertTrue(rho[i] > LIMIT);
      } else {
        assertTrue(rho[i] < LIMIT);
      }
    }
    final Double mean = new DoubleTimeSeriesStatisticsCalculator(new MeanCalculator()).evaluate(MA);
    assertEquals(mean, THETA[0], eps);
    final Double variance = new DoubleTimeSeriesStatisticsCalculator(new SampleVarianceCalculator()).evaluate(MA);
    double sum = 1;
    for (int i = 1; i <= ORDER; i++) {
      sum += THETA[i] * THETA[i];
    }
    assertEquals(variance, sum * STD * STD, eps);
  }
}
