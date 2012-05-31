/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.horizon;

import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;

/**
 * 
 */
public class ConstantSpreadCurveRolldownFunctionTest {
  private static final double[] TIMES = new double[] {1, 2, 3, 4, 5, 10 };
  private static final double[] RATES = new double[] {0.03, 0.05, 0.04, 0.07, 0.02, 0.1 };
  private static final YieldCurve INTERPOLATED_CURVE = new YieldCurve(InterpolatedDoublesCurve.from(TIMES, RATES, Interpolator1DFactory.LINEAR_INSTANCE));
  private static final ConstantSpreadCurveRolldownFunction FUNCTION = ConstantSpreadCurveRolldownFunction.getInstance();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurve() {
    FUNCTION.rollDownCurve(null, 1);
  }

  @Test
  public void testRolldown() {
    final YieldAndDiscountCurve newYieldCurve = FUNCTION.rollDownCurve(INTERPOLATED_CURVE, 1);
    assertTrue(newYieldCurve.getCurve() instanceof FunctionalDoublesCurve);
    assertEquals(newYieldCurve.getInterestRate(1.), INTERPOLATED_CURVE.getInterestRate(2.), 0);
    assertEquals(newYieldCurve.getInterestRate(2.), INTERPOLATED_CURVE.getInterestRate(3.), 0);
    assertEquals(newYieldCurve.getInterestRate(3.), INTERPOLATED_CURVE.getInterestRate(4.), 0);
    assertEquals(newYieldCurve.getInterestRate(4.), INTERPOLATED_CURVE.getInterestRate(5.), 0);
    assertEquals(newYieldCurve.getInterestRate(5.), INTERPOLATED_CURVE.getInterestRate(6.), 0);
  }
}
