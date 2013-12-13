/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.analytic.twoasset;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.twoasset.ProductOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.twoasset.StandardTwoAssetOptionDataBundle;
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
public class ProductOptionModelTest {
  private static final double S1 = 100;
  private static final double S2 = 105;
  private static final YieldAndDiscountCurve R = YieldCurve.from(ConstantDoublesCurve.from(0.07));
  private static final double B1 = 0.02;
  private static final double B2 = 0.05;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final ProductOptionModel MODEL = new ProductOptionModel();
  private static final Expiry EXPIRY1 = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.1));
  private static final Expiry EXPIRY2 = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.5));
  private static final double EPS = 1e-4;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getPricingFunction(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
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
