/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap;

import org.junit.Test;

import com.opengamma.financial.interestrate.InterestRateCalculator;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.ConstantDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.InterpolatedDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.interpolation.LinearInterpolator1D;

/**
 * 
 */
public class SwapRateCalculatorTest {
  private static final double DF_1 = 0.95;
  private static final InterestRateCalculator CALCULATOR = new InterestRateCalculator();
  private static final YieldAndDiscountCurve FUNDING_CURVE = new ConstantDiscountCurve(DF_1);
  private static final YieldAndDiscountCurve FORWARD_CURVE;
  private static final Swap SWAP;

  static {
    final double[] t1 = new double[] {0, 0.5, 1, 1.5, 2, 2.5, 3, 3.5};
    final int n = t1.length;
    final double[] t2 = new double[] {0.5, 1, 1.5, 2, 2.5, 3};
    final double[] delta = new double[] {0, 0, 0, 0, 0, 0};
    final double[] forward = new double[n];
    for (int i = 0; i < n; i++) {
      forward[i] = Math.pow(DF_1, t1[i]);
    }
    FORWARD_CURVE = new InterpolatedDiscountCurve(t1, forward, new LinearInterpolator1D());
    SWAP = new Swap(t2, t2, delta, delta);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullForwardCurve() {
    CALCULATOR.getRate(null, FUNDING_CURVE, SWAP);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFundingCurve() {
    CALCULATOR.getRate(FORWARD_CURVE, null, SWAP);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSwap() {
    CALCULATOR.getRate(FORWARD_CURVE, FUNDING_CURVE, null);
  }

  @Test
  public void test() {
    CALCULATOR.getRate(FORWARD_CURVE, FUNDING_CURVE, SWAP);
  }
}
