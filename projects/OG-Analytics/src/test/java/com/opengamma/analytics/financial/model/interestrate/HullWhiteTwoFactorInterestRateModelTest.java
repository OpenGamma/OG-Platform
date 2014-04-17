/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteTwoFactorDataBundle;
import com.opengamma.analytics.financial.model.volatility.curve.VolatilityCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class HullWhiteTwoFactorInterestRateModelTest {
  private static final double RATE = 0.04;
  private static final double SIGMA1 = 0.1;
  private static final double SIGMA2 = 0.15;
  private static final double T1 = 1.4;
  private static final double T2 = 16;
  private static final ZonedDateTime TODAY = DateUtils.getUTCDate(2010, 8, 1);
  private static final ZonedDateTime START = DateUtils.getDateOffsetWithYearFraction(TODAY, T1);
  private static final ZonedDateTime MATURITY = DateUtils.getDateOffsetWithYearFraction(START, T2);
  private static final YieldCurve R = YieldCurve.from(ConstantDoublesCurve.from(RATE));
  private static final VolatilityCurve VOL1 = new VolatilityCurve(ConstantDoublesCurve.from(SIGMA1));
  private static final VolatilityCurve VOL2 = new VolatilityCurve(ConstantDoublesCurve.from(SIGMA2));
  private static final double SPEED1 = 0.2;
  private static final double SPEED2 = 0.07;
  private static final double U = 0.13;
  private static final double F = 0.06;
  private static final double RHO = 0.43;
  private static final HullWhiteTwoFactorInterestRateModel MODEL = new HullWhiteTwoFactorInterestRateModel();
  private static final double EPS = 1e-9;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDate() {
    MODEL.getDiscountBondFunction(null, MATURITY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMaturity() {
    MODEL.getDiscountBondFunction(START, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getDiscountBondFunction(START, MATURITY).evaluate((HullWhiteTwoFactorDataBundle) null);
  }

  @Test
  public void test() {
    HullWhiteTwoFactorDataBundle data = new HullWhiteTwoFactorDataBundle(R, new VolatilityCurve(ConstantDoublesCurve.from(0)), VOL2, TODAY, SPEED1, SPEED2, U, YieldCurve.from(ConstantDoublesCurve
        .from(F)), RHO);
    assertEquals(MODEL.getDiscountBondFunction(START, MATURITY).evaluate(data), 0, EPS);
    data = new HullWhiteTwoFactorDataBundle(R, VOL1, VOL2, TODAY, SPEED1, SPEED2, U, YieldCurve.from(ConstantDoublesCurve.from(F)), RHO);
    assertEquals(MODEL.getDiscountBondFunction(START, START).evaluate(data), 1, EPS);
  }
}
