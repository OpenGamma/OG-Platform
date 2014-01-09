/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

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
public class AmericanVanillaOptionDefinitionTest {
  private static final double DIFF = 10;
  private static final double STRIKE = 100;
  private static final double LOW_PRICE = 5;
  private static final double HIGH_PRICE = LOW_PRICE + DIFF;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 5, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 1));
  private static final OptionDefinition CALL = new AmericanVanillaOptionDefinition(STRIKE, EXPIRY, true);
  private static final OptionDefinition PUT = new AmericanVanillaOptionDefinition(STRIKE, EXPIRY, false);
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.05)), 0., new VolatilitySurface(ConstantDoublesSurface.from(0.2)),
      STRIKE, DATE);
  private static final double EPS = 1e-15;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataInPayoff() {
    CALL.getPayoffFunction().getPayoff(null, LOW_PRICE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullOptionPriceInPayoff() {
    CALL.getPayoffFunction().getPayoff(DATA, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeOptionPriceInPayoff() {
    CALL.getPayoffFunction().getPayoff(DATA, -LOW_PRICE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataInExercise() {
    CALL.getExerciseFunction().shouldExercise(null, LOW_PRICE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullOptionPriceInExercise() {
    CALL.getExerciseFunction().shouldExercise(DATA, -LOW_PRICE);
  }

  @Test
  public void testExerciseFunction() {
    OptionExerciseFunction<StandardOptionDataBundle> exercise = CALL.getExerciseFunction();
    assertTrue(exercise.shouldExercise(DATA.withSpot(STRIKE + DIFF), LOW_PRICE));
    assertFalse(exercise.shouldExercise(DATA.withSpot(STRIKE - DIFF), LOW_PRICE));
    assertFalse(exercise.shouldExercise(DATA.withSpot(STRIKE + DIFF), HIGH_PRICE));
    assertFalse(exercise.shouldExercise(DATA.withSpot(STRIKE - DIFF), HIGH_PRICE));
    exercise = PUT.getExerciseFunction();
    assertFalse(exercise.shouldExercise(DATA.withSpot(STRIKE + DIFF), LOW_PRICE));
    assertTrue(exercise.shouldExercise(DATA.withSpot(STRIKE - DIFF), LOW_PRICE));
    assertFalse(exercise.shouldExercise(DATA.withSpot(STRIKE + DIFF), HIGH_PRICE));
    assertFalse(exercise.shouldExercise(DATA.withSpot(STRIKE - DIFF), HIGH_PRICE));
  }

  @Test
  public void testPayoffFunction() {
    OptionPayoffFunction<StandardOptionDataBundle> payoff = CALL.getPayoffFunction();
    assertEquals(payoff.getPayoff(DATA.withSpot(STRIKE + DIFF), LOW_PRICE), DIFF, EPS);
    assertEquals(payoff.getPayoff(DATA.withSpot(STRIKE - DIFF), LOW_PRICE), LOW_PRICE, EPS);
    assertEquals(payoff.getPayoff(DATA.withSpot(STRIKE + DIFF), HIGH_PRICE), HIGH_PRICE, EPS);
    assertEquals(payoff.getPayoff(DATA.withSpot(STRIKE - DIFF), HIGH_PRICE), HIGH_PRICE, EPS);
    payoff = PUT.getPayoffFunction();
    assertEquals(payoff.getPayoff(DATA.withSpot(STRIKE + DIFF), LOW_PRICE), LOW_PRICE, EPS);
    assertEquals(payoff.getPayoff(DATA.withSpot(STRIKE - DIFF), LOW_PRICE), DIFF, EPS);
    assertEquals(payoff.getPayoff(DATA.withSpot(STRIKE + DIFF), HIGH_PRICE), HIGH_PRICE, EPS);
    assertEquals(payoff.getPayoff(DATA.withSpot(STRIKE - DIFF), HIGH_PRICE), HIGH_PRICE, EPS);
  }

  @Test
  public void testEqualsAndHashCode() {
    assertFalse(CALL.equals(PUT));
    OptionDefinition call = new AmericanVanillaOptionDefinition(STRIKE, EXPIRY, true);
    final OptionDefinition put = new AmericanVanillaOptionDefinition(STRIKE, EXPIRY, false);
    assertEquals(call, CALL);
    assertEquals(call.hashCode(), CALL.hashCode());
    assertEquals(put, PUT);
    assertEquals(put.hashCode(), PUT.hashCode());
    call = new AmericanVanillaOptionDefinition(STRIKE + 1, EXPIRY, true);
    assertFalse(call.equals(CALL));
    call = new AmericanVanillaOptionDefinition(STRIKE, new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 2)), true);
    assertFalse(call.equals(CALL));
    call = new EuropeanVanillaOptionDefinition(STRIKE, EXPIRY, true);
    assertFalse(call.equals(CALL));
  }
}
