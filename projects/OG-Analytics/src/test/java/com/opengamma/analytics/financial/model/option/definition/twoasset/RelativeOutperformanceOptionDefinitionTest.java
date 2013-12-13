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
public class RelativeOutperformanceOptionDefinitionTest {
  private static final double K = 1;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 1));
  private static final YieldAndDiscountCurve R = YieldCurve.from(ConstantDoublesCurve.from(0.2));
  private static final double B1 = 0.02;
  private static final double B2 = 0.05;
  private static final double S1 = 100;
  private static final double S2 = 120;
  private static final VolatilitySurface SIGMA1 = new VolatilitySurface(ConstantDoublesSurface.from(0.4));
  private static final VolatilitySurface SIGMA2 = new VolatilitySurface(ConstantDoublesSurface.from(0.6));
  private static final double RHO = 0;
  private static final StandardTwoAssetOptionDataBundle DATA = new StandardTwoAssetOptionDataBundle(R, B1, B2, SIGMA1, SIGMA2, S1, S2, RHO, DATE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    new RelativeOutperformanceOptionDefinition(K, EXPIRY, true).getPayoffFunction().getPayoff(null, null);
  }

  @Test
  public void testExercise() {
    RelativeOutperformanceOptionDefinition option = new RelativeOutperformanceOptionDefinition(K, EXPIRY, true);
    assertFalse(option.getExerciseFunction().shouldExercise(DATA, null));
    option = new RelativeOutperformanceOptionDefinition(K, EXPIRY, false);
    assertFalse(option.getExerciseFunction().shouldExercise(DATA, null));
    final StandardTwoAssetOptionDataBundle data = DATA.withFirstCostOfCarry(180);
    option = new RelativeOutperformanceOptionDefinition(K, EXPIRY, true);
    assertFalse(option.getExerciseFunction().shouldExercise(data, null));
    option = new RelativeOutperformanceOptionDefinition(K, EXPIRY, false);
    assertFalse(option.getExerciseFunction().shouldExercise(DATA, null));
  }

  @Test
  public void testPayoff() {
    final RelativeOutperformanceOptionDefinition call = new RelativeOutperformanceOptionDefinition(K, EXPIRY, true);
    final RelativeOutperformanceOptionDefinition put = new RelativeOutperformanceOptionDefinition(K, EXPIRY, false);
    final double eps = 1e-12;
    assertEquals(call.getPayoffFunction().getPayoff(DATA, null), 0, eps);
    assertEquals(put.getPayoffFunction().getPayoff(DATA, null), 1. / 6, eps);
    final StandardTwoAssetOptionDataBundle data = DATA.withSecondSpot(80);
    assertEquals(call.getPayoffFunction().getPayoff(data, null), 1. / 4, eps);
    assertEquals(put.getPayoffFunction().getPayoff(data, null), 0, eps);
  }
}
