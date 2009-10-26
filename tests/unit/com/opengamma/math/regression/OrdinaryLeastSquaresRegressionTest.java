/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.regression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class OrdinaryLeastSquaresRegressionTest {
  private static final LeastSquaresRegression REGRESSION = new OrdinaryLeastSquaresRegression();
  private static final double EPS = 1e-4;
  private static final double FACTOR = 1. / EPS;

  @Test
  public void test() {
    final Double[][] x = new Double[20][5];
    final Double[] y = new Double[20];
    final double[] a = new double[] { 3.4, 1.2, -0.62, -0.44, 0.65 };
    for (int i = 0; i < 20; i++) {
      for (int j = 0; j < 5; j++) {
        x[i][j] = Math.random() + (Math.random() - 0.5) / FACTOR;
      }
      y[i] = a[0] * x[i][0] + a[1] * x[i][1] + a[2] * x[i][2] + a[3] * x[i][3] + a[4] * x[i][4] + Math.random() / FACTOR;
    }
    final LeastSquaresRegressionResult result = REGRESSION.regress(x, null, y, false);
    final Double[] beta = result.getBetas();
    final Double[] tStat = result.getTStatistics();
    final Double[] pStat = result.getPValues();
    final Double[] stdErr = result.getStandardErrorOfBetas();
    for (int i = 0; i < 5; i++) {
      assertEquals(beta[i], a[i], EPS);
      assertTrue(Math.abs(tStat[i]) > FACTOR);
      assertTrue(pStat[i] < EPS);
      assertTrue(stdErr[i] < EPS);
    }
    assertEquals(result.getRSquared(), 1, EPS);
    assertEquals(result.getAdjustedRSquared(), 1, EPS);
    final Double[] residuals = result.getResiduals();
    for (int i = 0; i < 20; i++) {
      assertEquals(y[i], a[0] * x[i][0] + a[1] * x[i][1] + a[2] * x[i][2] + a[3] * x[i][3] + a[4] * x[i][4] + residuals[i], 10 * EPS);
    }
  }
}
