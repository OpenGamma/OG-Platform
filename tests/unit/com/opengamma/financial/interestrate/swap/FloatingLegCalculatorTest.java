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
public class FloatingLegCalculatorTest {
  private static final FloatingLegCalculator CALCULATOR = new FloatingLegCalculator();
  private static final double DF_1 = 0.9;
  private static final double DF_2 = 0.8;
  private static final YieldAndDiscountCurve FLAT_FORWARD_CURVE = new ConstantDiscountCurve(DF_1);
  private static final YieldAndDiscountCurve FLAT_FUNDING_CURVE = new ConstantDiscountCurve(DF_2);
  private static final YieldAndDiscountCurve LINEAR_FORWARD_CURVE;
  private static final YieldAndDiscountCurve LINEAR_FUNDING_CURVE;
  private static final double[] PAYMENT_TIME = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
  private static final double[] OFFSET = new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  private static final Swap SWAP = new Swap(PAYMENT_TIME, PAYMENT_TIME, OFFSET, OFFSET);

  static {
    final int n = PAYMENT_TIME.length + 1;
    final double[] t = new double[n];
    final double[] df1 = new double[n];
    final double[] df2 = new double[n];
    for (int i = 0; i < n; i++) {
      t[i] = i;
      df1[i] = Math.pow(DF_1, i);
      df2[i] = Math.pow(DF_2, i);
    }
    LINEAR_FORWARD_CURVE = new InterpolatedDiscountCurve(t, df1, new LinearInterpolator1D());
    LINEAR_FUNDING_CURVE = new InterpolatedDiscountCurve(t, df2, new LinearInterpolator1D());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullForwardCurve() {
    CALCULATOR.getFloatLeg(null, FLAT_FUNDING_CURVE, SWAP);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFundingCurve() {
    CALCULATOR.getFloatLeg(FLAT_FORWARD_CURVE, null, SWAP);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSwap() {
    CALCULATOR.getFloatLeg(FLAT_FORWARD_CURVE, FLAT_FORWARD_CURVE, null);
  }

  @Test
  public void test() {
    assertEquals(CALCULATOR.getFloatLeg(FLAT_FORWARD_CURVE, FLAT_FUNDING_CURVE, SWAP), 0, 0);
    assertEquals(CALCULATOR.getFloatLeg(FLAT_FORWARD_CURVE, LINEAR_FUNDING_CURVE, SWAP), 0, 0);
    assertEquals(CALCULATOR.getFloatLeg(LINEAR_FORWARD_CURVE, FLAT_FUNDING_CURVE, SWAP), 1. / DF_1 * DF_2, 1e-12);
    double expected = 0;
    for (int i = 1; i <= SWAP.getNumberOfFloatingPayments(); i++) {
      expected += Math.pow(DF_2, i) * (1. / DF_1 - 1);
    }
    assertEquals(CALCULATOR.getFloatLeg(LINEAR_FORWARD_CURVE, LINEAR_FUNDING_CURVE, SWAP), expected, 1e-12);
  }
}
