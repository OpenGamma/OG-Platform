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
public class EffectiveDurationCalculatorTest {
  private static final EffectiveDurationCalculator EDC = new EffectiveDurationCalculator();
  private static final String CURVE_NAME = "Test Curve";

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyAnnuity() {
    EDC.calculate(null, 1.0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testZeroPrice() {
    int n = 10;
    double[] paymentTimes = new double[n];
    double[] coupons = new double[n];
    double tau = 0.5;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
      coupons[i] = 0.05;
    }

    FixedAnnuity annuity = new FixedAnnuity(paymentTimes, 1.0, coupons, CURVE_NAME);
    EDC.calculate(annuity, 0.0);
  }

  @Test
  public void testSinglePayment() {
    int n = 10;
    double[] paymentTimes = new double[n];
    double[] coupons = new double[n];
    double tau = 0.5;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
    }
    coupons[n - 1] = 3.0;

    FixedAnnuity annuity = new FixedAnnuity(paymentTimes, 1.0, coupons, CURVE_NAME);
    double duration = EDC.calculate(annuity, 0.889);
    assertEquals(n * tau, duration, 1e-8);
  }

  @Test
  public void testPriceSensitivity() {
    int n = 10;
    double[] paymentTimes = new double[n];
    double[] coupons = new double[n];
    double tau = 0.5;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
      coupons[i] = 0.03 + i / 50.0 + (i == n - 1 ? 1 / tau : 0);
    }
    FixedAnnuity annuity = new FixedAnnuity(paymentTimes, 1.0, coupons, CURVE_NAME);
    double duration1 = EDC.calculate(annuity, 0.889);
    double duration2 = EDC.calculate(annuity, 0.789);
    assertTrue(duration1 > duration2);
  }
}
