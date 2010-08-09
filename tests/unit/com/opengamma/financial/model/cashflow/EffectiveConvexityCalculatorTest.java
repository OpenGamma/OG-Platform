/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.cashflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.financial.interestrate.annuity.definition.FixedAnnuity;

/**
 * 
 */
public class EffectiveConvexityCalculatorTest {
  private static final EffectiveConvexityCalculator ECC = new EffectiveConvexityCalculator();
  private static final String CURVE_NAME = "Test Curve";

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyAnnuity() {
    ECC.calculate(null, 1.0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testZeroPrice() {
    int n = 10;
    double[] paymentTimes = new double[n];
    double[] paymentAmounts = new double[n];
    double tau = 0.5;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
      paymentAmounts[i] = 1.0;
    }

    FixedAnnuity annuity = new FixedAnnuity(paymentTimes, paymentAmounts, CURVE_NAME);
    ECC.calculate(annuity, 0.0);
  }

  @Test
  public void testSinglePayment() {
    int n = 10;
    double[] paymentTimes = new double[n];
    double[] paymentAmounts = new double[n];
    double tau = 0.5;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
    }
    paymentAmounts[n - 1] = 1.0;

    FixedAnnuity annuity = new FixedAnnuity(paymentTimes, paymentAmounts, CURVE_NAME);
    double convexity = ECC.calculate(annuity, 0.889);
    assertEquals(n * n * tau * tau, convexity, 1e-7);
  }

  @Test
  public void testPriceSensitivity() {
    int n = 10;
    double[] paymentTimes = new double[n];
    double[] paymentAmounts = new double[n];
    double tau = 0.5;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
      paymentAmounts[i] = 1.0 + i / 5.0;
    }
    FixedAnnuity annuity = new FixedAnnuity(paymentTimes, paymentAmounts, CURVE_NAME);
    double convexity1 = ECC.calculate(annuity, 0.889);
    double convexity2 = ECC.calculate(annuity, 0.789);
    assertTrue(convexity1 > convexity2);
  }

}
