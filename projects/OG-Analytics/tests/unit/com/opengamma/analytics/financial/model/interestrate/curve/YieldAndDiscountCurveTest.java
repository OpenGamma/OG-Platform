/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedCurveShiftFunction;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;

/**
 * 
 */
public class YieldAndDiscountCurveTest {
  private static final InterpolatedDoublesCurve R = InterpolatedDoublesCurve.from(new double[] {1, 2, 3}, new double[] {0.03, 0.04, 0.05}, new LinearInterpolator1D());
  private static final InterpolatedDoublesCurve DF = InterpolatedDoublesCurve
      .from(new double[] {1, 2, 3}, new double[] {Math.exp(-0.03), Math.exp(-0.08), Math.exp(-0.15)}, new LinearInterpolator1D());
  private static final YieldAndDiscountCurve YIELD = new YieldCurve(R);
  private static final YieldAndDiscountCurve DISCOUNT = new DiscountCurve(DF);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull1() {
    new YieldCurve(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull2() {
    new DiscountCurve(null);
  }

  @Test
  public void testHashCodeAndEquals() {
    YieldAndDiscountCurve other = new YieldCurve(R);
    assertEquals(other, YIELD);
    assertEquals(other.hashCode(), YIELD.hashCode());
    other = new YieldCurve(DF);
    assertFalse(other.equals(YIELD));
  }

  @Test
  public void testGetters() {
    assertEquals(YIELD.getCurve(), R);
    assertEquals(YIELD.getInterestRate(1.4), R.getYValue(1.4), 1e-15);
    assertEquals(YIELD.getDiscountFactor(1.5), Math.exp(-1.5 * R.getYValue(1.5)), 1e-15);
    assertEquals(DISCOUNT.getInterestRate(1.4), -Math.log(DF.getYValue(1.4)) / 1.4, 1e-15);
    assertEquals(DISCOUNT.getDiscountFactor(1.5), DF.getYValue(1.5), 1e-15);
  }

  @Test
  public void testShift() {
    final InterpolatedCurveShiftFunction f = new InterpolatedCurveShiftFunction();
    YieldAndDiscountCurve shifted1 = YIELD.withParallelShift(3);
    InterpolatedDoublesCurve shifted2 = f.evaluate(R, 3.);
    assertArrayEquals(shifted1.getCurve().getXData(), shifted2.getXData());
    assertArrayEquals(shifted1.getCurve().getYData(), shifted2.getYData());
    shifted1 = YIELD.withSingleShift(1, 3);
    shifted2 = f.evaluate(R, 1, 3.);
    assertArrayEquals(shifted1.getCurve().getXData(), shifted2.getXData());
    assertArrayEquals(shifted1.getCurve().getYData(), shifted2.getYData());
    shifted1 = YIELD.withMultipleShifts(new double[] {1, 2}, new double[] {3, 4});
    shifted2 = f.evaluate(R, new double[] {1, 2}, new double[] {3, 4});
    assertArrayEquals(shifted1.getCurve().getXData(), shifted2.getXData());
    assertArrayEquals(shifted1.getCurve().getYData(), shifted2.getYData());
  }
}
