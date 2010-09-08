/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap;

import org.junit.Test;

import com.opengamma.financial.interestrate.ParRateCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.model.interestrate.curve.ConstantDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.InterpolatedDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.math.interpolation.LinearInterpolator1D;

/**
 * 
 */
public class SwapRateCalculatorTest {
  private static final double DF_1 = 0.95;
  private static final ParRateCalculator CALCULATOR = ParRateCalculator.getInstance();
  private static final YieldAndDiscountCurve FUNDING_CURVE = new ConstantDiscountCurve(DF_1);
  private static final YieldAndDiscountCurve FORWARD_CURVE;
  private static final String FUNDING_CURVE_NAME = "Bill";
  private static final String FORWARD_CURVE_NAME = "Ben";
  private static final FixedFloatSwap SWAP;

  static {
    final double[] t1 = new double[] {0, 0.5, 1, 1.5, 2, 2.5, 3, 3.5};
    final int n = t1.length;
    final double[] t2 = new double[] {0.5, 1, 1.5, 2, 2.5, 3};
    final double[] forward = new double[n];
    for (int i = 0; i < n; i++) {
      forward[i] = Math.pow(DF_1, t1[i]);
    }
    FORWARD_CURVE = new InterpolatedDiscountCurve(t1, forward, new LinearInterpolator1D());
    SWAP = new FixedFloatSwap(t2, t2, 0.0, FUNDING_CURVE_NAME, FORWARD_CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullForwardCurve() {
    YieldCurveBundle bundle = new YieldCurveBundle();
    bundle.setCurve(FUNDING_CURVE_NAME, FUNDING_CURVE);
    CALCULATOR.getValue(SWAP, bundle);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFundingCurve() {
    YieldCurveBundle bundle = new YieldCurveBundle();
    bundle.setCurve(FORWARD_CURVE_NAME, FORWARD_CURVE);
    CALCULATOR.getValue(SWAP, bundle);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSwap() {
    YieldCurveBundle bundle = new YieldCurveBundle();
    bundle.setCurve(FUNDING_CURVE_NAME, FUNDING_CURVE);
    bundle.setCurve(FORWARD_CURVE_NAME, FORWARD_CURVE);
    CALCULATOR.getValue(null, bundle);
  }

  @Test
  public void test() {
    YieldCurveBundle bundle = new YieldCurveBundle();
    bundle.setCurve(FUNDING_CURVE_NAME, FUNDING_CURVE);
    bundle.setCurve(FORWARD_CURVE_NAME, FORWARD_CURVE);
    CALCULATOR.getValue(SWAP, bundle);
  }
}
