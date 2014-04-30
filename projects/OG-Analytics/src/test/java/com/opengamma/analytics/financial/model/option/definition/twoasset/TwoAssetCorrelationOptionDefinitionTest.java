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
public class TwoAssetCorrelationOptionDefinitionTest {
  private static final double K = 90;
  private static final double PAYOUT = 85;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 1));
  private static final YieldAndDiscountCurve R = YieldCurve.from(ConstantDoublesCurve.from(0.02));
  private static final double B1 = 0.;
  private static final double B2 = 0.;
  private static final double S1 = 100;
  private static final double S2 = 80;
  private static final VolatilitySurface SIGMA1 = new VolatilitySurface(ConstantDoublesSurface.from(0.1));
  private static final VolatilitySurface SIGMA2 = new VolatilitySurface(ConstantDoublesSurface.from(0.11));
  private static final double RHO = 0.9;
  private static final StandardTwoAssetOptionDataBundle DATA = new StandardTwoAssetOptionDataBundle(R, B1, B2, SIGMA1, SIGMA2, S1, S2, RHO, DATE);
  private static final TwoAssetCorrelationOptionDefinition CALL = new TwoAssetCorrelationOptionDefinition(K, EXPIRY, true, PAYOUT);
  private static final TwoAssetCorrelationOptionDefinition PUT = new TwoAssetCorrelationOptionDefinition(K, EXPIRY, false, PAYOUT);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    CALL.getPayoffFunction().getPayoff(null, null);
  }

  @Test
  public void test() {
    assertEquals(CALL.getPayoutLevel(), PAYOUT, 0);
    TwoAssetCorrelationOptionDefinition other = new TwoAssetCorrelationOptionDefinition(K, EXPIRY, true, PAYOUT);
    assertEquals(CALL, other);
    assertEquals(CALL.hashCode(), other.hashCode());
    other = new TwoAssetCorrelationOptionDefinition(PAYOUT, EXPIRY, true, PAYOUT);
    assertFalse(CALL.equals(other));
    other = new TwoAssetCorrelationOptionDefinition(K, EXPIRY, true, K);
    assertFalse(CALL.equals(other));
  }

  @Test
  public void testExercise() {
    assertFalse(CALL.getExerciseFunction().shouldExercise(DATA, null));
    assertFalse(PUT.getExerciseFunction().shouldExercise(DATA, null));
  }

  @Test
  public void testPayoff() {
    assertEquals(CALL.getPayoffFunction().getPayoff(DATA, null), 0, 0);
    assertEquals(PUT.getPayoffFunction().getPayoff(DATA, null), 0, 0);
    TwoAssetCorrelationOptionDefinition other = new TwoAssetCorrelationOptionDefinition(70, EXPIRY, true, 60);
    assertEquals(other.getPayoffFunction().getPayoff(DATA, null), 20, 0);
    other = new TwoAssetCorrelationOptionDefinition(70, EXPIRY, false, 120);
    assertEquals(other.getPayoffFunction().getPayoff(DATA.withFirstSpot(50), null), 40, 0);
  }
}
