/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.time.Instant;
import javax.time.InstantProvider;

import org.junit.Test;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.Price;
import com.opengamma.financial.model.interestrate.curve.ConstantInterestRateDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.option.definition.LogOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 * @author emcleod
 */
public class LogOptionModelTest {
  private static final AnalyticOptionModel<LogOptionDefinition, StandardOptionDataBundle> MODEL = new LogOptionModel();
  private static final Greek PRICE = new Price();
  private static final List<Greek> REQUIRED_GREEKS = Arrays.asList(new Greek[] { PRICE });
  private static final InstantProvider DATE = Instant.instant(1000);
  private static final Expiry EXPIRY = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.75));
  private static final DiscountCurve CURVE = new ConstantInterestRateDiscountCurve(DATE, 0.08);
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

  private void assertPriceEquals(LogOptionDefinition definition, double sigma, double price) {
    StandardOptionDataBundle bundle = getBundle(sigma);
    Map<Greek, Map<String, Double>> actual = MODEL.getGreeks(definition, bundle, REQUIRED_GREEKS);
    assertEquals(actual.get(PRICE).values().iterator().next(), price, EPS);
  }

  private StandardOptionDataBundle getBundle(double sigma) {
    return new StandardOptionDataBundle(CURVE, B, new ConstantVolatilitySurface(DATE, sigma), SPOT, DATE);
  }

  private LogOptionDefinition getDefinition(double strike) {
    return new LogOptionDefinition(strike, EXPIRY);
  }

}
