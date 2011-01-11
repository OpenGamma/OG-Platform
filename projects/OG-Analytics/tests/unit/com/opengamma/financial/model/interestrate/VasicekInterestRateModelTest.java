/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.interestrate.definition.VasicekDataBundle;
import com.opengamma.financial.model.volatility.curve.VolatilityCurve;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class VasicekInterestRateModelTest {
  private static final int YEARS = 10;
  private static final ZonedDateTime TODAY = DateUtil.getUTCDate(2010, 7, 1);
  private static final ZonedDateTime START = DateUtil.getUTCDate(2011, 7, 1);
  private static final ZonedDateTime MATURITY = DateUtil.getDateOffsetWithYearFraction(START, 10);
  private static final VasicekInterestRateModel MODEL = new VasicekInterestRateModel();

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
    MODEL.getDiscountBondFunction(START, MATURITY).evaluate((VasicekDataBundle) null);
  }

  @Test
  public void test() {
    final YieldCurve shortRate = new YieldCurve(ConstantDoublesCurve.from(0.05));
    final double longRate = 0.06;
    final double speed = 0.01;
    final VolatilityCurve sigma = new VolatilityCurve(ConstantDoublesCurve.from(0.01));
    VasicekDataBundle data = new VasicekDataBundle(shortRate, sigma, TODAY, longRate, speed);
    assertEquals(MODEL.getDiscountBondFunction(START, START).evaluate(data), 1, 0);
    data = new VasicekDataBundle(shortRate, new VolatilityCurve(ConstantDoublesCurve.from(0)), TODAY, longRate, speed);
    final double factor = (1 - Math.exp(-speed * YEARS));
    final double lnA = longRate * (factor / speed - YEARS);
    assertEquals(MODEL.getDiscountBondFunction(START, MATURITY).evaluate(data), Math.exp(lnA - data.getShortRate(0) * factor / speed), 0);
  }
}
