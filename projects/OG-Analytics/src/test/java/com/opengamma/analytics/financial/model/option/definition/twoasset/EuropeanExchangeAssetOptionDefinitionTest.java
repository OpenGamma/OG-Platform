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
public class EuropeanExchangeAssetOptionDefinitionTest {
  private static final double S1 = 100;
  private static final double S2 = 120;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 1, 1);
  private static final StandardTwoAssetOptionDataBundle DATA = new StandardTwoAssetOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.02)), 0, 0, new VolatilitySurface(
      ConstantDoublesSurface.from(0.2)), new VolatilitySurface(ConstantDoublesSurface.from(0.15)), S1, S2, 0.5, DATE);
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 0.4));

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExpiry() {
    new EuropeanExchangeAssetOptionDefinition(null, 10, 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroQ1() {
    new EuropeanExchangeAssetOptionDefinition(EXPIRY, 0, 10);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZeroQ2() {
    new EuropeanExchangeAssetOptionDefinition(EXPIRY, 10, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeQ1() {
    new EuropeanExchangeAssetOptionDefinition(EXPIRY, -2, 10);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeQ2() {
    new EuropeanExchangeAssetOptionDefinition(EXPIRY, 10, -6);
  }

  @Test
  public void testGetters() {
    final double q1 = 30;
    final double q2 = 31;
    final EuropeanExchangeAssetOptionDefinition option = new EuropeanExchangeAssetOptionDefinition(EXPIRY, q1, q2);
    assertEquals(option.getFirstQuantity(), q1, 0);
    assertEquals(option.getSecondQuantity(), q2, 0);
  }

  @Test
  public void testHashCodeAndEquals() {
    final double q1 = 1;
    final double q2 = 2;
    final EuropeanExchangeAssetOptionDefinition option = new EuropeanExchangeAssetOptionDefinition(EXPIRY, q1, q2);
    EuropeanExchangeAssetOptionDefinition other = new EuropeanExchangeAssetOptionDefinition(EXPIRY, q1, q2);
    assertEquals(option, other);
    assertEquals(option.hashCode(), other.hashCode());
    other = new EuropeanExchangeAssetOptionDefinition(new Expiry(DateUtils.getUTCDate(2011, 2, 1)), q1, q2);
    assertFalse(option.equals(other));
    other = new EuropeanExchangeAssetOptionDefinition(EXPIRY, q1 + q2, q2);
    assertFalse(option.equals(other));
    other = new EuropeanExchangeAssetOptionDefinition(EXPIRY, q1, q1 + q2);
    assertFalse(option.equals(other));
  }

  @Test
  public void testPayoffFunction() {
    final double eps = 1e-15;
    final double q1 = 1.2;
    final double q2 = 1;
    final EuropeanExchangeAssetOptionDefinition option = new EuropeanExchangeAssetOptionDefinition(EXPIRY, q1, q2);
    StandardTwoAssetOptionDataBundle data = new StandardTwoAssetOptionDataBundle(DATA);
    assertEquals(option.getPayoffFunction().getPayoff(data, 0.), 0, eps);
    data = DATA.withFirstSpot(110);
    assertEquals(option.getPayoffFunction().getPayoff(data, 0.), 12, eps);
    data = DATA.withFirstSpot(80);
    assertEquals(option.getPayoffFunction().getPayoff(data, 0.), 0, eps);
    data = DATA.withSecondSpot(130);
    assertEquals(option.getPayoffFunction().getPayoff(data, 0.), 0, eps);
    data = DATA.withSecondSpot(110);
    assertEquals(option.getPayoffFunction().getPayoff(data, 0.), 10, eps);
  }

  @Test
  public void testExerciseFunction() {
    final double q1 = 1;
    final double q2 = 1.2;
    final EuropeanExchangeAssetOptionDefinition option = new EuropeanExchangeAssetOptionDefinition(EXPIRY, q1, q2);
    StandardTwoAssetOptionDataBundle data = DATA.withFirstSpot(1);
    assertFalse(option.getExerciseFunction().shouldExercise(data, 0.));
    data = DATA.withFirstSpot(1000);
    assertFalse(option.getExerciseFunction().shouldExercise(data, 0.));
    data = DATA.withSecondSpot(1);
    assertFalse(option.getExerciseFunction().shouldExercise(data, 0.));
    data = DATA.withSecondSpot(1000);
    assertFalse(option.getExerciseFunction().shouldExercise(data, 0.));

  }
}
