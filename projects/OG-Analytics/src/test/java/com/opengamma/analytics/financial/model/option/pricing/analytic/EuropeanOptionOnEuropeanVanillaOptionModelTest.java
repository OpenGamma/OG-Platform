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
import com.opengamma.analytics.financial.model.option.definition.EuropeanOptionOnEuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
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
public class EuropeanOptionOnEuropeanVanillaOptionModelTest {
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final double UNDERLYING_STRIKE = 520;
  private static final Expiry UNDERLYING_EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.5));
  private static final EuropeanVanillaOptionDefinition UNDERLYING = new EuropeanVanillaOptionDefinition(UNDERLYING_STRIKE, UNDERLYING_EXPIRY, true);
  private static final double STRIKE = 50;
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.25));
  private static final EuropeanOptionOnEuropeanVanillaOptionDefinition OPTION = new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, false, UNDERLYING);
  private static final EuropeanOptionOnEuropeanVanillaOptionModel MODEL = new EuropeanOptionOnEuropeanVanillaOptionModel();
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.08)), 0.05, new VolatilitySurface(ConstantDoublesSurface.from(0.35)),
      500, DATE);
  private static final BlackScholesMertonModel BSM = new BlackScholesMertonModel();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getPricingFunction(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getPricingFunction(OPTION).evaluate((StandardOptionDataBundle) null);
  }

  @Test
  public void test() {
    assertEquals(MODEL.getPricingFunction(OPTION).evaluate(DATA), 21.196, 1e-3);
    final EuropeanVanillaOptionDefinition call = new EuropeanVanillaOptionDefinition(UNDERLYING_STRIKE, UNDERLYING_EXPIRY, true);
    final EuropeanVanillaOptionDefinition put = new EuropeanVanillaOptionDefinition(UNDERLYING_STRIKE, EXPIRY, false);
    final EuropeanOptionOnEuropeanVanillaOptionDefinition callOnCall = new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, true, call);
    final EuropeanOptionOnEuropeanVanillaOptionDefinition putOnCall = new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, false, call);
    assertEquals(MODEL.getPricingFunction(callOnCall).evaluate(DATA) - MODEL.getPricingFunction(putOnCall).evaluate(DATA),
        BSM.getPricingFunction(call).evaluate(DATA) - STRIKE * Math.exp(-0.08 * 0.25), 1e-3);
    final EuropeanOptionOnEuropeanVanillaOptionDefinition callOnPut = new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, true, put);
    final EuropeanOptionOnEuropeanVanillaOptionDefinition putOnPut = new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, false, put);
    assertEquals(MODEL.getPricingFunction(callOnPut).evaluate(DATA) - MODEL.getPricingFunction(putOnPut).evaluate(DATA), BSM.getPricingFunction(put).evaluate(DATA) - STRIKE * Math.exp(-0.08 * 0.25),
        1e-3);
  }
}
