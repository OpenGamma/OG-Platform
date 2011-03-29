/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import com.opengamma.financial.interestrate.bond.definition.Bond;

/**
 * 
 */
public class ModifiedDurationCalculatorTest {
  private static final ModifiedDurationCalculator MDC = new ModifiedDurationCalculator();
  private static final String CURVE_NAME = "Test Curve";

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBond() {
    MDC.calculate(null, 1.0, 2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeDirtyPrice() {
    MDC.calculate(new Bond(new double[] {1, 2, 3}, 0.02, CURVE_NAME), -100, 4);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeCompoundingFrequency() {
    MDC.calculate(new Bond(new double[] {1, 2, 3}, 0.02, CURVE_NAME), 100, -3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroPrice() {
    final int n = 10;
    final double[] paymentTimes = new double[n];
    final double tau = 0.5;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
    }
    final Bond bond = new Bond(paymentTimes, 0.0, CURVE_NAME);
    MDC.calculate(bond, 0.0, 2);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroCompondFreq() {
    final int n = 10;
    final double[] paymentTimes = new double[n];
    final double tau = 0.5;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
    }

    final Bond bond = new Bond(paymentTimes, 0.0, CURVE_NAME);
    MDC.calculate(bond, 0.956, 0);
  }

  @Test
  public void testSinglePayment() {
    final int n = 10;
    final double[] paymentTimes = new double[n];
    final double tau = 0.5;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
    }

    final Bond bond = new Bond(paymentTimes, 0.0, CURVE_NAME);
    final double duration = MDC.calculate(bond, 1.0, 4);
    assertEquals(n * tau, duration, 1e-8);
  }

  @Test
  public void testMultiplePayment() {
    final int n = 10;
    final int m = 2;
    final double[] paymentTimes = new double[n];
    final double tau = 0.5;
    final double alpha = 0.5;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;

    }

    final double yield = 0.5;
    final double coupon = (Math.pow(1 + yield / m, m * tau) - 1) / alpha;
    final Bond bond = new Bond(paymentTimes, coupon, alpha, 0.0, CURVE_NAME);
    final double duration = MDC.calculate(bond, 1.0, m);

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
    final int n = 10;
    final double[] paymentTimes = new double[n];
    final double tau = 0.5;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
    }
    final Bond bond = new Bond(paymentTimes, 0.05, CURVE_NAME);
    final double duration1 = MDC.calculate(bond, 0.889, 2);
    final double duration2 = MDC.calculate(bond, 0.789, 2);
    assertTrue(duration1 > duration2);
  }

}
