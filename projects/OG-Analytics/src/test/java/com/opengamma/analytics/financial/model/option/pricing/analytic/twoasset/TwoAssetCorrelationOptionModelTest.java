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
import com.opengamma.analytics.financial.model.option.definition.twoasset.StandardTwoAssetOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.twoasset.TwoAssetCorrelationOptionDefinition;
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
public class TwoAssetCorrelationOptionModelTest {
  private static final double S1 = 52;
  private static final double S2 = 65;
  private static final YieldAndDiscountCurve R = YieldCurve.from(ConstantDoublesCurve.from(0.1));
  private static final double B1 = 0.1;
  private static final double B2 = 0.1;
  private static final VolatilitySurface SIGMA1 = new VolatilitySurface(ConstantDoublesSurface.from(0.2));
  private static final VolatilitySurface SIGMA2 = new VolatilitySurface(ConstantDoublesSurface.from(0.3));
  private static final double RHO = 0.75;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final StandardTwoAssetOptionDataBundle DATA = new StandardTwoAssetOptionDataBundle(R, B1, B2, SIGMA1, SIGMA2, S1, S2, RHO, DATE);
  private static final TwoAssetCorrelationOptionModel MODEL = new TwoAssetCorrelationOptionModel();
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.5));
  private static final TwoAssetCorrelationOptionDefinition OPTION = new TwoAssetCorrelationOptionDefinition(50, EXPIRY, true, 70);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getPricingFunction(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getPricingFunction(OPTION).evaluate((StandardTwoAssetOptionDataBundle) null);
  }

  @Test
  public void test() {
    assertEquals(MODEL.getPricingFunction(OPTION).evaluate(DATA), 4.7073, 1e-4);
  }
}
