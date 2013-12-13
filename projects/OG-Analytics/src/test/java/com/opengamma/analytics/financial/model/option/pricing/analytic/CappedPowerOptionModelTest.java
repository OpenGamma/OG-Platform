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
import com.opengamma.analytics.financial.model.option.definition.CappedPowerOptionDefinition;
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
public class CappedPowerOptionModelTest {
  private static final double B = 0.02;
  private static final double DELTA = 90;
  private static final double STRIKE = 100;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2009, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.5));
  private static final YieldAndDiscountCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(0.08));
  private static final VolatilitySurface SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(0.1));
  private static final StandardOptionDataBundle BUNDLE = new StandardOptionDataBundle(CURVE, B, SURFACE, STRIKE - DELTA, DATE);
  private static final AnalyticOptionModel<CappedPowerOptionDefinition, StandardOptionDataBundle> CAPPED_MODEL = new CappedPowerOptionModel();
  private static final AnalyticOptionModel<AsymmetricPowerOptionDefinition, StandardOptionDataBundle> UNCAPPED_MODEL = new AsymmetricPowerOptionModel();
  private static final Set<Greek> REQUIRED_GREEKS = Collections.singleton(Greek.FAIR_PRICE);
  private static final double CALL_CAP = 500;
  private static final double PUT_CAP = STRIKE;
  private static final double EPS = 1e-4;

  @Test
  public void test() {
    assertEquals(getCappedPrice(1.9, CALL_CAP, true), getUncappedPrice(1.9, true), EPS);
    assertEquals(getCappedPrice(1.95, CALL_CAP, true), getUncappedPrice(1.95, true), EPS);
    assertEquals(getCappedPrice(2., CALL_CAP, true), getUncappedPrice(2., true), EPS);
    assertEquals(getCappedPrice(2.05, CALL_CAP, true), getUncappedPrice(2.05, true), EPS);
    assertEquals(getCappedPrice(2.1, CALL_CAP, true), getUncappedPrice(2.1, true), EPS);
    assertEquals(getCappedPrice(1.9, PUT_CAP, false), getUncappedPrice(1.9, false), EPS);
    assertEquals(getCappedPrice(1.95, PUT_CAP, false), getUncappedPrice(1.95, false), EPS);
    assertEquals(getCappedPrice(2., PUT_CAP, false), getUncappedPrice(2., false), EPS);
    assertEquals(getCappedPrice(2.05, PUT_CAP, false), getUncappedPrice(2.05, false), EPS);
    assertEquals(getCappedPrice(2.1, PUT_CAP, false), getUncappedPrice(2.1, false), EPS);
  }

  private double getCappedPrice(final double power, final double cap, final boolean isCall) {
    return CAPPED_MODEL.getGreeks(getDefinition(power, cap, isCall), BUNDLE, REQUIRED_GREEKS).get(Greek.FAIR_PRICE);
  }

  private double getUncappedPrice(final double power, final boolean isCall) {
    return UNCAPPED_MODEL.getGreeks(getDefinition(power, isCall), BUNDLE, REQUIRED_GREEKS).get(Greek.FAIR_PRICE);
  }

  private CappedPowerOptionDefinition getDefinition(final double power, final double cap, final boolean isCall) {
    return new CappedPowerOptionDefinition(STRIKE, EXPIRY, power, cap, isCall);
  }

  private AsymmetricPowerOptionDefinition getDefinition(final double power, final boolean isCall) {
    return new AsymmetricPowerOptionDefinition(STRIKE, EXPIRY, power, isCall);
  }
}
