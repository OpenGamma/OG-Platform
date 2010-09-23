/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


/**
 * 
 */
public class ConstantDiscountCurveTest {

  @Test(expected = IllegalArgumentException.class)
  public void testHighDF() {
    new ConstantDiscountCurve(1.2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLowDF() {
    new ConstantDiscountCurve(0);
  }

  @Test
  public void test() {
    final double df = 0.95;
    final double t = 0.5;
    final ConstantDiscountCurve curve = new ConstantDiscountCurve(df);
    assertEquals(curve.getMaturities(), null);
    assertEquals(curve.withMultipleShifts(null), null);
    assertEquals(curve.withParallelShift(null), null);
    assertEquals(curve.withSingleShift(null, null), null);
    assertEquals(curve.getDiscountFactor(t), df, 0);
    assertEquals(Math.exp(-curve.getInterestRate(t) * t), df, 1e-15);
  }
}
