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

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.ConstantDoublesCurve;

/**
 * 
 */
public class BondConvexityCalculatorTest {
  private static final BondConvexityCalculator CC = new BondConvexityCalculator();
  private static final String CURVE_NAME = "Test Curve";

  @Test(expected = IllegalArgumentException.class)
  public void testNullBond() {
    CC.calculate(null, 1.0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativePrice() {
    CC.calculate(new Bond(new double[] {1, 2, 3}, 0.04, CURVE_NAME), -102);
  }

  @Test
  public void testSinglePayment() {
    final int n = 10;
    final double[] paymentTimes = new double[n];
    final double[] coupons = new double[n];
    final double[] yearFractions = new double[n];
    Arrays.fill(yearFractions, 1.0);
    final double tau = 0.5;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
    }
    coupons[n - 1] = 2.0;
    final Bond bond = new Bond(paymentTimes, coupons, yearFractions, 0.0, CURVE_NAME);
    final double convexity = CC.calculate(bond, 0.889);
    assertEquals(n * n * tau * tau, convexity, 1e-7);
  }

  @Test
  public void testPriceSensitivity() {
    final int n = 10;
    final double[] paymentTimes = new double[n];
    final double[] coupons = new double[n];
    final double[] yearFractions = new double[n];
    Arrays.fill(yearFractions, 1.0);
    final double tau = 0.5;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
      coupons[i] = 1.0 + i / 5.0;
    }
    final Bond bond = new Bond(paymentTimes, coupons, yearFractions, 0.0, CURVE_NAME);
    final double convexity1 = CC.calculate(bond, 0.889);
    final double convexity2 = CC.calculate(bond, 0.789);
    assertTrue(convexity1 > convexity2);
  }

  @Test
  public void testWithCurves() {
    final Bond bond = new Bond(new double[] {1, 2, 3}, 0.03, CURVE_NAME);
    final YieldCurveBundle curves = new YieldCurveBundle(new String[] {CURVE_NAME}, new YieldCurve[] {new YieldCurve(ConstantDoublesCurve.from(0.05))});
    final double dirtyPrice = BondDirtyPriceCalculator.getInstance().calculate(bond, curves);
    assertEquals(CC.calculate(bond, curves), CC.calculate(bond, dirtyPrice), 1e-15);
  }
}
