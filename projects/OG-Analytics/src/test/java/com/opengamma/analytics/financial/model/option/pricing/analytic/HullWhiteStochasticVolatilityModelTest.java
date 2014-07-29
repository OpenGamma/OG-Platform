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
import com.opengamma.analytics.financial.model.option.definition.HullWhiteStochasticVolatilityModelDataBundle;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
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
public class HullWhiteStochasticVolatilityModelTest {
  private static final AnalyticOptionModel<OptionDefinition, HullWhiteStochasticVolatilityModelDataBundle> MODEL = new HullWhiteStochasticVolatilityModel();
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2009, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.25));
  private static final YieldAndDiscountCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(0.08));
  private static final double B = 0;
  private static final VolatilitySurface SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(Math.sqrt(0.09)));
  private static final double SPOT = 100;
  private static final double LAMBDA = 0.1;
  private static final double SIGMA_LR = Math.sqrt(0.0625);
  private static final double VOL_OF_VOL = 0.5;
  private static final double EPS = 1e-4;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getPricingFunction(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getPricingFunction(new EuropeanVanillaOptionDefinition(100, EXPIRY, true)).evaluate((HullWhiteStochasticVolatilityModelDataBundle) null);
  }

  @Test
  public void test() {
    HullWhiteStochasticVolatilityModelDataBundle data = new HullWhiteStochasticVolatilityModelDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, -0.75);
    OptionDefinition definition = new EuropeanVanillaOptionDefinition(70, EXPIRY, false);
    assertModel(0.0904, definition, data);
    data = data.withCorrelation(-0.5);
    definition = new EuropeanVanillaOptionDefinition(80, EXPIRY, false);
    assertModel(0.4278, definition, data);
    data = data.withCorrelation(-0.25);
    definition = new EuropeanVanillaOptionDefinition(90, EXPIRY, false);
    assertModel(1.6982, definition, data);
    data = data.withCorrelation(0.);
    definition = new EuropeanVanillaOptionDefinition(100, EXPIRY, false);
    assertModel(5.3061, definition, data);
    definition = new EuropeanVanillaOptionDefinition(100, EXPIRY, true);
    assertModel(5.3061, definition, data);
    data = data.withCorrelation(0.25);
    definition = new EuropeanVanillaOptionDefinition(110, EXPIRY, true);
    assertModel(2.1274, definition, data);
    data = data.withCorrelation(0.5);
    definition = new EuropeanVanillaOptionDefinition(120, EXPIRY, true);
    assertModel(0.8881, definition, data);
    data = data.withCorrelation(0.75);
    definition = new EuropeanVanillaOptionDefinition(130, EXPIRY, true);
    assertModel(0.4287, definition, data);
  }

  private void assertModel(final double value, final OptionDefinition definition, final HullWhiteStochasticVolatilityModelDataBundle data) {
    assertEquals(value, MODEL.getPricingFunction(definition).evaluate(data), EPS);
  }
}
