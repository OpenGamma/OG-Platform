/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap;

import org.junit.Test;

import com.opengamma.financial.interestrate.FundingCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * 
 */
public class FundingCurveSensitivityCalculatorTest {
  private static final FundingCurveSensitivityCalculator CALCULATOR = new FundingCurveSensitivityCalculator();
  private static final double DF_1 = 0.95;
  private static final double DF_2 = 0.9;
  private static final YieldAndDiscountCurve FLAT_FORWARD_CURVE = new ConstantDiscountCurve(DF_1);
  private static final YieldAndDiscountCurve FLAT_FUNDING_CURVE = new ConstantDiscountCurve(DF_2);
  // private static final YieldAndDiscountCurve FORWARD_CURVE;
  // private static final YieldAndDiscountCurve FUNDING_CURVE;
  private static final double[] PAYMENT_TIME = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
  private static final double[] OFFSET = new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  private static final Swap NO_OFFSET_SWAP = new Swap(PAYMENT_TIME, PAYMENT_TIME, OFFSET, OFFSET);

  //private static double ANNUITY;
  //private static final double EPS = 1e-12;

  //  static {
  //    final int n = PAYMENT_TIME.length + 1;
  //    final double[] t = new double[n];
  //    final double[] df1 = new double[n];
  //    final double[] df2 = new double[n];
  //    for (int i = 0; i < n; i++) {
  //      t[i] = i;
  //      df1[i] = Math.pow(DF_1, i);
  //      df2[i] = Math.pow(DF_2, i);
  //    }
  //    FORWARD_CURVE = new InterpolatedDiscountCurve(t, df1, new LinearInterpolator1D());
  //    FUNDING_CURVE = new InterpolatedDiscountCurve(t, df2, new LinearInterpolator1D());
  //  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullForwardCurve() {
    CALCULATOR.getFundingCurveSensitivities(null, FLAT_FUNDING_CURVE, NO_OFFSET_SWAP);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFundingCurve() {
    CALCULATOR.getFundingCurveSensitivities(FLAT_FORWARD_CURVE, null, NO_OFFSET_SWAP);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSwap() {
    CALCULATOR.getFundingCurveSensitivities(FLAT_FORWARD_CURVE, FLAT_FUNDING_CURVE, null);
  }

  @Test
  public void test() {
  }
}
