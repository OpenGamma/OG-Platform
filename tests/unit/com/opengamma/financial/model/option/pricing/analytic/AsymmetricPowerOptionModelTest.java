/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import javax.time.Duration;
import javax.time.Instant;
import javax.time.InstantProvider;

import org.junit.Test;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.Price;
import com.opengamma.financial.model.interestrate.curve.ConstantInterestRateDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.option.definition.AsymmetricPowerOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 * @author emcleod
 */
public class AsymmetricPowerOptionModelTest {
  private static final double B = 0.02;
  private static final double SPOT = 10;
  private static final double STRIKE = 100;
  private static final InstantProvider DATE = Instant.millisInstant(1000);
  private static final Expiry EXPIRY = new Expiry(DATE.toInstant().plus(Duration.seconds(DateUtil.SECONDS_PER_YEAR / 2)));
  private static final DiscountCurve CURVE = new ConstantInterestRateDiscountCurve(DATE, 0.08);
  private static final AnalyticOptionModel<AsymmetricPowerOptionDefinition, StandardOptionDataBundle> MODEL = new AsymmetricPowerOptionModel();
  private static final Greek PRICE = new Price();
  private static final List<Greek> REQUIRED_GREEKS = Arrays.asList(new Greek[] { PRICE });
  private static final double EPS = 1e-4;

  @Test
  public void test() {
    assertEquals(getPrice(0.1, 1.9, true), 0.3102, EPS);
    assertEquals(getPrice(0.1, 1.95, true), 1.9320, EPS);
    assertEquals(getPrice(0.1, 2., true), 6.7862, EPS);
    assertEquals(getPrice(0.1, 2.05, true), 15.8587, EPS);
    assertEquals(getPrice(0.1, 2.1, true), 28.4341, EPS);
    assertEquals(getPrice(0.1, 1.9, false), 18.2738, EPS);
    assertEquals(getPrice(0.1, 1.95, false), 10.2890, EPS);
    assertEquals(getPrice(0.1, 2., false), 4.3539, EPS);
    assertEquals(getPrice(0.1, 2.05, false), 1.3089, EPS);
    assertEquals(getPrice(0.1, 2.1, false), 0.2745, EPS);
  }

  private double getPrice(double sigma, double power, boolean isCall) {
    return MODEL.getGreeks(getDefinition(power, isCall), getBundle(sigma), REQUIRED_GREEKS).get(PRICE).values().iterator().next();
  }

  private StandardOptionDataBundle getBundle(double sigma) {
    return new StandardOptionDataBundle(CURVE, B, new ConstantVolatilitySurface(DATE, sigma), SPOT, DATE);
  }

  private AsymmetricPowerOptionDefinition getDefinition(double power, boolean isCall) {
    return new AsymmetricPowerOptionDefinition(STRIKE, EXPIRY, power, isCall);
  }
}
