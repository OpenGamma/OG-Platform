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
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.LogOptionDefinition;
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
public class LogOptionModelTest {
  private static final AnalyticOptionModel<LogOptionDefinition, StandardOptionDataBundle> MODEL = new LogOptionModel();
  private static final Set<Greek> REQUIRED_GREEKS = Collections.singleton(Greek.FAIR_PRICE);
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2009, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.75));
  private static final YieldAndDiscountCurve CURVE = YieldCurve.from(ConstantDoublesCurve.from(0.08));
  private static final double B = 0.04;
  private static final double SPOT = 100;
  private static final double EPS = 1e-4;

  @Test
  public void test() {
    LogOptionDefinition definition = getDefinition(70);
    assertPriceEquals(definition, 0.2, 0.3510);
    assertPriceEquals(definition, 0.3, 0.3422);
    assertPriceEquals(definition, 0.4, 0.3379);
    assertPriceEquals(definition, 0.5, 0.3365);
    assertPriceEquals(definition, 0.6, 0.3362);
    definition = getDefinition(130);
    assertPriceEquals(definition, 0.2, 0.0056);
    assertPriceEquals(definition, 0.3, 0.0195);
    assertPriceEquals(definition, 0.4, 0.0363);
    assertPriceEquals(definition, 0.5, 0.0532);
    assertPriceEquals(definition, 0.6, 0.0691);
  }

  private void assertPriceEquals(final LogOptionDefinition definition, final double sigma, final double price) {
    final StandardOptionDataBundle bundle = getBundle(sigma);
    final GreekResultCollection actual = MODEL.getGreeks(definition, bundle, REQUIRED_GREEKS);
    assertEquals(actual.get(Greek.FAIR_PRICE), price, EPS);
  }

  private StandardOptionDataBundle getBundle(final double sigma) {
    return new StandardOptionDataBundle(CURVE, B, new VolatilitySurface(ConstantDoublesSurface.from(sigma)), SPOT, DATE);
  }

  private LogOptionDefinition getDefinition(final double strike) {
    return new LogOptionDefinition(strike, EXPIRY);
  }

}
