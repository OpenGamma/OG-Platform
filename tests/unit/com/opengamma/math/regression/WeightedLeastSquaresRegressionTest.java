/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.regression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class WeightedLeastSquaresRegressionTest {
  private static final double EPS = 1e-6;

  @Test
  public void test() {
    final Double a0 = 2.3;
    final Double a1 = -4.5;
    final Double a2 = 0.76;
    final Double a3 = 3.4;
    final int n = 30;
    final Double[][] x = new Double[n][3];
    final Double[] yIntercept = new Double[n];
    final Double[] yNoIntercept = new Double[n];
    final Double[][] w1 = new Double[n][n];
    final Double[] w2 = new Double[n];
    Double y, x1, x2, x3;
    for (int i = 0; i < n; i++) {
      x1 = (double) i;
      x2 = x1 * x1;
      x3 = Math.sqrt(x1);
      x[i] = new Double[] { x1, x2, x3 };
      y = x1 * a1 + x2 * a2 + x3 * a3;
      yNoIntercept[i] = y;
      yIntercept[i] = y + a0;
      for (int j = 0; j < n; j++) {
        w1[i][j] = Math.random();
      }
      w1[i][i] = 1.;
      w2[i] = 1.;
    }
    final WeightedLeastSquaresRegression wlsRegression = new WeightedLeastSquaresRegression();
    final OrdinaryLeastSquaresRegression olsRegression = new OrdinaryLeastSquaresRegression();
    try {
      wlsRegression.regress(x, (Double[]) null, yNoIntercept, false);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    LeastSquaresRegressionResult wls = wlsRegression.regress(x, w1, yIntercept, true);
    LeastSquaresRegressionResult ols = olsRegression.regress(x, yIntercept, true);
    testRegressions(n, 4, wls, ols);
    wls = wlsRegression.regress(x, w1, yNoIntercept, false);
    ols = olsRegression.regress(x, yNoIntercept, false);
    testRegressions(n, 3, wls, ols);
    wls = wlsRegression.regress(x, w2, yIntercept, true);
    ols = olsRegression.regress(x, yIntercept, true);
    testRegressions(n, 4, wls, ols);
    wls = wlsRegression.regress(x, w2, yNoIntercept, false);
    ols = olsRegression.regress(x, yNoIntercept, false);
    testRegressions(n, 3, wls, ols);
  }

  private void testRegressions(final int n, final int k, final LeastSquaresRegressionResult regression1, final LeastSquaresRegressionResult regression2) {
    final Double[] r1 = regression1.getResiduals();
    final Double[] r2 = regression2.getResiduals();
    for (int i = 0; i < n; i++) {
      assertEquals(r1[i], r2[i], EPS);
    }
    final Double[] b1 = regression1.getBetas();
    final Double[] t1 = regression1.getTStatistics();
    final Double[] p1 = regression1.getPValues();
    final Double[] s1 = regression1.getStandardErrorOfBetas();
    final Double[] b2 = regression2.getBetas();
    final Double[] t2 = regression2.getTStatistics();
    final Double[] p2 = regression2.getPValues();
    final Double[] s2 = regression2.getStandardErrorOfBetas();
    for (int i = 0; i < k; i++) {
      assertEquals(b1[i], b2[i], EPS);
      assertEquals(t1[i], t2[i], EPS);
      assertEquals(p1[i], p2[i], EPS);
      assertEquals(s1[i], s2[i], EPS);
    }
    assertEquals(regression1.getRSquared(), regression2.getRSquared(), EPS);
    assertEquals(regression1.getAdjustedRSquared(), regression2.getAdjustedRSquared(), EPS);
    assertEquals(regression1.getMeanSquareError(), regression2.getMeanSquareError(), EPS);
  }
}
