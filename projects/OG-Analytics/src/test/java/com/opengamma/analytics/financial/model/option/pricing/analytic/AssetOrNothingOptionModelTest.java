/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.AssetOrNothingOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class AssetOrNothingOptionModelTest {
  private static final double R = 0.07;
  private static final YieldAndDiscountCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(R));
  private static final double B = 0.02;
  private static final VolatilitySurface SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(0.27));
  private static final double SPOT = 70;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final double STRIKE = 65;
  private static final double T = 0.5;
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, T));
  private static final AssetOrNothingOptionDefinition PUT = new AssetOrNothingOptionDefinition(STRIKE, EXPIRY, false);
  private static final AssetOrNothingOptionDefinition CALL = new AssetOrNothingOptionDefinition(STRIKE, EXPIRY, true);
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE);
  private static final AnalyticOptionModel<AssetOrNothingOptionDefinition, StandardOptionDataBundle> MODEL = new AssetOrNothingOptionModel();
  private static final double EPS = 1e-12;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getPricingFunction(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getPricingFunction(PUT).evaluate((StandardOptionDataBundle) null);
  }

  @Test
  public void testZeroVol() {
    StandardOptionDataBundle data = DATA.withVolatilitySurface(new VolatilitySurface(ConstantDoublesSurface.from(0)));
    final double df = Math.exp(T * (B - R));
    assertEquals(MODEL.getPricingFunction(CALL).evaluate(data), df * SPOT, EPS);
    assertEquals(MODEL.getPricingFunction(PUT).evaluate(data), 0, 0);
    data = data.withSpot(60);
    assertEquals(MODEL.getPricingFunction(CALL).evaluate(data), 0, 0);
    assertEquals(MODEL.getPricingFunction(PUT).evaluate(data), df * 60, EPS);
  }

  @Test
  public void test() {
    assertEquals(MODEL.getPricingFunction(PUT).evaluate(DATA), 20.2069, 1e-4);
  }
}
