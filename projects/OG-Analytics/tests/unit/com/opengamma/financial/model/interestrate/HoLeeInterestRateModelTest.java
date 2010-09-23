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
import com.opengamma.financial.model.interestrate.definition.StandardDiscountBondModelDataBundle;
import com.opengamma.financial.model.volatility.curve.ConstantVolatilityCurve;
import com.opengamma.financial.model.volatility.curve.VolatilityCurve;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class HoLeeInterestRateModelTest {
  private static final int YEARS = 10;
  private static final HoLeeInterestRateModel MODEL = new HoLeeInterestRateModel();
  private static final ZonedDateTime TODAY = DateUtil.getUTCDate(2010, 7, 1);
  private static final ZonedDateTime START = DateUtil.getUTCDate(2011, 7, 1);
  private static final ZonedDateTime MATURITY = DateUtil.getDateOffsetWithYearFraction(START, 10);
  private static final double IR = 0.05;
  private static final YieldAndDiscountCurve R = new ConstantYieldCurve(IR);
  private static final double VOL = 0.1;
  private static final VolatilityCurve SIGMA = new ConstantVolatilityCurve(VOL);
  private static final StandardDiscountBondModelDataBundle DATA = new StandardDiscountBondModelDataBundle(R, SIGMA, TODAY);

  @Test(expected = IllegalArgumentException.class)
  public void testNullTime() {
    MODEL.getDiscountBondFunction(null, MATURITY);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullMaturity() {
    MODEL.getDiscountBondFunction(START, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getDiscountBondFunction(START, MATURITY).evaluate((StandardDiscountBondModelDataBundle) null);
  }

  @Test
  public void test() {
    final double eps = 1e-9;
    assertEquals(MODEL.getDiscountBondFunction(START, START).evaluate(DATA), 1, 0);
    StandardDiscountBondModelDataBundle data = new StandardDiscountBondModelDataBundle(new ConstantYieldCurve(0.), new ConstantVolatilityCurve(0), TODAY);
    assertEquals(MODEL.getDiscountBondFunction(START, MATURITY).evaluate(data), 1, 0);
    data = new StandardDiscountBondModelDataBundle(new ConstantYieldCurve(0.), SIGMA, TODAY);
    assertEquals(MODEL.getDiscountBondFunction(START, MATURITY).evaluate(data), Math.exp(-0.5 * VOL * VOL * YEARS * YEARS), 0);
    data = new StandardDiscountBondModelDataBundle(R, new ConstantVolatilityCurve(0), TODAY);
    assertEquals(MODEL.getDiscountBondFunction(START, MATURITY).evaluate(data), Math.exp(-IR * YEARS), eps);
  }
}
