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
import com.opengamma.analytics.financial.model.option.definition.twoasset.RelativeOutperformanceOptionDefinition;
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
public class RelativeOutperformanceOptionModelTest {
  private static final double S1 = 130;
  private static final double S2 = 100;
  private static final YieldAndDiscountCurve R = YieldCurve.from(ConstantDoublesCurve.from(0.07));
  private static final double B1 = 0.05;
  private static final double B2 = 0.03;
  private static final VolatilitySurface SIGMA1 = new VolatilitySurface(ConstantDoublesSurface.from(0.3));
  private static final VolatilitySurface SIGMA2 = new VolatilitySurface(ConstantDoublesSurface.from(0.4));
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final RelativeOutperformanceOptionModel MODEL = new RelativeOutperformanceOptionModel();
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.25));
  private static final double EPS = 1e-4;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getPricingFunction(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getPricingFunction(new RelativeOutperformanceOptionDefinition(0.1, EXPIRY, true)).evaluate((StandardTwoAssetOptionDataBundle) null);
  }

  @Test
  public void test() {
    RelativeOutperformanceOptionDefinition option = new RelativeOutperformanceOptionDefinition(0.1, EXPIRY, true);
    StandardTwoAssetOptionDataBundle data = new StandardTwoAssetOptionDataBundle(R, B1, B2, SIGMA1, SIGMA2, S1, S2, -0.5, DATE);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 1.2582, EPS);
    option = new RelativeOutperformanceOptionDefinition(0.5, EXPIRY, true);
    data = data.withCorrelation(0);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 0.8449, EPS);
    option = new RelativeOutperformanceOptionDefinition(1, EXPIRY, true);
    data = data.withCorrelation(0.5);
    assertEquals(MODEL.getPricingFunction(option).evaluate(data), 0.3382, EPS);
  }
}
