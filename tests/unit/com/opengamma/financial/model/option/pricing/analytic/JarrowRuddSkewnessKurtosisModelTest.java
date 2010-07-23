/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.SkewKurtosisOptionDataBundle;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class JarrowRuddSkewnessKurtosisModelTest {
  private static final YieldAndDiscountCurve R = new ConstantYieldCurve(0.05);
  private static final double B = 0.02;
  private static final double SIGMA = 0.4;
  private static final double T = 0.5;
  private static final VolatilitySurface SURFACE = new ConstantVolatilitySurface(SIGMA);
  private static final double SPOT = 90;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 1, 1);
  private static final BlackScholesMertonModel BSM = new BlackScholesMertonModel();
  private static final JarrowRuddSkewnessKurtosisModel MODEL = new JarrowRuddSkewnessKurtosisModel();
  private static final double SKEW;
  private static final double KURTOSIS;
  private static final OptionDefinition CALL = new EuropeanVanillaOptionDefinition(100, new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, T)), true);
  private static final OptionDefinition PUT = new EuropeanVanillaOptionDefinition(100, new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, T)), false);
  private static final SkewKurtosisOptionDataBundle NORMAL_DATA;

  static {
    final double y = Math.sqrt(Math.exp(SIGMA * SIGMA * T) - 1);
    SKEW = 3 * y + y * y * y;
    KURTOSIS = y * y * (16 + y * y * (15 + y * y * (6 + y * y))) + 3;
    NORMAL_DATA = new SkewKurtosisOptionDataBundle(R, B, SURFACE, SPOT, DATE, SKEW, KURTOSIS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getPricingFunction(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getPricingFunction(CALL).evaluate((SkewKurtosisOptionDataBundle) null);
  }

  @Test
  public void testNormal() {
    assertEquals(MODEL.getPricingFunction(CALL).evaluate(NORMAL_DATA), BSM.getPricingFunction(CALL).evaluate(NORMAL_DATA), 1e-9);
    assertEquals(MODEL.getPricingFunction(PUT).evaluate(NORMAL_DATA), BSM.getPricingFunction(PUT).evaluate(NORMAL_DATA), 1e-9);
  }
}
