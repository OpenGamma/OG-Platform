/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SupershareOptionDefinition;
import com.opengamma.analytics.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.SupershareOptionModel;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class SupershareOptionModelTest {
  private static final YieldAndDiscountCurve CURVE = new YieldCurve(ConstantDoublesCurve.from(0.1));
  private static final double B = 0.;
  private static final VolatilitySurface SURFACE = new VolatilitySurface(ConstantDoublesSurface.from(0.2));
  private static final double SPOT = 100;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final double LOWER = 90;
  private static final double UPPER = 110;
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.25));
  private static final SupershareOptionDefinition OPTION = new SupershareOptionDefinition(EXPIRY, LOWER, UPPER);
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE);
  private static final AnalyticOptionModel<SupershareOptionDefinition, StandardOptionDataBundle> MODEL = new SupershareOptionModel();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getPricingFunction(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getPricingFunction(OPTION).evaluate((StandardOptionDataBundle) null);
  }

  @Test
  public void testZeroVol() {
    StandardOptionDataBundle data = DATA.withVolatilitySurface(new VolatilitySurface(ConstantDoublesSurface.from(1e-15))).withSpot(LOWER - 1);
    assertEquals(MODEL.getPricingFunction(OPTION).evaluate(data), 0, 0);
    data = data.withSpot(UPPER + 1);
    assertEquals(MODEL.getPricingFunction(OPTION).evaluate(data), 0, 0);
  }

  @Test
  public void test() {
    assertEquals(MODEL.getPricingFunction(OPTION).evaluate(DATA), 0.7389, 1e-4);
  }
}
