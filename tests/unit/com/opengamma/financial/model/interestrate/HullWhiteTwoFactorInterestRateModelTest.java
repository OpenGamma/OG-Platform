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
import com.opengamma.financial.model.interestrate.definition.HullWhiteTwoFactorInterestRateDataBundle;
import com.opengamma.financial.model.volatility.curve.ConstantVolatilityCurve;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class HullWhiteTwoFactorInterestRateModelTest {
  private static final double RATE = 0.04;
  private static final double SIGMA1 = 0.1;
  private static final double SIGMA2 = 0.15;
  private static final double T1 = 1.4;
  private static final double T2 = 16;
  private static final ZonedDateTime TODAY = DateUtil.getUTCDate(2010, 8, 1);
  private static final ZonedDateTime START = DateUtil.getDateOffsetWithYearFraction(TODAY, T1);
  private static final ZonedDateTime MATURITY = DateUtil.getDateOffsetWithYearFraction(START, T2);
  private static final ConstantYieldCurve R = new ConstantYieldCurve(RATE);
  private static final ConstantVolatilityCurve VOL1 = new ConstantVolatilityCurve(SIGMA1);
  private static final ConstantVolatilityCurve VOL2 = new ConstantVolatilityCurve(SIGMA2);
  private static final double SPEED1 = 0.2;
  private static final double SPEED2 = 0.07;
  private static final double U = 0.13;
  private static final double F = 0.17;
  private static final double RHO = 0.43;
  private static final HullWhiteTwoFactorInterestRateModel MODEL = new HullWhiteTwoFactorInterestRateModel();
  private static final double EPS = 1e-9;

  @Test(expected = IllegalArgumentException.class)
  public void testNullDate() {
    MODEL.getInterestRateFunction(null, MATURITY);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullMaturity() {
    MODEL.getInterestRateFunction(START, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getInterestRateFunction(START, MATURITY).evaluate((HullWhiteTwoFactorInterestRateDataBundle) null);
  }

  @Test
  public void test() {
    HullWhiteTwoFactorInterestRateDataBundle data = new HullWhiteTwoFactorInterestRateDataBundle(R, new ConstantVolatilityCurve(0), VOL2, TODAY, SPEED1, SPEED2, U, F, RHO);
    assertEquals(MODEL.getInterestRateFunction(START, MATURITY).evaluate(data), 0, EPS);
    data = new HullWhiteTwoFactorInterestRateDataBundle(R, VOL1, VOL2, TODAY, SPEED1, SPEED2, U, F, RHO);
    assertEquals(MODEL.getInterestRateFunction(START, START).evaluate(data), 1, EPS);
  }
}
