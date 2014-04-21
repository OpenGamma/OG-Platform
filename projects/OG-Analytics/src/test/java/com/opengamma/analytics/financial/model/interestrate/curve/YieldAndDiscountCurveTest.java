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

import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.test.TestGroup;

/**
 * Tests related to the construction of yield and discounting curves.
 */
@Test(groups = TestGroup.UNIT)
public class YieldAndDiscountCurveTest {

  private static final double[] TIME = new double[] {1, 2, 3};
  private static final double[] RATES = new double[] {0.03, 0.04, 0.05};
  private static final double[] DF_VALUES = new double[] {Math.exp(-0.03), Math.exp(-0.08), Math.exp(-0.15)};
  private static final Interpolator1D INTERPOLATOR = new LinearInterpolator1D();
  private static final InterpolatedDoublesCurve R = InterpolatedDoublesCurve.from(TIME, RATES, INTERPOLATOR);
  private static final InterpolatedDoublesCurve DF = InterpolatedDoublesCurve.from(TIME, DF_VALUES, INTERPOLATOR);
  private static final YieldCurve YIELD = YieldCurve.from(R);
  private static final DiscountCurve DISCOUNT = DiscountCurve.from(DF);
  private static final int COMPOUNDING = 2;
  private static final YieldPeriodicCurve YIELD_PERIODIC = YieldPeriodicCurve.from(COMPOUNDING, R);

  private static final double TOLERANCE_RATE = 1.0E-10;
  private static final double TOLERANCE_PV = 1.0E-10;
  private static final double TOLERANCE_SENSI = 1.0E-6;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullName() {
    new YieldCurve(null, R);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurve() {
    new YieldCurve("Name", null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullNamePer() {
    new YieldPeriodicCurve(null, COMPOUNDING, R);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullCurvePer() {
    new YieldPeriodicCurve("Name", COMPOUNDING, null);
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
  public void gettersYieldPeriodic() {
    assertEquals("YieldPeriodicCurve: getter", YIELD_PERIODIC.getCurve(), R);
    double point = 1.5;
    assertEquals("YieldPeriodicCurve: getter", YIELD_PERIODIC.getPeriodicInterestRate(point, COMPOUNDING), R.getYValue(point), TOLERANCE_RATE);
    double rate = R.getYValue(point);
    double dfannual = Math.pow(1.0 + rate / COMPOUNDING, -COMPOUNDING);
    double df = Math.pow(1.0 + rate / COMPOUNDING, -point * COMPOUNDING);
    assertEquals("YieldPeriodicCurve: getter", YIELD_PERIODIC.getDiscountFactor(point), df, TOLERANCE_PV);
    assertEquals("YieldPeriodicCurve: getter", YIELD_PERIODIC.getInterestRate(point), -Math.log(dfannual), TOLERANCE_PV);
  }

  @Test
  public void interestRateParameterSensitivityYieldPeriodic() {
    int nbPt = 20;
    double shift = 1.0E-6;
    double[] time = new double[nbPt + 1];
    double[] rt = new double[nbPt + 1];
    double[][] ps = new double[nbPt + 1][];
    for (int looppt = 0; looppt <= nbPt; looppt++) {
      time[looppt] = TIME[0] + looppt * (TIME[TIME.length - 1] - TIME[0]) / nbPt;
      rt[looppt] = YIELD_PERIODIC.getInterestRate(time[looppt]);
      ps[looppt] = YIELD_PERIODIC.getInterestRateParameterSensitivity(time[looppt]);
    }
    for (int loopr = 0; loopr < RATES.length; loopr++) {
      double[] rateShift = RATES.clone();
      rateShift[loopr] += shift;
      YieldPeriodicCurve yieldPeriodicShifted = YieldPeriodicCurve.from(COMPOUNDING, InterpolatedDoublesCurve.from(TIME, rateShift, new LinearInterpolator1D()));
      for (int looppt = 0; looppt <= nbPt; looppt++) {
        double r = yieldPeriodicShifted.getInterestRate(time[looppt]);
        assertEquals("ParameterSensitivity - YieldPeriodic", (r - rt[looppt]) / shift, ps[looppt][loopr], TOLERANCE_SENSI);
      }
    }
  }

  @Test
  public void interestRateParameterSensitivityDiscounting() {
    int nbPt = 20;
    double shift = 1.0E-6;
    double[] time = new double[nbPt + 1];
    double[] rt = new double[nbPt + 1];
    double[][] ps = new double[nbPt + 1][];
    for (int looppt = 0; looppt <= nbPt; looppt++) {
      time[looppt] = TIME[0] + looppt * (TIME[TIME.length - 1] - TIME[0]) / nbPt;
      rt[looppt] = DISCOUNT.getInterestRate(time[looppt]);
      ps[looppt] = DISCOUNT.getInterestRateParameterSensitivity(time[looppt]);
    }
    for (int loopr = 0; loopr < RATES.length; loopr++) {
      double[] dfShift = DF_VALUES.clone();
      dfShift[loopr] += shift;
      DiscountCurve discountShift = DiscountCurve.from(InterpolatedDoublesCurve.from(TIME, dfShift, new LinearInterpolator1D()));
      for (int looppt = 0; looppt <= nbPt; looppt++) {
        double r = discountShift.getInterestRate(time[looppt]);
        assertEquals("ParameterSensitivity - YieldPeriodic", (r - rt[looppt]) / shift, ps[looppt][loopr], TOLERANCE_SENSI);
      }
    }
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

  @Test
  /**
   * Tests the build of a discount curve from yields (cc).
   */
  public void discountCurveFromYieldsInterpolated() {
    DiscountCurve dfFromYields = DiscountCurve.fromYieldsInterpolated(TIME, RATES, INTERPOLATOR, "DF");
    InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) dfFromYields.getCurve();
    Double[] df = curveInt.getYData();
    for (int loopyield = 0; loopyield < TIME.length; loopyield++) {
      assertEquals("DiscountCurve.fromYieldsInterpolated", RATES[loopyield], -Math.log(df[loopyield]) / TIME[loopyield], TOLERANCE_RATE);
    }
    assertEquals("DiscountCurve.fromYieldsInterpolated", INTERPOLATOR, curveInt.getInterpolator());
    assertArrayEquals("DiscountCurve.fromYieldsInterpolated", TIME, curveInt.getXDataAsPrimitive(), TOLERANCE_RATE);
  }

  @Test
  /**
   * Tests the build of a discount curve from yields (cc).
   */
  public void yieldCurveDiscountFactorInterpolated() {
    YieldCurve yFromDF = YieldCurve.fromDiscountFactorInterpolated(TIME, DF_VALUES, INTERPOLATOR, "Yield");
    InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) yFromDF.getCurve();
    Double[] y = curveInt.getYData();
    for (int loopyield = 0; loopyield < TIME.length; loopyield++) {
      assertEquals("YieldCurve.fromDiscountFactorInterpolated", DF_VALUES[loopyield], Math.exp(-y[loopyield] * TIME[loopyield]), TOLERANCE_RATE);
    }
    assertEquals("DiscountCurve.fromYieldsInterpolated", INTERPOLATOR, curveInt.getInterpolator());
    assertArrayEquals("DiscountCurve.fromYieldsInterpolated", TIME, curveInt.getXDataAsPrimitive(), TOLERANCE_RATE);
  }

  @Test
  /**
   * Tests the build of a discount curve from yields (cc).
   */
  public void yieldPeriodicFromYieldsInterpolated() {
    YieldPeriodicCurve perFromYields = YieldPeriodicCurve.fromYieldsInterpolated(TIME, RATES, COMPOUNDING, INTERPOLATOR, "DF");
    InterpolatedDoublesCurve curveInt = (InterpolatedDoublesCurve) perFromYields.getCurve();
    Double[] yP = curveInt.getYData();
    for (int loopyield = 0; loopyield < TIME.length; loopyield++) {
      assertEquals("YieldPeriodicCurve.fromYieldsInterpolated", RATES[loopyield], COMPOUNDING * Math.log(1.0 + yP[loopyield] / COMPOUNDING), TOLERANCE_RATE);
    }
    assertEquals("YieldPeriodicCurve.fromYieldsInterpolated", INTERPOLATOR, curveInt.getInterpolator());
    assertArrayEquals("YieldPeriodicCurve.fromYieldsInterpolated", TIME, curveInt.getXDataAsPrimitive(), TOLERANCE_RATE);
  }

}
