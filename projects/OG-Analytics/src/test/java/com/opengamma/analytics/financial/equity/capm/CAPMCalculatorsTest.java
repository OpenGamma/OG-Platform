/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.capm;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.analytics.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.analytics.math.statistics.descriptive.GeometricMeanCalculator;
import com.opengamma.analytics.math.statistics.descriptive.MeanCalculator;
import com.opengamma.analytics.math.statistics.descriptive.SampleCovarianceCalculator;
import com.opengamma.analytics.math.statistics.descriptive.SampleVarianceCalculator;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.instant.ImmutableInstantDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CAPMCalculatorsTest {
  private static final double HIGH_BETA = 2;
  private static final double LOW_BETA = -0.02;
  private static final double RISK_FREE = 0.04;
  private static final DoubleTimeSeries<?> RISK_FREE_RETURN;
  private static final DoubleTimeSeries<?> MARKET_RETURN;
  private static final DoubleTimeSeries<?> HIGH_BETA_RETURN;
  private static final DoubleTimeSeries<?> LOW_BETA_RETURN;
  private static final double EPS = 1e-12;
  private static final DoubleTimeSeriesStatisticsCalculator COVARIANCE = new DoubleTimeSeriesStatisticsCalculator(new SampleCovarianceCalculator());
  private static final DoubleTimeSeriesStatisticsCalculator VARIANCE = new DoubleTimeSeriesStatisticsCalculator(new SampleVarianceCalculator());
  private static final CAPMBetaCalculator BETA = new CAPMBetaCalculator(COVARIANCE, VARIANCE);
  private static final DoubleTimeSeriesStatisticsCalculator GEOMETRIC_MEAN = new DoubleTimeSeriesStatisticsCalculator(new GeometricMeanCalculator());
  private static final DoubleTimeSeriesStatisticsCalculator ARITHMETIC_MEAN = new DoubleTimeSeriesStatisticsCalculator(new MeanCalculator());
  private static final CAPMExpectedReturnCalculator RETURN = new CAPMExpectedReturnCalculator(GEOMETRIC_MEAN, ARITHMETIC_MEAN);

  static {
    final int n = 1000;
    final long[] t = new long[n];
    final double[] r0 = new double[n];
    final double[] r1 = new double[n];
    final double[] r2 = new double[n];
    final double[] r3 = new double[n];
    for (int i = 0; i < n; i++) {
      t[i] = i;
      r0[i] = Math.random() - 0.5;
      r1[i] = RISK_FREE;
      r2[i] = r0[i] * HIGH_BETA;
      r3[i] = r0[i] * LOW_BETA;
    }
    RISK_FREE_RETURN = ImmutableInstantDoubleTimeSeries.of(t, r1);
    MARKET_RETURN = ImmutableInstantDoubleTimeSeries.of(t, r0);
    HIGH_BETA_RETURN = ImmutableInstantDoubleTimeSeries.of(t, r2);
    LOW_BETA_RETURN = ImmutableInstantDoubleTimeSeries.of(t, r3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCovarianceCalculator() {
    new CAPMBetaCalculator(null, VARIANCE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVarianceCalculator() {
    new CAPMBetaCalculator(COVARIANCE, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExpectedMarketReturnCalculator() {
    new CAPMExpectedReturnCalculator(null, ARITHMETIC_MEAN);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExpectedRiskFreeRateCalculator() {
    new CAPMExpectedReturnCalculator(GEOMETRIC_MEAN, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTSArray() {
    BETA.evaluate((DoubleTimeSeries<?>[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLength() {
    BETA.evaluate(MARKET_RETURN);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testShortTS() {
    BETA.evaluate(ImmutableInstantDoubleTimeSeries.of(new long[] {1}, new double[] {1}),
        ImmutableInstantDoubleTimeSeries.of(new long[] {1}, new double[] {1}));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMismatchingDates() {
    final Instant[] t = (Instant[]) MARKET_RETURN.timesArray();
    t[1] = t[1].plusMillis(100);
    final DoubleTimeSeries<?> ts = ImmutableInstantDoubleTimeSeries.of(t, MARKET_RETURN.valuesArrayFast());
    BETA.evaluate(MARKET_RETURN, ts);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMarketPriceTS() {
    RETURN.evaluate(null, RISK_FREE_RETURN, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRiskFreeTS() {
    RETURN.evaluate(MARKET_RETURN, null, 0);
  }

  @Test
  public void testBetas() {
    assertEquals(BETA.evaluate(MARKET_RETURN, MARKET_RETURN), 1, EPS);
    assertEquals(BETA.evaluate(RISK_FREE_RETURN, MARKET_RETURN), 0, EPS);
    assertEquals(BETA.evaluate(HIGH_BETA_RETURN, MARKET_RETURN), HIGH_BETA, EPS);
    assertEquals(BETA.evaluate(LOW_BETA_RETURN, MARKET_RETURN), LOW_BETA, EPS);
  }

  @Test
  public void testExpectedReturns() {
    assertEquals(RETURN.evaluate(MARKET_RETURN, RISK_FREE_RETURN, 1), 0, EPS);
    assertEquals(RETURN.evaluate(MARKET_RETURN, RISK_FREE_RETURN, HIGH_BETA), (1 - HIGH_BETA) * RISK_FREE, EPS);
    assertEquals(RETURN.evaluate(MARKET_RETURN, RISK_FREE_RETURN, LOW_BETA), (1 - LOW_BETA) * RISK_FREE, EPS);
  }
}
