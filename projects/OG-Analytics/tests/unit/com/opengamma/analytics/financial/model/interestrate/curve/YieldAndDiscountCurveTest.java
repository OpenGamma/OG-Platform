/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;

/**
 * 
 */
public class YieldAndDiscountCurveTest {

  private static final double[] TIME = new double[] {1, 2, 3};
  private static final InterpolatedDoublesCurve R = InterpolatedDoublesCurve.from(TIME, new double[] {0.03, 0.04, 0.05}, new LinearInterpolator1D());
  private static final InterpolatedDoublesCurve DF = InterpolatedDoublesCurve.from(TIME, new double[] {Math.exp(-0.03), Math.exp(-0.08), Math.exp(-0.15)}, new LinearInterpolator1D());
  private static final YieldCurve YIELD = YieldCurve.from(R);
  private static final DiscountCurve DISCOUNT = DiscountCurve.from(DF);

  private static final double TOLERANCE_RATE = 1.0E-10;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullName() {
    new YieldCurve(null, R);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurve() {
    new YieldCurve("Name", null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullNameDsc() {
    new DiscountCurve(null, R);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurveDsc() {
    new DiscountCurve("Name", null);
  }

  @Test
  public void testHashCodeAndEquals() {
    YieldAndDiscountCurve other = YieldCurve.from(R);
    assertEquals(other, YIELD);
    assertEquals(other.hashCode(), YIELD.hashCode());
    other = YieldCurve.from(DF);
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
    double shift = 0.03;
    int nbPt = 20;
    YieldAndDiscountCurve shifted1 = YIELD.withParallelShift(shift);
    for (int looppt = 0; looppt <= nbPt; looppt++) {
      double time = TIME[0] + looppt * (TIME[TIME.length - 1] - TIME[0]) / nbPt;
      assertEquals("ParallelShift", YIELD.getInterestRate(time) + shift, shifted1.getInterestRate(time), TOLERANCE_RATE);
    }
    double timeShift = 1.5;
    YieldAndDiscountCurve shifted2 = YIELD.withSingleShift(timeShift, shift);
    for (int looppt = 0; looppt <= nbPt; looppt++) {
      double time = TIME[0] + looppt * (TIME[TIME.length - 1] - TIME[0]) / nbPt;
      double localShift = Math.abs(time - timeShift) < 1.0E-3 ? shift : 0.0;
      assertEquals("SingleShift", YIELD.getInterestRate(time) + localShift, shifted2.getInterestRate(time), TOLERANCE_RATE);
    }
  }

}
