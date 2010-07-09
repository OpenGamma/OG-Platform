/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.ConstantDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * 
 */
public class AnnuityCalculatorTest {
  private static final double DF = 0.9;
  private static final AnnuityCalculator CALCULATOR = new AnnuityCalculator();
  private static final YieldAndDiscountCurve CURVE = new ConstantDiscountCurve(DF);
  private static final double[] PAYMENT_TIME = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
  private static final double[] OFFSET = new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  private static final Swap SWAP = new Swap(PAYMENT_TIME, PAYMENT_TIME, OFFSET, OFFSET);

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurve() {
    CALCULATOR.getAnnuity(null, SWAP);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSwap() {
    CALCULATOR.getAnnuity(CURVE, null);
  }

  @Test
  public void test() {
    assertEquals(CALCULATOR.getAnnuity(CURVE, SWAP), DF * 10, 1e-12);
  }

}
