/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class YieldAndDiscountAddZeroSpreadCurveTest {

  private static final double[] TIME_YIELD = new double[] {1, 2, 2.5, 3};
  private static final double[] TIME_DSC = new double[] {1, 2, 3};
  private static final InterpolatedDoublesCurve R = InterpolatedDoublesCurve.from(TIME_YIELD, new double[] {0.03, 0.04, 0.04, 0.05}, new LinearInterpolator1D());
  private static final InterpolatedDoublesCurve DF = InterpolatedDoublesCurve.from(TIME_DSC, new double[] {Math.exp(-0.03), Math.exp(-0.08), Math.exp(-0.15)}, new LinearInterpolator1D());
  private static final YieldCurve YIELD = YieldCurve.from(R);
  private static final DiscountCurve DISCOUNT = DiscountCurve.from(DF);

  private static final YieldAndDiscountAddZeroSpreadCurve ZERO_ADD_SPREAD = new YieldAndDiscountAddZeroSpreadCurve("Spread", false, YIELD, DISCOUNT);

  private static final double TOLERANCE_RATE = 1.0E-10;

  @Test
  /**
   * Test the number of parameters.
   */
  public void numberOfParameters() {
    assertEquals("YieldAndDiscountAddZeroSpreadCurve: number of parameters", YIELD.getNumberOfParameters() + DISCOUNT.getNumberOfParameters(), ZERO_ADD_SPREAD.getNumberOfParameters(), TOLERANCE_RATE);
  }

  @Test
  /**
   * Test the interest rate for the spread curve.
   */
  public void interestRate() {
    int nbPt = 20;
    YieldAndDiscountAddZeroSpreadCurve zeroSpreadSubstract = new YieldAndDiscountAddZeroSpreadCurve("Spread substract", true, YIELD, DISCOUNT);
    for (int looppt = 0; looppt <= nbPt; looppt++) {
      double time = TIME_YIELD[0] + looppt * (TIME_YIELD[TIME_YIELD.length - 1] - TIME_YIELD[0]) / nbPt;
      assertEquals("YieldAndDiscountAddZeroSpreadCurve: zc rate ", YIELD.getInterestRate(time) + DISCOUNT.getInterestRate(time), ZERO_ADD_SPREAD.getInterestRate(time), TOLERANCE_RATE);
      assertEquals("YieldAndDiscountAddZeroSpreadCurve: zc rate ", YIELD.getInterestRate(time) - DISCOUNT.getInterestRate(time), zeroSpreadSubstract.getInterestRate(time), TOLERANCE_RATE);
    }
  }

  @Test
  /**
   * Test the parameter sensitivity.
   */
  public void parameterSensitivity() {
    int nbPt = 20;
    YieldAndDiscountAddZeroSpreadCurve zeroSpreadSubstract = new YieldAndDiscountAddZeroSpreadCurve("Spread substract", true, YIELD, DISCOUNT);
    int nbYield = YIELD.getNumberOfParameters();
    int nbDsc = DISCOUNT.getNumberOfParameters();
    for (int looppt = 0; looppt <= nbPt; looppt++) {
      double time = TIME_YIELD[0] + looppt * (TIME_YIELD[TIME_YIELD.length - 1] - TIME_YIELD[0]) / nbPt;
      double[] sensiAddSpread = ZERO_ADD_SPREAD.getInterestRateParameterSensitivity(time);
      double[] sensiSubSpread = zeroSpreadSubstract.getInterestRateParameterSensitivity(time);
      assertEquals("YieldAndDiscountAddZeroSpreadCurve: nb sensitivity to parameters - add", nbYield + nbDsc, sensiAddSpread.length);
      assertEquals("YieldAndDiscountAddZeroSpreadCurve: nb sensitivity to parameters - substract", nbYield + nbDsc, sensiSubSpread.length);
      double[] sensiYield = YIELD.getInterestRateParameterSensitivity(time);
      double[] sensiDsc = DISCOUNT.getInterestRateParameterSensitivity(time);
      for (int loops = 0; loops < nbYield; loops++) {
        assertEquals("YieldAndDiscountAddZeroSpreadCurve: sensitivity to parameters - add " + loops, sensiYield[loops], sensiAddSpread[loops], TOLERANCE_RATE);
        assertEquals("YieldAndDiscountAddZeroSpreadCurve: sensitivity to parameters- substract " + loops, sensiYield[loops], sensiSubSpread[loops], TOLERANCE_RATE);
      }
      for (int loops = 0; loops < nbDsc; loops++) {
        assertEquals("YieldAndDiscountAddZeroSpreadCurve: sensitivity to parameters - add " + loops, sensiDsc[loops], sensiAddSpread[loops + nbYield], TOLERANCE_RATE);
        assertEquals("YieldAndDiscountAddZeroSpreadCurve: sensitivity to parameters- substract " + loops, sensiDsc[loops], sensiSubSpread[loops + nbYield], TOLERANCE_RATE);
      }
    }
  }

}
