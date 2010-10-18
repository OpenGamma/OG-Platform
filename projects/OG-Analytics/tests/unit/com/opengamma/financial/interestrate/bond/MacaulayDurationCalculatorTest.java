/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.financial.interestrate.bond.definition.Bond;

/**
 * 
 */
public class MacaulayDurationCalculatorTest {
  private static final MacaulayDurationCalculator MDC = new MacaulayDurationCalculator();
  private static final String CURVE_NAME = "Test Curve";

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyBond() {
    MDC.calculate(null, 1.0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testZeroPrice() {
    int n = 10;
    double[] paymentTimes = new double[n];
    double tau = 0.5;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
    }

    Bond bond = new Bond(paymentTimes, 0.0, CURVE_NAME);
    MDC.calculate(bond, 0.0);
  }

  @Test
  public void testSinglePayment() {
    int n = 10;
    double[] paymentTimes = new double[n];
    double tau = 0.5;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
    }

    Bond bond = new Bond(paymentTimes, 0.0, CURVE_NAME);
    double duration = MDC.calculate(bond, 0.889);
    assertEquals(n * tau, duration, 1e-8);
  }

  @Test
  public void testMultiplePayment() {
    int n = 10;
    double[] paymentTimes = new double[n];
    double tau = 0.5;
    double alpha = 0.48;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
    }

    double yield = 0.05;
    double coupon = (Math.exp(yield * tau) - 1) / alpha;
    Bond bond = new Bond(paymentTimes, coupon, alpha, 0.0, CURVE_NAME);
    double duration = MDC.calculate(bond, 1.0);

    double sum = 0.0;
    double t = 0;
    for (int i = 0; i < n; i++) {
      t = paymentTimes[i];
      sum += t * Math.exp(-yield * t) * (coupon * alpha + (i == (n - 1) ? 1.0 : 0.0));
    }
    assertEquals(sum, duration, 1e-8);
  }

  @Test
  public void testPriceSensitivity() {
    int n = 10;
    double[] paymentTimes = new double[n];
    double tau = 0.5;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
    }

    Bond bond = new Bond(paymentTimes, 0.05, CURVE_NAME);
    double duration1 = MDC.calculate(bond, 0.889);
    double duration2 = MDC.calculate(bond, 0.789);
    assertTrue(duration1 > duration2);
  }
}
