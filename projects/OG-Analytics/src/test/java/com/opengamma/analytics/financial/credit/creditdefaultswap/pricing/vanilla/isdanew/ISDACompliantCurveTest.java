/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

/**
 * 
 */
public class ISDACompliantCurveTest {
  private static final double EPS = 1e-5;

  @Test
  public void getRTTest() {
    final double[] t = new double[] {0.1, 0.2, 0.5, 0.7, 1.0, 2.0, 3.0, 3.4, 10.0 };
    final double[] r = new double[] {1.0, 0.8, 0.7, 1.2, 1.2, 1.3, 1.2, 1.0, 0.9 };
    final int n = t.length;
    final double[] rt = new double[n];
    for (int i = 0; i < n; i++) {

      rt[i] = r[i] * t[i];
    }
    final ISDACompliantCurve curve = new ISDACompliantCurve(t, r);

    double rTCalculatedi = 0.0;
    double ti = 0.0;
    final int iterationMax = 1000000;
    for (int i = 0; i < iterationMax; i++) {
      ti = ti + i / iterationMax * 100;
      if (ti <= t[0]) {

        rTCalculatedi = ti * r[0];
      }

      else if (ti >= t[t.length - 1]) {
        rTCalculatedi = ti * r[t.length - 1];
      } else {
        int indexpointi = Arrays.binarySearch(t, ti);
        if (indexpointi >= 0) {
          rTCalculatedi = t[indexpointi] * r[indexpointi];
        } else {
          indexpointi = -(1 + indexpointi);
          if (indexpointi == 0) {
            rTCalculatedi = ti * r[0];
          } else if (indexpointi == n) {
            rTCalculatedi = ti * r[n - 1];
          } else {
            final double t1 = t[indexpointi - 1];
            final double t2 = t[indexpointi];
            final double dt = t2 - t1;
            rTCalculatedi = ((t2 - ti) * r[indexpointi - 1] * t[indexpointi - 1] + (ti - t1) * r[indexpointi] * t[indexpointi]) / dt;
          }
        }
      }
      final double rTexpectedi = curve.getRT(ti);
      assertEquals("Time: " + ti, rTexpectedi, rTCalculatedi, 1e-10);
    }
  }

  @Test
  public void getNodeSensitivity() {

    final double[] t = new double[] {0.1, 0.2, 0.5, 0.7, 1.0, 2.0, 3.0, 3.4, 10.0 };
    final double[] r = new double[] {1.0, 0.8, 0.7, 1.2, 1.2, 1.3, 1.2, 1.0, 0.9 };
    final int n = t.length;
    final double[] rt = new double[n];
    for (int i = 0; i < n; i++) {

      rt[i] = r[i] * t[i];
    }
    final ISDACompliantCurve curve = new ISDACompliantCurve(t, r);

    final double[] rTCalculatedi = new double[n];
    double ti = 0.0;
    final int iterationMax = 1000000;
    for (int i = 0; i < iterationMax; i++) {

      ti = ti + i / iterationMax * 100;
      if (ti <= t[0]) {

        rTCalculatedi[0] = 1.0;
      }

      else if (ti >= t[t.length - 1]) {
        rTCalculatedi[t.length - 1] = 1.0;
      } else {
        int indexpointi = Arrays.binarySearch(t, ti);
        if (indexpointi >= 0) {
          rTCalculatedi[indexpointi] = 1.0;
        } else {
          indexpointi = -(1 + indexpointi);
          if (indexpointi == 0) {
            rTCalculatedi[0] = 1.0;
          } else if (indexpointi == n) {
            rTCalculatedi[n - 1] = 1.0;
          } else {
            final double t1 = t[indexpointi - 1];
            final double t2 = t[indexpointi];
            final double dt = t2 - t1;
            rTCalculatedi[indexpointi - 1] = t1 * (t2 - ti) / dt / ti;
            rTCalculatedi[indexpointi] = t2 * (ti - t1) / dt / ti;
          }
        }
      }
      for (int j = 0; j < n; j++) {
        final double[] rTexpectedi = curve.getNodeSensitivity(ti);
        assertEquals("Time: " + ti, rTexpectedi[j], rTCalculatedi[j], 1e-10);
      }

    }
  }

  @Test
  public void getNodeSensitivityvsFiniteDifferenceTest() {
    final double[] t = new double[] {0.1, 0.2, 0.5, 0.7, 1.0, 2.0, 3.0, 3.4, 10.0 };
    final double[] r = new double[] {1.0, 0.8, 0.7, 1.2, 1.2, 1.3, 1.2, 1.0, 0.9 };
    final int n = t.length;
    ISDACompliantCurve curve = new ISDACompliantCurve(t, r);

    double ti = 0.001;
    final int iterationMax = 10000;
    for (int i = 1; i < iterationMax; i++) {
      ti = ti + i / iterationMax * 100;
      final double[] sensi = curve.getNodeSensitivity(ti);
      for (int j = 0; j < n; j++) {
        r[j] = r[j] + 10e-6;
        curve = new ISDACompliantCurve(t, r);
        double sensii = curve.getRT(ti) / ti;
        r[j] = r[j] - 2 * 10e-6;
        curve = new ISDACompliantCurve(t, r);
        sensii = sensii - curve.getRT(ti) / ti;
        sensii = sensii / (2 * 10e-6);
        r[j] = r[j] + 10e-6;
        curve = new ISDACompliantCurve(t, r);
        assertEquals("node: " + j, sensi[j], sensii, 1e-5);
      }
    }
  }

  @Test
  public void getSingleNodeSensitivityvsNodesensitivityTest() {
    final double[] t = new double[] {0.1, 0.2, 0.5, 0.7, 1.0, 2.0, 3.0, 3.4, 10.0 };
    final double[] r = new double[] {1.0, 0.8, 0.7, 1.2, 1.2, 1.3, 1.2, 1.0, 0.9 };
    final ISDACompliantCurve curve = new ISDACompliantCurve(t, r);
    double ti = 0.001;
    final int iterationMax = 1000000;
    double sensitivityExpectedi = 0.0;
    double sensitivityCalculatedi = 0.0;

    for (int i = 0; i < iterationMax; i++) {
      ti = ti + i / iterationMax * 100;

      for (int j = 0; j < t.length; j++) {
        sensitivityExpectedi = curve.getSingleNodeSensitivity(ti, j);
        sensitivityCalculatedi = curve.getNodeSensitivity(ti)[j];
        assertEquals("Time: " + ti, sensitivityExpectedi, sensitivityCalculatedi, EPS);
      }
    }
  }

  @Test
  public void getSingleNodeSensitivityvsSingleNodeDiscountFactorsensitivityTest() {
    final double[] t = new double[] {0.1, 0.2, 0.5, 0.7, 1.0, 2.0, 3.0, 3.4, 10.0 };
    final double[] r = new double[] {1.0, 0.8, 0.7, 1.2, 1.2, 1.3, 1.2, 1.0, 0.9 };
    final ISDACompliantCurve curve = new ISDACompliantCurve(t, r);
    double ti = 0.001;
    final int iterationMax = 10000;
    double sensitivityExpectedi = 0.0;
    double sensitivityCalculatedi = 0.0;

    for (int i = 0; i < iterationMax; i++) {
      ti = ti + i / iterationMax * 100;

      for (int j = 0; j < t.length; j++) {
        sensitivityExpectedi = curve.getSingleNodeSensitivity(ti, j);
        sensitivityExpectedi = -ti * sensitivityExpectedi * Math.exp(-curve.getRT(ti));
        sensitivityCalculatedi = curve.getSingleNodeDiscountFactorSensitivity(ti, j);
        assertEquals("Time: " + ti, sensitivityExpectedi, sensitivityCalculatedi, EPS);
      }
    }

  }

  @Test
  public void withRatesTest() {
    final double[] t = new double[] {0.1, 0.2, 0.5, 0.7, 1.0, 2.0, 3.0, 3.4, 10.0 };
    final double[] r1 = new double[] {1.0, 0.8, 0.7, 1.2, 1.2, 1.3, 1.2, 1.0, 0.9 };
    final double[] r2 = new double[] {1.0, 0.8, 0.7, 1.2, 1.2, 1.3, 1.2, 1.0, 0.9 };
    final ISDACompliantCurve curve = new ISDACompliantCurve(t, r1);
    curve.withRates(r2);
    for (int i = 0; i < t.length; i++) {
      assertEquals("Node: " + i, curve.getZeroRate(t[i]), r2[i], EPS);
      assertEquals("Node: " + i, curve.getRT(t[i]), r2[i] * t[i], EPS);
      assertEquals("Node: " + i, curve.getDiscountFactor(t[i]), Math.exp(-r2[i] * t[i]), EPS);
    }
  }

  @Test
  public void withRateTest() {
    final double[] t = new double[] {0.1, 0.2, 0.5, 0.7, 1.0, 2.0, 3.0, 3.4, 10.0 };
    final double[] r1 = new double[] {1.0, 0.8, 0.7, 1.2, 1.2, 1.3, 1.2, 1.0, 0.9 };
    final double[] r2 = new double[] {1.0, 0.8, 0.7, 1.2, 1.2, 3.0, 1.2, 1.0, 0.9 };
    final ISDACompliantCurve curve = new ISDACompliantCurve(t, r1);
    final ISDACompliantCurve newcurve = curve.withRate(3.0, 5);
    for (int i = 0; i < t.length; i++) {
      assertEquals("Node: " + i, newcurve.getZeroRate(t[i]), r2[i], EPS);
      assertEquals("Node: " + i, newcurve.getRT(t[i]), r2[i] * t[i], EPS);
      assertEquals("Node: " + i, newcurve.getDiscountFactor(t[i]), Math.exp(-r2[i] * t[i]), EPS);
    }

  }

  @Test
  public void withDiscountFactorTest() {
    final double[] t = new double[] {0.1, 0.2, 0.5, 0.7, 1.0, 2.0, 3.0, 3.4, 10.0 };
    final double[] r1 = new double[] {1.0, 0.8, 0.7, 1.2, 1.2, 1.3, 1.2, 1.0, 0.9 };
    final double[] r2 = new double[] {1.0, 0.8, 0.7, 1.2, 1.2, -Math.log(0.6) / t[5], 1.2, 1.0, 0.9 };
    final ISDACompliantCurve curve = new ISDACompliantCurve(t, r1);
    final ISDACompliantCurve newcurve = curve.withDiscountFactor(.6, 5);
    for (int i = 0; i < t.length; i++) {
      assertEquals("Node: " + i, newcurve.getZeroRate(t[i]), r2[i], EPS);
      assertEquals("Node: " + i, newcurve.getRT(t[i]), r2[i] * t[i], EPS);
      assertEquals("Node: " + i, newcurve.getDiscountFactor(t[i]), Math.exp(-r2[i] * t[i]), EPS);
    }

  }

  @Test
  public void getZeroRateTest() {

    final double[] t = new double[] {0.1, 0.2, 0.5, 0.7, 1.0, 2.0, 3.0, 3.4, 10.0 };
    final double[] r = new double[] {1.0, 0.8, 0.7, 1.2, 1.2, 1.3, 1.2, 1.0, 0.9 };
    final ISDACompliantCurve curve = new ISDACompliantCurve(t, r);
    double ti = 0.001;
    final int iterationMax = 1000000;
    for (int i = 0; i < iterationMax; i++) {
      ti = ti + i / iterationMax * 100;
      assertEquals("Time: " + ti, curve.getZeroRate(ti), curve.getRT(ti) / ti, EPS);
    }
  }

  @Test
  public void getNumberOfKnotsTest() {
    final double[] t = new double[] {0.1, 0.2, 0.5, 0.7, 1.0, 2.0, 3.0, 3.4, 10.0 };
    final double[] r = new double[] {1.0, 0.8, 0.7, 1.2, 1.2, 1.3, 1.2, 1.0, 0.9 };
    final ISDACompliantCurve curve = new ISDACompliantCurve(t, r);
    assertEquals("length", curve.getNumberOfKnots(), t.length, EPS);
  }

  @Test
  public void offsetTest() {
    final double[] timesFromBase = new double[] {0.1, 0.2, 0.5, 0.7, 1.0, 2.0, 3.0, 3.4, 10.0 };
    final double[] r = new double[] {0.04, 0.08, 0.07, 0.12, 0.12, 0.13, 0.12, 0.1, 0.09 };

    final double offset = -0.04;

    final ISDACompliantCurve c1 = new ISDACompliantCurve(timesFromBase, r);
    final ISDACompliantCurve c2 = new ISDACompliantCurve(timesFromBase, r, offset);

    final double rtb = offset * r[0];
    final double pb = Math.exp(rtb);

    final int steps = 1;// 1001;
    for (int i = 0; i < steps; i++) {
      final double time = 9.96;// (12.0 * i) / (steps - 1);
      final double p1 = c1.getDiscountFactor(time - offset) / pb;
      final double p2 = c2.getDiscountFactor(time);

      final double rt1 = c1.getRT(time - offset) + rtb;
      final double rt2 = c2.getRT(time);

      assertEquals("discount " + time, p1, p2, 1e-15);
      assertEquals("rt " + time, rt1, rt2, 1e-15);
      if (time > 0.0) {
        final double r1 = rt1 / time;
        final double r2 = c2.getZeroRate(time);
        assertEquals("r " + time, r1, r2, 1e-15);
      }
    }

  }

  @Test
  public void senseTest() {
    final double[] t = new double[] {0.1, 0.2, 0.5, 0.7, 1.0, 2.0, 3.0, 3.4, 10.0 };
    final double[] r = new double[] {1.0, 0.8, 0.7, 1.2, 1.2, 1.3, 1.2, 1.0, 0.9 };
    final ISDACompliantCurve curve = new ISDACompliantCurve(t, r);

    final int n = curve.getNumberOfKnots();
    final int nExamples = 200;
    for (int jj = 0; jj < nExamples; jj++) {
      final double time = jj * 11.0 / (nExamples - 1);
      final double[] fd = fdSense(curve, time);
      final double[] anal = curve.getNodeSensitivity(time);
      for (int i = 0; i < n; i++) {
        final double anal2 = curve.getSingleNodeSensitivity(time, i);
        assertEquals("test1 - Time: " + time, fd[i], anal[i], 1e-10);
        assertEquals("test2 - Time: " + time, anal[i], anal2, 0.0);
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

  @Test
  public void discountFactorSenseTest() {
    final double[] t = new double[] {0.1, 0.2, 0.5, 0.7, 1.0, 2.0, 3.0, 3.4, 10.0 };
    final double[] r = new double[] {1.0, 0.8, 0.7, 1.2, 1.2, 1.3, 1.2, 1.0, 0.9 };
    final ISDACompliantCurve curve = new ISDACompliantCurve(t, r);

    final int n = curve.getNumberOfKnots();
    final int nExamples = 200;
    for (int jj = 0; jj < nExamples; jj++) {
      final double time = jj * 11.0 / (nExamples - 1);
      final double[] fd = fdDiscountFactorSense(curve, time);

      for (int i = 0; i < n; i++) {
        final double anal = curve.getSingleNodeDiscountFactorSensitivity(time, i);
        assertEquals("Time: " + time, fd[i], anal, 1e-10);
      }
    }

    // // check nodes
    // for (int jj = 0; jj < n; jj++) {
    // final double[] anal = curve.getNodeSensitivity(t[jj]);
    // for (int i = 0; i < n; i++) {
    // final double anal2 = curve.getSingleNodeSensitivity(t[jj], i);
    // final double expected = i == jj ? 1.0 : 0.0;
    // assertEquals(expected, anal[i], 0.0);
    // assertEquals(expected, anal2, 0.0);
    // }
    // }

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

  private double[] fdDiscountFactorSense(final ISDACompliantCurve curve, final double t) {
    final int n = curve.getNumberOfKnots();
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      final double r = curve.getZeroRateAtIndex(i);
      final ISDACompliantCurve curveUp = curve.withRate(r + EPS, i);
      final ISDACompliantCurve curveDown = curve.withRate(r - EPS, i);
      final double up = curveUp.getDiscountFactor(t);
      final double down = curveDown.getDiscountFactor(t);
      res[i] = (up - down) / 2 / EPS;
    }
    return res;
  }

}
