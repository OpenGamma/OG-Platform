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
import com.opengamma.analytics.financial.model.option.definition.BatesGeneralizedJumpDiffusionModelDataBundle;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
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
public class BatesGeneralizedJumpDiffusionModelTest {
  private static final AnalyticOptionModel<OptionDefinition, BatesGeneralizedJumpDiffusionModelDataBundle> MODEL = new BatesGeneralizedJumpDiffusionModel();
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();
  private static final YieldAndDiscountCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(0.08));
  private static final double B = 0.08;
  private static final VolatilitySurface SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(0.25));
  private static final double SPOT = 100;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2009, 1, 1);
  private static final Expiry EXPIRY1 = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.1));
  private static final Expiry EXPIRY2 = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.25));
  private static final Expiry EXPIRY3 = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.5));
  private static final double EPS1 = 1e-2;
  private static final double EPS2 = 1e-9;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getPricingFunction(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getPricingFunction(new EuropeanVanillaOptionDefinition(100, EXPIRY1, true)).evaluate((BatesGeneralizedJumpDiffusionModelDataBundle) null);
  }

  @Test
  public void test() {
    OptionDefinition call = new EuropeanVanillaOptionDefinition(80, EXPIRY1, true);
    BatesGeneralizedJumpDiffusionModelDataBundle data = new BatesGeneralizedJumpDiffusionModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, 0., -0.04, 0.);
    assertEquals(BSM.getPricingFunction(call).evaluate(data), MODEL.getPricingFunction(call).evaluate(data), EPS2);
    call = new EuropeanVanillaOptionDefinition(80, EXPIRY1, true);
    data = data.withLambda(1.).withDelta(0.1);
    assertEquals(20.67, MODEL.getPricingFunction(call).evaluate(data), EPS1);
    call = new EuropeanVanillaOptionDefinition(90, EXPIRY2, true);
    data = data.withLambda(5.);
    assertEquals(14.13, MODEL.getPricingFunction(call).evaluate(data), EPS1);
    call = new EuropeanVanillaOptionDefinition(100, EXPIRY3, true);
    data = data.withLambda(10.);
    assertEquals(13.62, MODEL.getPricingFunction(call).evaluate(data), EPS1);
    data = data.withDelta(0.25);
    data = data.withLambda(1.);
    call = new EuropeanVanillaOptionDefinition(90, EXPIRY1, true);
    assertEquals(11.57, MODEL.getPricingFunction(call).evaluate(data), EPS1);
    call = new EuropeanVanillaOptionDefinition(100, EXPIRY2, true);
    data = data.withLambda(5.);
    assertEquals(12.25, MODEL.getPricingFunction(call).evaluate(data), EPS1);
    call = new EuropeanVanillaOptionDefinition(110, EXPIRY3, true);
    data = data.withLambda(10.);
    assertEquals(20.43, MODEL.getPricingFunction(call).evaluate(data), EPS1);
    data = data.withDelta(0.5);
    data = data.withLambda(1.);
    call = new EuropeanVanillaOptionDefinition(100, EXPIRY1, true);
    assertEquals(5.18, MODEL.getPricingFunction(call).evaluate(data), EPS1);
    call = new EuropeanVanillaOptionDefinition(110, EXPIRY2, true);
    data = data.withLambda(5.);
    assertEquals(16.52, MODEL.getPricingFunction(call).evaluate(data), EPS1);
    call = new EuropeanVanillaOptionDefinition(120, EXPIRY3, true);
    data = data.withLambda(10.);
    assertEquals(37.03, MODEL.getPricingFunction(call).evaluate(data), EPS1);
  }
}
