/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

/**
 * 
 */
public class ISDACompliantCurveTest {
  private static final double EPS = 1e-5;

  @Test
  public void senseTest() {
    double[] t = new double[] {0.1, 0.2, 0.5, 0.7, 1.0, 2.0, 3.0, 3.4, 10.0};
    double[] r = new double[] {1.0, 0.8, 0.7, 1.2, 1.2, 1.3, 1.2, 1.0, 0.9};
    ISDACompliantCurve curve = new ISDACompliantCurve(t, r);

    final int n = curve.getNumberOfKnots();
    final int nExamples = 200;
    for (int jj = 0; jj < nExamples; jj++) {
      final double time = jj * 11.0 / (nExamples - 1);
      final double[] fd = fdSense(curve, time);
      final double[] anal = curve.getNodeSensitivity(time);
      for (int i = 0; i < n; i++) {
        final double anal2 = curve.getSingleNodeSensitivity(time, i);
        assertEquals("Time: " + time, fd[i], anal[i], 1e-10);
        assertEquals("Time: " + time, anal[i], anal2, 0.0);
      }
    }

    // check nodes
    for (int jj = 0; jj < n; jj++) {
      final double[] anal = curve.getNodeSensitivity(t[jj]);
      for (int i = 0; i < n; i++) {
        final double anal2 = curve.getSingleNodeSensitivity(t[jj], i);
        final double expected = i == jj ? 1.0 : 0.0;
        assertEquals(expected, anal[i], 0.0);
        assertEquals(expected, anal2, 0.0);
      }
    }

  }

  private double[] fdSense(final ISDACompliantCurve curve, final double t) {
    final int n = curve.getNumberOfKnots();
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      final double r = curve.getZeroRateAtIndex(i);
      final ISDACompliantCurve curveUp = curve.withRate(r + EPS, i);
      final ISDACompliantCurve curveDown = curve.withRate(r - EPS, i);
      final double up = curveUp.getZeroRate(t);
      final double down = curveDown.getZeroRate(t);
      res[i] = (up - down) / 2 / EPS;
    }
    return res;
  }

}
