/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorInterestRateDataBundle;
import com.opengamma.financial.model.volatility.curve.ConstantVolatilityCurve;
import com.opengamma.financial.model.volatility.curve.VolatilityCurve;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class HullWhiteOneFactorInterestRateModelTest {
  private static final double YEARS = 11.3;
  private static final double T = 1.23;
  private static final ZonedDateTime TODAY = DateUtil.getUTCDate(2010, 8, 1);
  private static final ZonedDateTime START = DateUtil.getDateOffsetWithYearFraction(TODAY, T);
  private static final ZonedDateTime MATURITY = DateUtil.getDateOffsetWithYearFraction(START, YEARS);
  private static final double RATE = 0.056;
  private static final double VOL = 0.01;
  private static final double SPEED = 0.13;
  private static final YieldAndDiscountCurve R = new ConstantYieldCurve(RATE);
  private static final VolatilityCurve SIGMA = new ConstantVolatilityCurve(VOL);
  private static final HullWhiteOneFactorInterestRateModel MODEL = new HullWhiteOneFactorInterestRateModel();
  private static final double EPS = 1e-8;

  @Test(expected = IllegalArgumentException.class)
  public void testNullTime() {
    MODEL.getInterestRateFunction(null, MATURITY);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullMaturity() {
    MODEL.getInterestRateFunction(START, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getInterestRateFunction(START, MATURITY).evaluate((HullWhiteOneFactorInterestRateDataBundle) null);
  }

  @Test
  public void test() {
    HullWhiteOneFactorInterestRateDataBundle data = new HullWhiteOneFactorInterestRateDataBundle(R, SIGMA, TODAY, SPEED);
    assertEquals(MODEL.getInterestRateFunction(START, START).evaluate(data), 1, EPS);
    data = new HullWhiteOneFactorInterestRateDataBundle(R, new ConstantVolatilityCurve(0), TODAY, SPEED);
    assertEquals(Math.log(MODEL.getInterestRateFunction(START, MATURITY).evaluate(data)), -RATE * YEARS, EPS);
    data = new HullWhiteOneFactorInterestRateDataBundle(R, SIGMA, TODAY, 200);
    assertEquals(Math.log(MODEL.getInterestRateFunction(START, MATURITY).evaluate(data)), -RATE * YEARS, EPS);
  }
}
