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
import com.opengamma.analytics.financial.model.interestrate.definition.VasicekDataBundle;
import com.opengamma.analytics.financial.model.volatility.curve.VolatilityCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class VasicekInterestRateModelTest {
  private static final int YEARS = 10;
  private static final ZonedDateTime TODAY = DateUtils.getUTCDate(2010, 7, 1);
  private static final ZonedDateTime START = DateUtils.getUTCDate(2011, 7, 1);
  private static final ZonedDateTime MATURITY = DateUtils.getDateOffsetWithYearFraction(START, 10);
  private static final VasicekInterestRateModel MODEL = new VasicekInterestRateModel();

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
    MODEL.getDiscountBondFunction(START, MATURITY).evaluate((VasicekDataBundle) null);
  }

  @Test
  public void test() {
    final YieldCurve shortRate = YieldCurve.from(ConstantDoublesCurve.from(0.05));
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
