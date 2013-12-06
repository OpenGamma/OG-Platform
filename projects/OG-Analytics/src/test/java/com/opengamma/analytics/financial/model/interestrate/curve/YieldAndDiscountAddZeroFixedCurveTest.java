/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the curve construction as the sum (or difference) of a main curve and a fixed curve.
 */
@Test(groups = TestGroup.UNIT)
public class YieldAndDiscountAddZeroFixedCurveTest {

  private static final Interpolator1D INTERPOLATOR_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final double[] TIME = new double[] {1, 2, 2.5, 3};
  private static final double[] YIELD = new double[] {0.01, 0.02, 0.02, 0.01};
  private static final InterpolatedDoublesCurve R = InterpolatedDoublesCurve.from(TIME, YIELD, INTERPOLATOR_LINEAR);
  private static final YieldAndDiscountCurve CURVE_MAIN = new YieldCurve("Main", R);
  private static final YieldAndDiscountCurve CURVE_FIXED = new YieldCurve("Fixed", new ConstantDoublesCurve(0.0010, "Fixed"));
  private static final String NAME = "Dsc";
  private static final YieldAndDiscountCurve CURVE_TOTAL = new YieldAndDiscountAddZeroFixedCurve(NAME, false, CURVE_MAIN, CURVE_FIXED);
  private static final double TOLERANCE_RATE = 1.0E-10;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void name() {
    new YieldAndDiscountAddZeroFixedCurve(null, false, CURVE_MAIN, CURVE_FIXED);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void curve() {
    new YieldAndDiscountAddZeroFixedCurve(NAME, false, null, CURVE_FIXED);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void curveFixed() {
    new YieldAndDiscountAddZeroFixedCurve(NAME, false, CURVE_MAIN, null);
  }

  @Test
  public void interestRate() {
    double[] t = {0.5, 1.0, 2.75};
    for (int loopt = 0; loopt < t.length; loopt++) {
      assertEquals("YieldAndDiscountAddZeroFixedCurve: rate", CURVE_MAIN.getInterestRate(t[loopt]) + CURVE_FIXED.getInterestRate(t[loopt]), CURVE_TOTAL.getInterestRate(t[loopt]), TOLERANCE_RATE);
    }
  }

  @Test
  public void parameters() {
    assertEquals("YieldAndDiscountAddZeroFixedCurve: parameters", CURVE_MAIN.getNumberOfParameters(), CURVE_TOTAL.getNumberOfParameters());
  }

}
