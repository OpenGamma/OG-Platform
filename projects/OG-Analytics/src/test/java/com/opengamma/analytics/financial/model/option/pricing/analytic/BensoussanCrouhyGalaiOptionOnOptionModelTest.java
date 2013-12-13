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
public class BensoussanCrouhyGalaiOptionOnOptionModelTest {
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final double SPOT = 500;
  private static final double UNDERLYING_STRIKE = 520;
  private static final Expiry UNDERLYING_EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.5));
  private static final EuropeanVanillaOptionDefinition UNDERLYING = new EuropeanVanillaOptionDefinition(UNDERLYING_STRIKE, UNDERLYING_EXPIRY, true);
  private static final double STRIKE = 50;
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.25));
  private static final EuropeanOptionOnEuropeanVanillaOptionDefinition OPTION = new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, false, UNDERLYING);
  private static final BensoussanCrouhyGalaiOptionOnOptionModel BCG = new BensoussanCrouhyGalaiOptionOnOptionModel();
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.08)), 0.05, new VolatilitySurface(ConstantDoublesSurface.from(0.35)),
      SPOT, DATE);
  private static final EuropeanOptionOnEuropeanVanillaOptionModel MODEL = new EuropeanOptionOnEuropeanVanillaOptionModel();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    BCG.getPricingFunction(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    BCG.getPricingFunction(OPTION).evaluate((StandardOptionDataBundle) null);
  }

  @Test
  public void test() {
    assertEquals(BCG.getPricingFunction(OPTION).evaluate(DATA), 19.9147, 1e-4);
    final EuropeanVanillaOptionDefinition underlying = new EuropeanVanillaOptionDefinition(SPOT - 100, UNDERLYING_EXPIRY, true);
    final EuropeanOptionOnEuropeanVanillaOptionDefinition option = new EuropeanOptionOnEuropeanVanillaOptionDefinition(20, EXPIRY, true, underlying);
    assertEquals(BCG.getPricingFunction(option).evaluate(DATA) / MODEL.getPricingFunction(option).evaluate(DATA), 1, 1e-2);
  }
}
