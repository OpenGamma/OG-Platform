/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.twoasset;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.twoasset.ProductOptionDefinition;
import com.opengamma.financial.model.option.definition.twoasset.StandardTwoAssetOptionDataBundle;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class ProductOptionModelTest {
  private static final double S1 = 100;
  private static final double S2 = 105;
  private static final YieldAndDiscountCurve R = new YieldCurve(ConstantDoublesCurve.from(0.07));
  private static final double B1 = 0.02;
  private static final double B2 = 0.05;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 7, 1);
  private static final ProductOptionModel MODEL = new ProductOptionModel();
  private static final Expiry EXPIRY1 = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.1));
  private static final Expiry EXPIRY2 = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.5));
  private static final double EPS = 1e-4;

  @Test(expected = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getPricingFunction(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getPricingFunction(new ProductOptionDefinition(0.1, EXPIRY1, true)).evaluate((StandardTwoAssetOptionDataBundle) null);
  }

  @Test
  public void test() {
    ProductOptionDefinition option = new ProductOptionDefinition(15000, EXPIRY1, true);
    StandardTwoAssetOptionDataBundle data = new StandardTwoAssetOptionDataBundle(R, B1, B2, new VolatilitySurface(ConstantDoublesSurface.from(0.2)), new VolatilitySurface(
        ConstantDoublesSurface.from(0.3)), S1, S2, -0.5, DATE);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 0.0028, EPS);
    data = data.withFirstVolatilitySurface(new VolatilitySurface(ConstantDoublesSurface.from(0.3))).withCorrelation(0);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 2.4026, EPS);
    option = new ProductOptionDefinition(15000, EXPIRY2, true);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 266.1594, EPS);
  }
}
