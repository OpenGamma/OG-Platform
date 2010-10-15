/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.opengamma.financial.interestrate.bond.definition.Bond;

/**
 * 
 */
public class ConvexityCalculatorTest {
  private static final ConvexityCalculator CC = new ConvexityCalculator();
  private static final String CURVE_NAME = "Test Curve";

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyBond() {
    CC.calculate(null, 1.0);
  }

  @Test
  public void testSinglePayment() {
    int n = 10;
    double[] paymentTimes = new double[n];
    double[] coupons = new double[n];
    double[] yearFractions = new double[n];
    Arrays.fill(yearFractions, 1.0);
    double tau = 0.5;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
    }
    coupons[n - 1] = 2.0;

    Bond bond = new Bond(paymentTimes, coupons, yearFractions, 0.0, CURVE_NAME);
    double convexity = CC.calculate(bond, 0.889);
    assertEquals(n * n * tau * tau, convexity, 1e-7);
  }

  @Test
  public void testPriceSensitivity() {
    int n = 10;
    double[] paymentTimes = new double[n];
    double[] coupons = new double[n];
    double[] yearFractions = new double[n];
    Arrays.fill(yearFractions, 1.0);
    double tau = 0.5;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
      coupons[i] = 1.0 + i / 5.0;
    }
    Bond bond = new Bond(paymentTimes, coupons, yearFractions, 0.0, CURVE_NAME);
    double convexity1 = CC.calculate(bond, 0.889);
    double convexity2 = CC.calculate(bond, 0.789);
    assertTrue(convexity1 > convexity2);
  }

}
