/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.opengamma.financial.interestrate.ForwardCurveSensitivityCalculator;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.financial.model.interestrate.curve.ConstantDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class ForwardCurveSensitivityCalculatorTest {
  private static final ForwardCurveSensitivityCalculator CALCULATOR = new ForwardCurveSensitivityCalculator();
  private static final double DF_1 = 0.95;
  private static final double DF_2 = 0.9;
  private static final YieldAndDiscountCurve FLAT_FORWARD_CURVE = new ConstantDiscountCurve(DF_1);
  private static final YieldAndDiscountCurve FLAT_FUNDING_CURVE = new ConstantDiscountCurve(DF_2);
  //private static final YieldAndDiscountCurve LINEAR_FORWARD_CURVE;
  //private static final YieldAndDiscountCurve LINEAR_FUNDING_CURVE;
  private static final double[] PAYMENT_TIME = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
  private static final double[] OFFSET = new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  private static final Swap NO_OFFSET_SWAP = new Swap(PAYMENT_TIME, PAYMENT_TIME, OFFSET, OFFSET);
  //private static double ANNUITY;
  private static final double EPS = 1e-12;

  //  static {
  //    final int n = PAYMENT_TIME.length + 1;
  //    final double[] t = new double[n];
  //    final double[] df1 = new double[n];
  //    final double[] df2 = new double[n];
  //    for (int i = 0; i < n; i++) {
  //      t[i] = i;
  //      df1[i] = Math.pow(DF_1, i);
  //      df2[i] = Math.pow(DF_2, i);
  //      if (i != 0) {
  //        ANNUITY += df2[i];
  //      }
  //    }
  //    LINEAR_FORWARD_CURVE = new InterpolatedDiscountCurve(t, df1, new LinearInterpolator1D());
  //    LINEAR_FUNDING_CURVE = new InterpolatedDiscountCurve(t, df2, new LinearInterpolator1D());
  //  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullForwardCurve() {
    CALCULATOR.getForwardCurveSensitivities(null, FLAT_FUNDING_CURVE, NO_OFFSET_SWAP);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFundingCurve() {
    CALCULATOR.getForwardCurveSensitivities(FLAT_FORWARD_CURVE, null, NO_OFFSET_SWAP);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSwap() {
    CALCULATOR.getForwardCurveSensitivities(FLAT_FORWARD_CURVE, FLAT_FUNDING_CURVE, null);
  }

  @Test
  public void test() {
    final List<Pair<Double, Double>> result = CALCULATOR.getForwardCurveSensitivities(FLAT_FORWARD_CURVE, FLAT_FUNDING_CURVE, NO_OFFSET_SWAP);
    final int n = PAYMENT_TIME.length;
    Pair<Double, Double> pair;
    for (int i = 0; i < result.size(); i++) {
      pair = result.get(i);
      if (i == 0) {
        assertEquals(pair.getSecond(), 0, 0);
      } else if (i % 2 == 0) {
        assertEquals(pair.getFirst(), i / 2, 0);
        assertEquals(pair.getSecond(), -PAYMENT_TIME[i / 2 - 1] * DF_2 / (DF_2 * n), EPS);
      } else {
        assertEquals(pair.getFirst(), (i + 1) / 2, 0);
        assertEquals(pair.getSecond(), PAYMENT_TIME[i / 2] * DF_2 / (DF_2 * n), EPS);
      }
    }
  }
}
