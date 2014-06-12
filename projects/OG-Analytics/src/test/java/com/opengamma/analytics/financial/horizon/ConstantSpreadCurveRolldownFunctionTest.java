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
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ConstantSpreadCurveRolldownFunctionTest {
  private static final double[] TIMES = new double[] {1, 2, 3, 4, 5, 10};
  private static final double[] RATES = new double[] {0.03, 0.05, 0.04, 0.07, 0.02, 0.1};
  private static final YieldCurve INTERPOLATED_CURVE = YieldCurve.from(InterpolatedDoublesCurve.from(TIMES, RATES, Interpolator1DFactory.LINEAR_INSTANCE));
  private static final YieldCurve FUNCTIONAL_CURVE = YieldCurve.from(FunctionalDoublesCurve.from(new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return 0.03 - x * 0.01;
    }

  }));
  private static final ConstantSpreadYieldCurveRolldownFunction FUNCTION = ConstantSpreadYieldCurveRolldownFunction.getInstance();
  private static final double EPS = 1e-15;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurve() {
    FUNCTION.rollDown(null, 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighT() {
    FUNCTION.rollDown(INTERPOLATED_CURVE, 1).getInterestRate(9.9);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLowT() {
    FUNCTION.rollDown(INTERPOLATED_CURVE, -2).getInterestRate(0.1);
  }

  @Test
  public void testRolldown() {
    YieldAndDiscountCurve newYieldCurve = FUNCTION.rollDown(INTERPOLATED_CURVE, 1);
    assertTrue(((YieldCurve) newYieldCurve).getCurve() instanceof FunctionalDoublesCurve);
    assertEquals(newYieldCurve.getInterestRate(1.), INTERPOLATED_CURVE.getInterestRate(2.), 0);
    assertEquals(newYieldCurve.getInterestRate(2.), INTERPOLATED_CURVE.getInterestRate(3.), 0);
    assertEquals(newYieldCurve.getInterestRate(3.), INTERPOLATED_CURVE.getInterestRate(4.), 0);
    assertEquals(newYieldCurve.getInterestRate(4.), INTERPOLATED_CURVE.getInterestRate(5.), 0);
    assertEquals(newYieldCurve.getInterestRate(5.), INTERPOLATED_CURVE.getInterestRate(6.), 0);
    assertEquals(0.045, newYieldCurve.getInterestRate(1.5), 0);
    assertEquals(0.04, newYieldCurve.getInterestRate(0.5), 0);
    assertEquals(0.02 + 4 * 0.08 / 5, newYieldCurve.getInterestRate(8.), 0);
    newYieldCurve = FUNCTION.rollDown(INTERPOLATED_CURVE, 3);
    assertTrue(((YieldCurve) newYieldCurve).getCurve() instanceof FunctionalDoublesCurve);
    assertEquals(newYieldCurve.getInterestRate(1.), INTERPOLATED_CURVE.getInterestRate(4.), 0);
    assertEquals(newYieldCurve.getInterestRate(2.), INTERPOLATED_CURVE.getInterestRate(5.), 0);
    assertEquals(newYieldCurve.getInterestRate(3.), INTERPOLATED_CURVE.getInterestRate(6.), 0);
    assertEquals(newYieldCurve.getInterestRate(4.), INTERPOLATED_CURVE.getInterestRate(7.), 0);
    assertEquals(newYieldCurve.getInterestRate(5.), INTERPOLATED_CURVE.getInterestRate(8.), 0);
    newYieldCurve = FUNCTION.rollDown(INTERPOLATED_CURVE, -1);
    assertTrue(((YieldCurve) newYieldCurve).getCurve() instanceof FunctionalDoublesCurve);
    assertEquals(newYieldCurve.getInterestRate(2.1), INTERPOLATED_CURVE.getInterestRate(1.1), 0);
    assertEquals(newYieldCurve.getInterestRate(2.), INTERPOLATED_CURVE.getInterestRate(1.), 0);
    assertEquals(newYieldCurve.getInterestRate(3.), INTERPOLATED_CURVE.getInterestRate(2.), 0);
    assertEquals(newYieldCurve.getInterestRate(4.), INTERPOLATED_CURVE.getInterestRate(3.), 0);
    assertEquals(newYieldCurve.getInterestRate(5.), INTERPOLATED_CURVE.getInterestRate(4.), 0);
    newYieldCurve = FUNCTION.rollDown(FUNCTIONAL_CURVE, 1);
    assertTrue(((YieldCurve) newYieldCurve).getCurve() instanceof FunctionalDoublesCurve);
    assertEquals(newYieldCurve.getInterestRate(1.), FUNCTIONAL_CURVE.getInterestRate(2.), 0);
    assertEquals(newYieldCurve.getInterestRate(2.), FUNCTIONAL_CURVE.getInterestRate(3.), 0);
    assertEquals(newYieldCurve.getInterestRate(3.), FUNCTIONAL_CURVE.getInterestRate(4.), 0);
    assertEquals(newYieldCurve.getInterestRate(4.), FUNCTIONAL_CURVE.getInterestRate(5.), 0);
    assertEquals(newYieldCurve.getInterestRate(5.), FUNCTIONAL_CURVE.getInterestRate(6.), 0);
    final Function1D<Double, Double> newF = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return 0.02 - x * 0.01;
      }

    };
    assertEquals(newF.evaluate(1.5), newYieldCurve.getInterestRate(1.5), EPS);
    assertEquals(newF.evaluate(0.5), newYieldCurve.getInterestRate(0.5), EPS);
    assertEquals(newF.evaluate(8.), newYieldCurve.getInterestRate(8.), EPS);

  }
}
