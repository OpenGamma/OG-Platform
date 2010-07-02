/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * 
 */
public class LiborCalculatorTest {
  private static final double DF = 0.95;
  private static final LiborCalculator CALCULATOR = new LiborCalculator();
  private static final YieldAndDiscountCurve FLAT_CURVE = new ConstantDiscountCurve(DF);
  private static final YieldAndDiscountCurve LINEAR_CURVE = new InterpolatedDiscountCurve(new double[], new double[], )
  private static final double[] PAYMENT_TIME = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
  private static final double[] OFFSET = new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  private static final Swap SWAP = new Swap(PAYMENT_TIME, PAYMENT_TIME, OFFSET, OFFSET);

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurve() {
    CALCULATOR.getLiborRate(null, SWAP);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSwap() {
    CALCULATOR.getLiborRate(FLAT_CURVE, null);
  }

  @Test
  public void test() {
    final double[] result = CALCULATOR.getLiborRate(FLAT_CURVE, SWAP);
    for (final double r : result) {
      assertEquals(r, 0, 0);
    }
  }
}
