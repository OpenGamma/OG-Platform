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
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.MertonJumpDiffusionModelDataBundle;
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
public class MertonJumpDiffusionModelTest {
  private static final AnalyticOptionModel<OptionDefinition, MertonJumpDiffusionModelDataBundle> MODEL = new MertonJumpDiffusionModel();
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
    MODEL.getPricingFunction(new EuropeanVanillaOptionDefinition(100, EXPIRY1, true)).evaluate((MertonJumpDiffusionModelDataBundle) null);
  }

  @Test
  public void test() {
    OptionDefinition call = new EuropeanVanillaOptionDefinition(80, EXPIRY1, true);
    MertonJumpDiffusionModelDataBundle data = new MertonJumpDiffusionModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, 1., 0.);
    assertEquals(BSM.getPricingFunction(call).evaluate(data), MODEL.getPricingFunction(call).evaluate(data), EPS2);
    call = new EuropeanVanillaOptionDefinition(80, EXPIRY1, true);
    data = data.withLambda(1.).withGamma(0.25);
    assertEquals(20.67, MODEL.getPricingFunction(call).evaluate(data), EPS1);
    call = new EuropeanVanillaOptionDefinition(90, EXPIRY2, true);
    data = data.withLambda(5.);
    assertEquals(12.75, MODEL.getPricingFunction(call).evaluate(data), EPS1);
    call = new EuropeanVanillaOptionDefinition(100, EXPIRY3, true);
    data = data.withLambda(10.);
    assertEquals(9.03, MODEL.getPricingFunction(call).evaluate(data), EPS1);
    data = data.withGamma(0.5);
    data = data.withLambda(1.);
    call = new EuropeanVanillaOptionDefinition(90, EXPIRY1, true);
    assertEquals(11.04, MODEL.getPricingFunction(call).evaluate(data), EPS1);
    call = new EuropeanVanillaOptionDefinition(100, EXPIRY2, true);
    data = data.withLambda(5.);
    assertEquals(5.87, MODEL.getPricingFunction(call).evaluate(data), EPS1);
    call = new EuropeanVanillaOptionDefinition(110, EXPIRY3, true);
    data = data.withLambda(10.);
    assertEquals(4.71, MODEL.getPricingFunction(call).evaluate(data), EPS1);
    data = data.withGamma(0.75);
    data = data.withLambda(1.);
    call = new EuropeanVanillaOptionDefinition(100, EXPIRY1, true);
    assertEquals(2.70, MODEL.getPricingFunction(call).evaluate(data), EPS1);
    call = new EuropeanVanillaOptionDefinition(110, EXPIRY2, true);
    data = data.withLambda(5.);
    assertEquals(2.05, MODEL.getPricingFunction(call).evaluate(data), EPS1);
    call = new EuropeanVanillaOptionDefinition(120, EXPIRY3, true);
    data = data.withLambda(10.);
    assertEquals(2.23, MODEL.getPricingFunction(call).evaluate(data), EPS1);
  }
}
