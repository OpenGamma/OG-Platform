/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.HullWhiteStochasticVolatilityModelOptionDataBundle;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.tuple.DoublesPair;

public class HullWhiteStochasticVolatilityModelTest {
  private static final AnalyticOptionModel<OptionDefinition, HullWhiteStochasticVolatilityModelOptionDataBundle> MODEL = new HullWhiteStochasticVolatilityModel();
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2009, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.25));
  private static final YieldAndDiscountCurve CURVE = new ConstantYieldCurve(0.08);
  private static final double B = 0;
  private static final VolatilitySurface SURFACE = new ConstantVolatilitySurface(Math.sqrt(0.09));
  private static final double SPOT = 100;
  private static final double LAMBDA = 0.1;
  private static final double SIGMA_LR = Math.sqrt(0.0625);
  private static final double VOL_OF_VOL = 0.5;
  private static final double EPS = 1e-4;

  @Test(expected = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getPricingFunction(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getPricingFunction(new EuropeanVanillaOptionDefinition(100, EXPIRY, true)).evaluate((HullWhiteStochasticVolatilityModelOptionDataBundle) null);
  }

  @Test
  public void test() {
    HullWhiteStochasticVolatilityModelOptionDataBundle data = new HullWhiteStochasticVolatilityModelOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, SIGMA_LR, VOL_OF_VOL, -0.75);
    @SuppressWarnings("unused")
    OptionDefinition definition = new EuropeanVanillaOptionDefinition(70, EXPIRY, false);
    // test(0.0904, definition, data);
    data = data.withCorrelation(-0.5);
    definition = new EuropeanVanillaOptionDefinition(80, EXPIRY, false);
    // test(0.4278, definition, data);
    data = data.withCorrelation(-0.25);
    definition = new EuropeanVanillaOptionDefinition(90, EXPIRY, false);
    // test(1.6982, definition, data);
    data = data.withCorrelation(0.);
    definition = new EuropeanVanillaOptionDefinition(100, EXPIRY, false);
    // test(5.3061, definition, data);
    definition = new EuropeanVanillaOptionDefinition(100, EXPIRY, true);
    // test(5.3061, definition, data);
    data = data.withCorrelation(0.25);
    definition = new EuropeanVanillaOptionDefinition(110, EXPIRY, true);
    // test(2.1274, definition, data);
    data = data.withCorrelation(0.5);
    definition = new EuropeanVanillaOptionDefinition(120, EXPIRY, true);
    // test(0.8881, definition, data);
    data = data.withCorrelation(0.75);
    definition = new EuropeanVanillaOptionDefinition(130, EXPIRY, true);
    // test(0.4287, definition, data);
  }

  @SuppressWarnings("unused")
  private void test(final double value, final OptionDefinition definition, final HullWhiteStochasticVolatilityModelOptionDataBundle data) {
    final HullWhiteStochasticVolatilityModelOptionDataBundle bsmEquivalent = new HullWhiteStochasticVolatilityModelOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE, LAMBDA, SURFACE
        .getVolatility(DoublesPair.of(0., 0.)), VOL_OF_VOL, 0.);
    assertEquals(value, MODEL.getPricingFunction(definition).evaluate(data), EPS);
    assertEquals(BSM.getPricingFunction(definition).evaluate(bsmEquivalent), MODEL.getPricingFunction(definition).evaluate(bsmEquivalent), EPS);
  }
}
