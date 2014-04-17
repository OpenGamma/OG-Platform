/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorDataBundle;
import com.opengamma.analytics.financial.model.volatility.curve.VolatilityCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class HullWhiteOneFactorInterestRateModelTest {
  private static final double YEARS = 11.3;
  private static final double T = 1.23;
  private static final ZonedDateTime TODAY = DateUtils.getUTCDate(2010, 8, 1);
  private static final ZonedDateTime START = DateUtils.getDateOffsetWithYearFraction(TODAY, T);
  private static final ZonedDateTime MATURITY = DateUtils.getDateOffsetWithYearFraction(START, YEARS);
  private static final double RATE = 0.056;
  private static final double VOL = 0.01;
  private static final double SPEED = 0.13;
  private static final YieldAndDiscountCurve R = YieldCurve.from(ConstantDoublesCurve.from(RATE));
  private static final VolatilityCurve SIGMA = new VolatilityCurve(ConstantDoublesCurve.from(VOL));
  private static final HullWhiteOneFactorInterestRateModel MODEL = new HullWhiteOneFactorInterestRateModel();
  private static final double EPS = 1e-8;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTime() {
    MODEL.getDiscountBondFunction(null, MATURITY);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMaturity() {
    MODEL.getDiscountBondFunction(START, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getDiscountBondFunction(START, MATURITY).evaluate((HullWhiteOneFactorDataBundle) null);
  }

  @Test
  public void test() {
    HullWhiteOneFactorDataBundle data = new HullWhiteOneFactorDataBundle(R, SIGMA, TODAY, SPEED);
    assertEquals(MODEL.getDiscountBondFunction(START, START).evaluate(data), 1, EPS);
    data = new HullWhiteOneFactorDataBundle(R, new VolatilityCurve(ConstantDoublesCurve.from(0)), TODAY, SPEED);
    assertEquals(Math.log(MODEL.getDiscountBondFunction(START, MATURITY).evaluate(data)), -RATE * YEARS, EPS);
    data = new HullWhiteOneFactorDataBundle(R, SIGMA, TODAY, 200);
    assertEquals(Math.log(MODEL.getDiscountBondFunction(START, MATURITY).evaluate(data)), -RATE * YEARS, EPS);
  }
}
