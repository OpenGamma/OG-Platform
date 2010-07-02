/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.InterpolatedDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.interpolation.LinearInterpolator1D;

/**
 * 
 */
public class LiborCalculatorTest {
  private static final double DF = 0.95;
  private static final LiborCalculator CALCULATOR = new LiborCalculator();
  private static final YieldAndDiscountCurve FLAT_CURVE = new ConstantDiscountCurve(DF);
  private static final YieldAndDiscountCurve CURVE;
  private static final double[] PAYMENT_TIME = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
  private static final double[] OFFSET = new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  private static final Swap NO_OFFSET_SWAP = new Swap(PAYMENT_TIME, PAYMENT_TIME, OFFSET, OFFSET);

  static {
    final int n = PAYMENT_TIME.length + 1;
    final double[] t = new double[n];
    final double[] df = new double[n];
    for (int i = 0; i < n; i++) {
      t[i] = i;
      df[i] = Math.pow(DF, i);
    }
    CURVE = new InterpolatedDiscountCurve(t, df, new LinearInterpolator1D());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurve() {
    CALCULATOR.getLiborRate(null, NO_OFFSET_SWAP);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSwap() {
    CALCULATOR.getLiborRate(FLAT_CURVE, null);
  }

  @Test
  public void test() {
    double[] result = CALCULATOR.getLiborRate(FLAT_CURVE, NO_OFFSET_SWAP);
    for (final double r : result) {
      assertEquals(r, 0, 0);
    }
    result = CALCULATOR.getLiborRate(CURVE, NO_OFFSET_SWAP);
    for (final double r : result) {
      assertEquals(r, 1. / .95 - 1, 1e-15);
    }
  }
}
