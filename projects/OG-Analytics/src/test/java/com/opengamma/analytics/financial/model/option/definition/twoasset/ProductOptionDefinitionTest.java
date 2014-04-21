/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition.twoasset;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
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
public class ProductOptionDefinitionTest {
  private static final double K = 90;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 1));
  private static final YieldAndDiscountCurve R = YieldCurve.from(ConstantDoublesCurve.from(0.02));
  private static final double B1 = 0.;
  private static final double B2 = 0.;
  private static final double S1 = 10;
  private static final double S2 = 9;
  private static final VolatilitySurface SIGMA1 = new VolatilitySurface(ConstantDoublesSurface.from(0.1));
  private static final VolatilitySurface SIGMA2 = new VolatilitySurface(ConstantDoublesSurface.from(0.11));
  private static final double RHO = 0.9;
  private static final StandardTwoAssetOptionDataBundle DATA = new StandardTwoAssetOptionDataBundle(R, B1, B2, SIGMA1, SIGMA2, S1, S2, RHO, DATE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    new ProductOptionDefinition(K, EXPIRY, true).getPayoffFunction().getPayoff(null, null);
  }

  @Test
  public void testExercise() {
    ProductOptionDefinition option = new ProductOptionDefinition(K, EXPIRY, true);
    assertFalse(option.getExerciseFunction().shouldExercise(DATA, null));
    option = new ProductOptionDefinition(K, EXPIRY, false);
    assertFalse(option.getExerciseFunction().shouldExercise(DATA, null));
    final StandardTwoAssetOptionDataBundle data = DATA.withFirstCostOfCarry(180);
    option = new ProductOptionDefinition(K, EXPIRY, true);
    assertFalse(option.getExerciseFunction().shouldExercise(data, null));
    option = new ProductOptionDefinition(K, EXPIRY, false);
    assertFalse(option.getExerciseFunction().shouldExercise(DATA, null));
  }

  @Test
  public void testPayoff() {
    final ProductOptionDefinition call = new ProductOptionDefinition(K, EXPIRY, true);
    final ProductOptionDefinition put = new ProductOptionDefinition(K, EXPIRY, false);
    final double eps = 1e-12;
    assertEquals(call.getPayoffFunction().getPayoff(DATA, null), 0, eps);
    assertEquals(put.getPayoffFunction().getPayoff(DATA, null), 0, eps);
    StandardTwoAssetOptionDataBundle data = DATA.withSecondSpot(8);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 0, eps);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 10, eps);
    data = DATA.withSecondSpot(10);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 10, eps);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, eps);
  }
}
