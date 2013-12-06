/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
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
public class BlackScholesMertonModelTest extends AnalyticOptionModelTest {
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2009, 1, 1);
  private static final Expiry ONE_YEAR = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 1));
  private static final Expiry NINE_MONTHS = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.75));
  private static final Expiry SIX_MONTHS = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.5));
  private static final Expiry THREE_MONTHS = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.25));
  private static final Expiry EIGHT_DAYS = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 8. / 365));
  private static final Expiry ONE_MONTH = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 1. / 12));
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> MODEL = new BlackScholesMertonModel();
  private static final double EPS = 1e-4;

  @Test
  public void testInputs() {
    final OptionDefinition definition = new EuropeanVanillaOptionDefinition(1., EIGHT_DAYS, true);
    super.assertInputs(MODEL, definition);
  }

  @Test
  public void testModels() {
    final Set<Greek> greeks = Collections.singleton(Greek.FAIR_PRICE);
    assertPrices(greeks, 65, THREE_MONTHS, true, 0.08, 0.08, 0.3, 60, getCollection(Greek.FAIR_PRICE, 2.1334));
    assertPrices(greeks, 95, SIX_MONTHS, false, 0.1, 0.05, 0.2, 100, getCollection(Greek.FAIR_PRICE, 2.4648));
    assertPrices(greeks, 19, NINE_MONTHS, true, 0.1, 0, 0.28, 19, getCollection(Greek.FAIR_PRICE, 1.7011));
    assertPrices(greeks, 19, NINE_MONTHS, false, 0.1, 0, 0.28, 19, getCollection(Greek.FAIR_PRICE, 1.7011));
    assertPrices(greeks, 3800, NINE_MONTHS, false, 0, 0, 0.15, 4200, getCollection(Greek.FAIR_PRICE, 65.6185));
    assertPrices(greeks, 1.6, SIX_MONTHS, true, 0.06, -0.02, 0.12, 1.56, getCollection(Greek.FAIR_PRICE, 0.0291));
  }

  @Test
  public void testGreeks() {
    assertGreek(Greek.DELTA, 100, SIX_MONTHS, true, 0.1, 0, 0.36, 105, getCollection(Greek.DELTA, 0.5946));
    assertGreek(Greek.DELTA, 100, SIX_MONTHS, false, 0.1, 0, 0.36, 105, getCollection(Greek.DELTA, -0.3566));
    assertGreek(Greek.VANNA, 80, THREE_MONTHS, false, 0.05, 0.05, 0.2, 90, getCollection(Greek.VANNA, -1.0008));
    assertGreek(Greek.DELTA_BLEED, 90, THREE_MONTHS, false, 0.14, 0, 0.24, 105, getCollection(Greek.DELTA_BLEED, 0.37));
    assertGreek(Greek.ELASTICITY, 100, SIX_MONTHS, false, 0.1, 0, 0.36, 105, getCollection(Greek.ELASTICITY, -4.8775));
    assertGreek(Greek.GAMMA, 60, NINE_MONTHS, false, 0.1, 0.1, 0.3, 55, getCollection(Greek.GAMMA, 0.0278));
    assertGreek(Greek.GAMMA, 60, NINE_MONTHS, true, 0.1, 0.1, 0.3, 55, getCollection(Greek.GAMMA, 0.0278));
    assertGreek(Greek.GAMMA_P, 50, EIGHT_DAYS, false, 0.12, 0.12, 0.15, 50, getCollection(Greek.GAMMA_P, 0.1781));
    assertGreek(Greek.GAMMA_P, 50, EIGHT_DAYS, false, 0.12, 0.12, 0.15, 50, getCollection(Greek.GAMMA_P, 0.1781));
    assertGreek(Greek.ZOMMA, 80, THREE_MONTHS, false, 0.05, 0, 0.26, 100, getCollection(Greek.ZOMMA, 0.0463));
    assertGreek(Greek.ZOMMA_P, 80, THREE_MONTHS, false, 0.05, 0, 0.26, 100, getCollection(Greek.ZOMMA_P, 0.0463));
    assertGreek(Greek.SPEED, 48, ONE_MONTH, false, 0.06, 0.01, 0.2, 50, getCollection(Greek.SPEED, -0.0291));
    assertGreek(Greek.SPEED, 48, ONE_MONTH, true, 0.06, 0.01, 0.2, 50, getCollection(Greek.SPEED, -0.0291));
    assertGreek(Greek.SPEED_P, 48, ONE_MONTH, false, 0.06, 0.01, 0.2, 50, getCollection(Greek.SPEED_P, -0.0135));
    assertGreek(Greek.SPEED_P, 48, ONE_MONTH, true, 0.06, 0.01, 0.2, 50, getCollection(Greek.SPEED_P, -0.0135));
    assertGreek(Greek.VEGA, 60, NINE_MONTHS, true, 0.105, 0.0695, 0.3, 55, getCollection(Greek.VEGA, 18.5027));
    assertGreek(Greek.VEGA, 60, NINE_MONTHS, false, 0.105, 0.0695, 0.3, 55, getCollection(Greek.VEGA, 18.5027));
    assertGreek(Greek.VOMMA, 130, NINE_MONTHS, false, 0.05, 0, 0.28, 90, getCollection(Greek.VOMMA, 92.3444));
    assertGreek(Greek.VOMMA_P, 130, NINE_MONTHS, false, 0.05, 0, 0.28, 90, getCollection(Greek.VOMMA_P, 2.5856));
    assertGreek(Greek.THETA, 405, ONE_MONTH, false, 0.07, 0.02, 0.2, 430, getCollection(Greek.THETA, -31.1924));
    assertGreek(Greek.DRIFTLESS_THETA, 405, ONE_MONTH, false, 0.07, 0.02, 0.2, 430, getCollection(Greek.DRIFTLESS_THETA, -32.6218));
    assertGreek(Greek.DRIFTLESS_THETA, 405, ONE_MONTH, true, 0.07, 0.02, 0.2, 430, getCollection(Greek.DRIFTLESS_THETA, -32.6218));
    assertGreek(Greek.RHO, 75, ONE_YEAR, true, 0.09, 0.09, 0.19, 72, getCollection(Greek.RHO, 38.7325));
    assertGreek(Greek.PHI, 453, SIX_MONTHS, false, 0.1068, 0.03, 0.28, 733, getCollection(Greek.PHI, 1.6180));
    assertGreek(Greek.CARRY_RHO, 490, THREE_MONTHS, false, 0.08, 0.03, 0.15, 500, getCollection(Greek.CARRY_RHO, -42.2254));
    assertGreek(Greek.ZETA, 95, THREE_MONTHS, false, 0.08, 0, 0.12, 100, getCollection(Greek.ZETA, 0.2047));
    assertGreek(Greek.ZETA, 95, THREE_MONTHS, true, 0.08, 0, 0.12, 100, getCollection(Greek.ZETA, 0.7953));
  }

  private GreekResultCollection getCollection(final Greek greek, final double value) {
    final GreekResultCollection collection = new GreekResultCollection();
    collection.put(greek, value);
    return collection;
  }

  private void assertPrices(final Set<Greek> greeks, final double strike, final Expiry expiry, final boolean isCall, final double r, final double b, final double sigma, final double spot,
      final GreekResultCollection expected) {
    final EuropeanVanillaOptionDefinition definition = new EuropeanVanillaOptionDefinition(strike, expiry, isCall);
    final StandardOptionDataBundle data = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(r)), b, new VolatilitySurface(ConstantDoublesSurface.from(sigma)), spot, DATE);
    final GreekResultCollection result = MODEL.getGreeks(definition, data, greeks);
    assertResults(result, expected);
    assertPutCallParity(strike, expiry, r, b, sigma, spot);
  }

  private void assertGreek(final Greek greek, final double strike, final Expiry expiry, final boolean isCall, final double r, final double b, final double sigma, final double spot,
      final GreekResultCollection expected) {
    final EuropeanVanillaOptionDefinition definition = new EuropeanVanillaOptionDefinition(strike, expiry, isCall);
    final StandardOptionDataBundle data = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(r)), b, new VolatilitySurface(ConstantDoublesSurface.from(sigma)), spot, DATE);
    final GreekResultCollection result = MODEL.getGreeks(definition, data, Collections.singleton(greek));
    assertResults(result, expected);
  }

  private void assertPutCallParity(final double strike, final Expiry expiry, final double r, final double b, final double sigma, final double spot) {
    final Set<Greek> greeks = Collections.singleton(Greek.FAIR_PRICE);
    final EuropeanVanillaOptionDefinition call = new EuropeanVanillaOptionDefinition(strike, expiry, true);
    final EuropeanVanillaOptionDefinition put = new EuropeanVanillaOptionDefinition(strike, expiry, false);
    final StandardOptionDataBundle data = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(r)), b, new VolatilitySurface(ConstantDoublesSurface.from(sigma)), spot, DATE);
    final GreekResultCollection callResult = MODEL.getGreeks(call, data, greeks);
    final GreekResultCollection putResult = MODEL.getGreeks(put, data, greeks);
    final Double c = callResult.values().iterator().next();
    final Double p = putResult.values().iterator().next();
    final double t = call.getTimeToExpiry(DATE);
    assertEquals(c, p + spot * Math.exp(t * (b - r)) - strike * Math.exp(-r * t), EPS);
  }
}
