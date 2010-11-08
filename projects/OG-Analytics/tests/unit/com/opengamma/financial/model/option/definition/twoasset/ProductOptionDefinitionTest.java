/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition.twoasset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class ProductOptionDefinitionTest {
  private static final double K = 90;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 1));
  private static final YieldAndDiscountCurve R = new YieldCurve(ConstantDoublesCurve.from(0.02));
  private static final double B1 = 0.;
  private static final double B2 = 0.;
  private static final double S1 = 10;
  private static final double S2 = 9;
  private static final VolatilitySurface SIGMA1 = new VolatilitySurface(ConstantDoublesSurface.from(0.1));
  private static final VolatilitySurface SIGMA2 = new VolatilitySurface(ConstantDoublesSurface.from(0.11));
  private static final double RHO = 0.9;
  private static final StandardTwoAssetOptionDataBundle DATA = new StandardTwoAssetOptionDataBundle(R, B1, B2, SIGMA1, SIGMA2, S1, S2, RHO, DATE);

  @Test(expected = IllegalArgumentException.class)
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
