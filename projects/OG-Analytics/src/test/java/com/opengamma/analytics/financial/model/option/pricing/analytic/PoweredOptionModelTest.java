/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.PoweredOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.option.pricing.OptionPricingException;
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
public class PoweredOptionModelTest {
  private static final AnalyticOptionModel<PoweredOptionDefinition, StandardOptionDataBundle> POWERED_MODEL = new PoweredOptionModel();
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();
  private static final Set<Greek> REQUIRED_GREEKS = Collections.singleton(Greek.FAIR_PRICE);
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2009, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.5));
  private static final YieldAndDiscountCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(0.1));
  private static final double B = 0.07;
  private static final double SPOT = 100;
  private static final double STRIKE = 100;
  private static final double SMALL_EPS = 1e-9;
  private static final double BIG_EPS = 1e-4;

  @Test
  public void testNonIntegerPower() {
    try {
      POWERED_MODEL.getGreeks(new PoweredOptionDefinition(100, EXPIRY, 1.3, true), getBundle(0.), REQUIRED_GREEKS);
      Assert.fail();
    } catch (final OptionPricingException e) {
      // Expected
    }
  }

  @Test
  public void testPowerOfOne() {
    PoweredOptionDefinition poweredDefinition = getPoweredDefinition(1, true);
    EuropeanVanillaOptionDefinition vanillaDefinition = getVanillaOption(true);
    assertPriceEquals(poweredDefinition, vanillaDefinition, 0.1);
    assertPriceEquals(poweredDefinition, vanillaDefinition, 0.2);
    assertPriceEquals(poweredDefinition, vanillaDefinition, 0.3);
    poweredDefinition = getPoweredDefinition(1, false);
    vanillaDefinition = getVanillaOption(false);
    assertPriceEquals(poweredDefinition, vanillaDefinition, 0.1);
    assertPriceEquals(poweredDefinition, vanillaDefinition, 0.2);
    assertPriceEquals(poweredDefinition, vanillaDefinition, 0.3);
  }

  @Test
  public void test() {
    PoweredOptionDefinition definition = getPoweredDefinition(2, true);
    assertPriceEquals(definition, 0.1, 53.4487);
    assertPriceEquals(definition, 0.2, 160.2944);
    assertPriceEquals(definition, 0.3, 339.3713);
    definition = getPoweredDefinition(3, true);
    assertPriceEquals(definition, 0.1, 758.8427);
    assertPriceEquals(definition, 0.2, 4608.7213);
    assertPriceEquals(definition, 0.3, 15624.1041);
    definition = getPoweredDefinition(2, false);
    assertPriceEquals(definition, 0.1, 9.7580);
    assertPriceEquals(definition, 0.2, 57.8677);
    assertPriceEquals(definition, 0.3, 142.2726);
    definition = getPoweredDefinition(3, false);
    assertPriceEquals(definition, 0.1, 89.6287);
    assertPriceEquals(definition, 0.2, 1061.2120);
    assertPriceEquals(definition, 0.3, 3745.1853);
  }

  private void assertPriceEquals(final PoweredOptionDefinition poweredDefinition, final EuropeanVanillaOptionDefinition vanillaDefinition, final double sigma) {
    final StandardOptionDataBundle bundle = getBundle(sigma);
    final GreekResultCollection actual = POWERED_MODEL.getGreeks(poweredDefinition, bundle, REQUIRED_GREEKS);
    final GreekResultCollection expected = BSM.getGreeks(vanillaDefinition, bundle, REQUIRED_GREEKS);
    assertEquals(expected.get(Greek.FAIR_PRICE), actual.get(Greek.FAIR_PRICE), SMALL_EPS);
  }

  private void assertPriceEquals(final PoweredOptionDefinition poweredDefinition, final double sigma, final double price) {
    final StandardOptionDataBundle bundle = getBundle(sigma);
    final GreekResultCollection actual = POWERED_MODEL.getGreeks(poweredDefinition, bundle, REQUIRED_GREEKS);
    assertEquals(price, actual.get(Greek.FAIR_PRICE), BIG_EPS * price);
  }

  private StandardOptionDataBundle getBundle(final double sigma) {
    return new StandardOptionDataBundle(CURVE, B, new VolatilitySurface(ConstantDoublesSurface.from(sigma)), SPOT, DATE);
  }

  private PoweredOptionDefinition getPoweredDefinition(final double power, final boolean isCall) {
    return new PoweredOptionDefinition(STRIKE, EXPIRY, power, isCall);
  }

  private EuropeanVanillaOptionDefinition getVanillaOption(final boolean isCall) {
    return new EuropeanVanillaOptionDefinition(STRIKE, EXPIRY, isCall);
  }
}
