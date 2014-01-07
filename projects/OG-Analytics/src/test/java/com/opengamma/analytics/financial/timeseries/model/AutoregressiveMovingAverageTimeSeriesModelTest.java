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
import com.opengamma.analytics.financial.timeseries.analysis.AutocovarianceFunctionCalculator;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class AutoregressiveMovingAverageTimeSeriesModelTest {
  private static final double MEAN = 0;
  private static final double STD = 0.1;
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(MEAN, STD, new MersenneTwister64(MersenneTwister.DEFAULT_SEED));
  private static final AutoregressiveTimeSeriesModel AR_MODEL = new AutoregressiveTimeSeriesModel(NORMAL);
  private static final MovingAverageTimeSeriesModel MA_MODEL = new MovingAverageTimeSeriesModel(NORMAL);
  private static final AutoregressiveMovingAverageTimeSeriesModel MODEL = new AutoregressiveMovingAverageTimeSeriesModel(NORMAL);
  private static final LocalDate[] DATES;
  private static final int P = 3;
  private static final int Q = 6;
  private static final LocalDateDoubleTimeSeries ARMA11;
  private static final LocalDateDoubleTimeSeries MA;
  private static final LocalDateDoubleTimeSeries AR;
  private static final double[] PHI;
  private static final double[] THETA;

  static {
    final int n = 20000;
    DATES = new LocalDate[n];
    for (int i = 0; i < n; i++) {
      DATES[i] = LocalDate.ofEpochDay(i);
    }
    PHI = new double[P + 1];
    PHI[0] = 0.;
    for (int i = 1; i <= P; i++) {
      PHI[i] = (i + 2.) / 15.;
    }
    THETA = new double[Q];
    final double[] theta1 = new double[Q + 1];
    theta1[0] = 0.;
    for (int i = 0; i < Q; i++) {
      THETA[i] = (i % 2 == 0 ? -1 : 1) * (i + 1) / 10.;
      theta1[i + 1] = THETA[i];
    }
    ARMA11 = MODEL.getSeries(PHI, 1, THETA, 1, DATES);
    MA = MA_MODEL.getSeries(theta1, Q, DATES);
    AR = AR_MODEL.getSeries(PHI, P, DATES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    new AutoregressiveMovingAverageTimeSeriesModel(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPhi() {
    MODEL.getSeries(null, P, THETA, Q, DATES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeP() {
    MODEL.getSeries(PHI, -P, THETA, Q, DATES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongP() {
    MODEL.getSeries(PHI, 2 * P, THETA, Q, DATES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTheta() {
    MODEL.getSeries(PHI, P, null, Q, DATES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeQ() {
    MODEL.getSeries(PHI, P, THETA, -Q, DATES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongQ() {
    MODEL.getSeries(PHI, P, THETA, 2 * Q, DATES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDates() {
    MODEL.getSeries(PHI, P, THETA, Q, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyDates() {
    MODEL.getSeries(PHI, P, THETA, Q, new LocalDate[0]);
  }

  @Test
  public void test() {
    final AutocovarianceFunctionCalculator autocovariance = new AutocovarianceFunctionCalculator();
    final AutocorrelationFunctionCalculator autocorrelation = new AutocorrelationFunctionCalculator();
    final double[] rhoAR = autocorrelation.evaluate(AR);
    final double[] rhoMA = autocorrelation.evaluate(MA);
    final double[] rhoARMAP0 = autocorrelation.evaluate(MODEL.getSeries(PHI, P, null, 0, DATES));
    final double[] rhoARMA0Q = autocorrelation.evaluate(MODEL.getSeries(null, 0, THETA, Q, DATES));
    final double eps = Math.sqrt(STD) + 0.01;
    for (int i = 0; i < 200; i++) {
      assertEquals(Math.abs(rhoARMAP0[i] - rhoAR[i]), 0., eps);
      assertEquals(Math.abs(rhoARMA0Q[i] - rhoMA[i]), 0., eps);
    }
    final double[] rhoARMA11 = autocorrelation.evaluate(ARMA11);
    final double[] gammaARMA11 = autocovariance.evaluate(ARMA11);
    assertEquals(PHI[1] - THETA[1] * STD * STD / gammaARMA11[0], rhoARMA11[1], eps);
  }
}
