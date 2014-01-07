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
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.AsymmetricPowerOptionDefinition;
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
public class AsymmetricPowerOptionModelTest {
  private static final double B = 0.02;
  private static final double SPOT = 10;
  private static final double STRIKE = 100;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2009, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.5));
  private static final YieldAndDiscountCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(0.08));
  private static final VolatilitySurface SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(0.1));
  private static final StandardOptionDataBundle BUNDLE = new StandardOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE);
  private static final AnalyticOptionModel<AsymmetricPowerOptionDefinition, StandardOptionDataBundle> MODEL = new AsymmetricPowerOptionModel();
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BS_MODEL = new BlackScholesMertonModel();
  private static final Set<Greek> REQUIRED_GREEKS = Collections.singleton(Greek.FAIR_PRICE);
  private static final double EPS = 1e-4;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getPricingFunction(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getPricingFunction(new AsymmetricPowerOptionDefinition(STRIKE, EXPIRY, 1, true)).evaluate((StandardOptionDataBundle) null);
  }

  @Test
  public void test() {
    assertEquals(getPrice(1.9, true), 0.3102, EPS);
    assertEquals(getPrice(1.95, true), 1.9320, EPS);
    assertEquals(getPrice(2., true), 6.7862, EPS);
    assertEquals(getPrice(2.05, true), 15.8587, EPS);
    assertEquals(getPrice(2.1, true), 28.4341, EPS);
    assertEquals(getPrice(1.9, false), 18.2738, EPS);
    assertEquals(getPrice(1.95, false), 10.2890, EPS);
    assertEquals(getPrice(2., false), 4.3539, EPS);
    assertEquals(getPrice(2.05, false), 1.3089, EPS);
    assertEquals(getPrice(2.1, false), 0.2745, EPS);

    for (int i = 0; i < 5; i++) {
      final double power = 1.9 + 0.05 * i;
      assertEquals(getPrice(power, true), getBSPrice(power, true), EPS);
      assertEquals(getPrice(power, false), getBSPrice(power, false), EPS);
    }
  }

  private double getPrice(final double power, final boolean isCall) {
    return MODEL.getGreeks(getDefinition(power, isCall), BUNDLE, REQUIRED_GREEKS).get(Greek.FAIR_PRICE);
  }

  private double getBSPrice(final double power, final boolean isCall) {
    final StandardOptionDataBundle bsBundle = getModifiedDataBundle(BUNDLE, power);
    return BS_MODEL.getGreeks(getDefinition(power, isCall), bsBundle, REQUIRED_GREEKS).get(Greek.FAIR_PRICE);
  }

  private AsymmetricPowerOptionDefinition getDefinition(final double power, final boolean isCall) {
    return new AsymmetricPowerOptionDefinition(STRIKE, EXPIRY, power, isCall);
  }

  private StandardOptionDataBundle getModifiedDataBundle(final StandardOptionDataBundle data, final double p) {
    final double t = DateUtils.getDifferenceInYears(DATE, EXPIRY.getExpiry());
    final double spot = Math.pow(data.getSpot(), p);
    double sigma = data.getVolatility(t, STRIKE);
    final double b = p * (data.getCostOfCarry() + (p - 1) * sigma * sigma * 0.5);
    sigma *= p;
    return new StandardOptionDataBundle(CURVE, b, new VolatilitySurface(ConstantDoublesSurface.from(sigma)), spot, DATE);

  }
}
