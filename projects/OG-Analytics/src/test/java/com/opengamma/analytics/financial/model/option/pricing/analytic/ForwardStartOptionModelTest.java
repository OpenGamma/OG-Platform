/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.Moneyness;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.ForwardStartOptionDefinition;
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
public class ForwardStartOptionModelTest {
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final double R = 0.08;
  private static final YieldCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(R));
  private static final VolatilitySurface SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(0.3));
  private static final double B = 0.04;
  private static final double SPOT = 60;
  private static final double PERCENT = 0.1;
  private static final ZonedDateTime START = DateUtils.getDateOffsetWithYearFraction(DATE, 0.25);
  private static final ZonedDateTime EXPIRY = DateUtils.getDateOffsetWithYearFraction(DATE, 1);
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE);
  private static final ForwardStartOptionDefinition FORWARD = new ForwardStartOptionDefinition(new Expiry(EXPIRY), true, new Expiry(START), PERCENT, Moneyness.OTM);
  private static final ForwardStartOptionDefinition NOW = new ForwardStartOptionDefinition(new Expiry(EXPIRY), true, new Expiry(DATE), PERCENT, Moneyness.OTM);
  private static final ForwardStartOptionDefinition END = new ForwardStartOptionDefinition(new Expiry(EXPIRY), true, new Expiry(EXPIRY), PERCENT, Moneyness.OTM);
  private static final EuropeanVanillaOptionDefinition VANILLA = new EuropeanVanillaOptionDefinition(SPOT * (1 + PERCENT), new Expiry(EXPIRY), true);
  private static final AnalyticOptionModel<ForwardStartOptionDefinition, StandardOptionDataBundle> MODEL = new ForwardStartOptionModel();
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getPricingFunction(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getPricingFunction(FORWARD).evaluate((StandardOptionDataBundle) null);
  }

  @Test
  public void test() {
    assertEquals(MODEL.getPricingFunction(END).evaluate(DATA), 0, 0);
    assertEquals(MODEL.getPricingFunction(FORWARD).evaluate(DATA.withVolatilitySurface(new VolatilitySurface(ConstantDoublesSurface.from(1e-9)))), 0, 0);
    assertEquals(MODEL.getPricingFunction(NOW).evaluate(DATA), BSM.getPricingFunction(VANILLA).evaluate(DATA), 1e-4);
    assertEquals(MODEL.getPricingFunction(FORWARD).evaluate(DATA), 4.4064, 1e-4);
  }
}
