/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class ConstantInterestRateDiscountCurveTest {
  private static final double EPS = 1e-15;

  @Test
  public void test() {
    final double rate = 0.05;
    final DiscountCurve curve = new ConstantInterestRateDiscountCurve(rate);
    try {
      curve.getInterpolator();
      fail();
    } catch (final UnsupportedOperationException e) {
      // Expected
    }
    final double t = 4;
    assertEquals(rate, curve.getInterestRate(t), EPS);
    assertEquals(Math.exp(-rate * t), curve.getDiscountFactor(t), EPS);
  }
}
