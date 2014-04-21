/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.curve;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.definition.FXOptionDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class FXVannaVolgaVolatilityCurveModelTest {
  private static final YieldAndDiscountCurve DOMESTIC = DiscountCurve.from(ConstantDoublesCurve.from(0.9902752));
  private static final YieldAndDiscountCurve FOREIGN = DiscountCurve.from(ConstantDoublesCurve.from(0.9945049));
  private static final double SPOT = 1.205;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final ZonedDateTime MATURITY = DateUtils.getDateOffsetWithYearFraction(DATE, 94. / 365);
  private static final double RR = -0.005;
  private static final double ATM = 0.0905;
  private static final double VWB = 0.0013;
  private static final FXOptionDataBundle DATA = new FXOptionDataBundle(DOMESTIC, FOREIGN, new VolatilitySurface(ConstantDoublesSurface.from(ATM)), SPOT, DATE);
  private static final FXVannaVolgaVolatilityCurveDataBundle MARKET_DATA = new FXVannaVolgaVolatilityCurveDataBundle(0.25, RR, ATM, VWB, MATURITY);
  private static final FXVannaVolgaVolatilityCurveModel MODEL = new FXVannaVolgaVolatilityCurveModel();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMarketQuotes() {
    MODEL.getCurve(null, DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getCurve(MARKET_DATA, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPair() {
    MODEL.getCurve(MARKET_DATA, DATA).getVolatility(null);
  }

  @Test
  public void test() {
    final VolatilityCurve curve = MODEL.getCurve(MARKET_DATA, DATA);
    assertEquals(curve.getVolatility(1.1733), 0.0943, 1e-4);
    assertEquals(curve.getVolatility(1.2114), 0.0905, 1e-4);
    assertEquals(curve.getVolatility(1.2487), 0.0893, 1e-4);
    assertEquals(curve.getVolatility(1e-7), curve.getVolatility(1e-4), 1e-4);
    assertEquals(curve.getVolatility(5.), curve.getVolatility(6.), 1e-4);
  }
}
