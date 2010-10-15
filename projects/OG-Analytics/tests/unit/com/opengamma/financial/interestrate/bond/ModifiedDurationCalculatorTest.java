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
public class ModifiedDurationCalculatorTest {
  private static final ModifiedDurationCalculator MDC = new ModifiedDurationCalculator();
  private static final String CURVE_NAME = "Test Curve";

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyBond() {
    MDC.calculate(null, 1.0, 2);
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
    MDC.calculate(bond, 0.0, 2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testZeroCompondFreq() {
    int n = 10;
    double[] paymentTimes = new double[n];
    double tau = 0.5;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
    }

    Bond bond = new Bond(paymentTimes, 0.0, CURVE_NAME);
    MDC.calculate(bond, 0.956, 0);
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
    double duration = MDC.calculate(bond, 1.0, 4);
    assertEquals(n * tau, duration, 1e-8);
  }

  @Test
  public void testMultiplePayment() {
    int n = 10;
    int m = 2;
    double[] paymentTimes = new double[n];
    double tau = 0.5;
    double alpha = 0.5;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;

    }

    double yield = 0.5;
    double coupon = (Math.pow(1 + yield / m, m * tau) - 1) / alpha;
    Bond bond = new Bond(paymentTimes, coupon, alpha, 0.0, CURVE_NAME);
    double duration = MDC.calculate(bond, 1.0, m);

    double sum = 0.0;
    double t = 0;
    for (int i = 0; i < n; i++) {
      t = paymentTimes[i];
      sum += t * Math.pow(1 + yield / m, -m * t) * (coupon * alpha + (i == (n - 1) ? 1.0 : 0.0));
    }
    sum /= (1 + yield / m);
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
    double duration1 = MDC.calculate(bond, 0.889, 2);
    double duration2 = MDC.calculate(bond, 0.789, 2);
    assertTrue(duration1 > duration2);
  }

}
