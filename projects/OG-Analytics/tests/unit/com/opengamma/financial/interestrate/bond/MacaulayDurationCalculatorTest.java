/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.ConstantDoublesCurve;

/**
 * 
 */
public class MacaulayDurationCalculatorTest {
  private static final MacaulayDurationCalculator MDC = new MacaulayDurationCalculator();
  private static final String CURVE_NAME = "Test Curve";

  @Test(expected = IllegalArgumentException.class)
  public void testNullBond() {
    MDC.calculate(null, 1.0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeDirtyPrice() {
    MDC.calculate(new Bond(new double[] {1, 2, 3}, 0.05, CURVE_NAME), -0.4);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testZeroPrice() {
    final int n = 10;
    final double[] paymentTimes = new double[n];
    final double tau = 0.5;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
    }
    final Bond bond = new Bond(paymentTimes, 0.0, CURVE_NAME);
    MDC.calculate(bond, 0.0);
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
    final double duration = MDC.calculate(bond, 0.889);
    assertEquals(n * tau, duration, 1e-8);
  }

  @Test
  public void testMultiplePayment() {
    final int n = 10;
    final double[] paymentTimes = new double[n];
    final double tau = 0.5;
    final double alpha = 0.48;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
    }
    final double yield = 0.05;
    final double coupon = (Math.exp(yield * tau) - 1) / alpha;
    final Bond bond = new Bond(paymentTimes, coupon, alpha, 0.0, CURVE_NAME);
    final double duration = MDC.calculate(bond, 1.0);

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
    final int n = 10;
    final double[] paymentTimes = new double[n];
    final double tau = 0.5;
    for (int i = 0; i < n; i++) {
      paymentTimes[i] = (i + 1) * tau;
    }
    final Bond bond = new Bond(paymentTimes, 0.05, CURVE_NAME);
    final double duration1 = MDC.calculate(bond, 0.889);
    final double duration2 = MDC.calculate(bond, 0.789);
    assertTrue(duration1 > duration2);
  }

  @Test
  public void testWithCurve() {
    final double[] t = new double[] {1, 2, 3, 4};
    final double[] coupons = new double[] {0.01, 0.01, 0.01, 0.01};
    final double[] fractions = new double[] {0.5, 1, 1, 1};
    final Bond bond = new Bond(t, coupons, fractions, 0, CURVE_NAME);
    final YieldCurveBundle curves = new YieldCurveBundle(new String[] {CURVE_NAME}, new YieldCurve[] {new YieldCurve(ConstantDoublesCurve.from(0.05))});
    assertEquals(MDC.calculate(bond, BondDirtyPriceCalculator.getInstance().calculate(bond, curves)), MDC.calculate(bond, curves), 0);
  }
}
