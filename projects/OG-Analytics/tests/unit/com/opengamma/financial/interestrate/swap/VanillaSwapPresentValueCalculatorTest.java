/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.LinearInterpolator1D;

/**
 * 
 */
public class VanillaSwapPresentValueCalculatorTest {
  private static final double[] FIXED_TIMES = new double[] {1, 2, 3, 4, 5};
  private static final double[] FIXED_PAYMENT = new double[] {10, 10, 10, 10, 10};
  private static final double FLOAT_TIME = 1.5;
  private static final double FLOAT_PAYMENT = -10.5;
  private static final YieldAndDiscountCurve CURVE = new DiscountCurve(InterpolatedDoublesCurve.from(new double[] {0, 5}, new double[] {1, 0.5}, new LinearInterpolator1D()));
  private static final VanillaSwapPresentValueCalculator CALCULATOR = new VanillaSwapPresentValueCalculator();

  @Test(expected = IllegalArgumentException.class)
  public void testNullFixedTimes() {
    CALCULATOR.getPresentValue(null, FIXED_PAYMENT, FLOAT_TIME, FLOAT_PAYMENT, CURVE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFixedPayment() {
    CALCULATOR.getPresentValue(FIXED_TIMES, null, FLOAT_TIME, FLOAT_PAYMENT, CURVE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurve() {
    CALCULATOR.getPresentValue(FIXED_TIMES, FIXED_PAYMENT, FLOAT_TIME, FLOAT_PAYMENT, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongTimes() {
    CALCULATOR.getPresentValue(new double[] {1, 2, 3, 4}, FIXED_PAYMENT, FLOAT_TIME, FLOAT_PAYMENT, CURVE);
  }

  @Test
  public void test() {
    final double pv = CALCULATOR.getPresentValue(FIXED_TIMES, FIXED_PAYMENT, FLOAT_TIME, FLOAT_PAYMENT, CURVE);
    final double expected = 10 * (0.9 + 0.8 + 0.7 + 0.6 + 0.5) - 10.5 * 0.85;
    assertEquals(expected, pv, 1e-12);
  }
}
