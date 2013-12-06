/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.capm;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.regression.LeastSquaresRegressionResult;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.instant.ImmutableInstantDoubleTimeSeries;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CAPMFromRegressionCalculatorTest {
  private static final DoubleTimeSeries<?> MARKET;
  private static final DoubleTimeSeries<?> ASSET_1;
  private static final DoubleTimeSeries<?> ASSET_2;
  private static final DoubleTimeSeries<?> ASSET_3;
  private static final DoubleTimeSeries<?> ASSET_4;
  private static final DoubleTimeSeries<?> ASSET_5;
  private static final double BETA1 = 2;
  private static final double BETA2 = -0.5;
  private static final double ALPHA1 = 0.05;
  private static final double ALPHA2 = -0.3;
  private static final double RF = 0.05;
  private static final double EPS = 1e-12;
  private static final Function<DoubleTimeSeries<?>, LeastSquaresRegressionResult> CAPM = new CAPMFromRegressionCalculator();

  static {
    final int n = 1000;
    final long[] t = new long[n];
    final double[] market = new double[n];
    final double[] asset1 = new double[n];
    final double[] asset2 = new double[n];
    final double[] asset3 = new double[n];
    final double[] asset4 = new double[n];
    final double[] asset5 = new double[n];
    for (int i = 0; i < n; i++) {
      market[i] = Math.random() - 0.5;
      asset1[i] = market[i] * BETA1;
      asset2[i] = market[i] * BETA2;
      asset3[i] = RF;
      asset4[i] = asset1[i] + ALPHA1;
      asset5[i] = asset2[i] + ALPHA2;
    }
    MARKET = ImmutableInstantDoubleTimeSeries.of(t, market);
    ASSET_1 = ImmutableInstantDoubleTimeSeries.of(t, asset1);
    ASSET_2 = ImmutableInstantDoubleTimeSeries.of(t, asset2);
    ASSET_3 = ImmutableInstantDoubleTimeSeries.of(t, asset3);
    ASSET_4 = ImmutableInstantDoubleTimeSeries.of(t, asset4);
    ASSET_5 = ImmutableInstantDoubleTimeSeries.of(t, asset5);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    CAPM.evaluate((DoubleTimeSeries<?>[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNoMarketData() {
    CAPM.evaluate(ASSET_1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInsufficientData() {
    CAPM.evaluate(ImmutableInstantDoubleTimeSeries.EMPTY_SERIES, ImmutableInstantDoubleTimeSeries.EMPTY_SERIES);
  }

  @Test
  public void testBetas() {
    double[] betas = CAPM.evaluate(MARKET, MARKET).getBetas();
    assertEquals(betas[0], 0, EPS);
    assertEquals(betas[1], 1, EPS);
    betas = CAPM.evaluate(ASSET_1, MARKET).getBetas();
    assertEquals(betas[0], 0, EPS);
    assertEquals(betas[1], BETA1, EPS);
    betas = CAPM.evaluate(ASSET_2, MARKET).getBetas();
    assertEquals(betas[0], 0, EPS);
    assertEquals(betas[1], BETA2, EPS);
    betas = CAPM.evaluate(ASSET_3, MARKET).getBetas();
    assertEquals(betas[0], RF, EPS);
    assertEquals(betas[1], 0, EPS);
    betas = CAPM.evaluate(ASSET_4, MARKET).getBetas();
    assertEquals(betas[0], ALPHA1, EPS);
    assertEquals(betas[1], BETA1, EPS);
    betas = CAPM.evaluate(ASSET_5, MARKET).getBetas();
    assertEquals(betas[0], ALPHA2, EPS);
    assertEquals(betas[1], BETA2, EPS);
  }
}
