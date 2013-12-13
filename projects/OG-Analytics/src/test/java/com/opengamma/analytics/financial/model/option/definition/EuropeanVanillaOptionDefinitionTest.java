/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

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
public class EuropeanVanillaOptionDefinitionTest {
  private static final double DIFF = 20;
  private static final double SPOT = 100;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 5, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 1));
  private static final OptionDefinition CALL = new EuropeanVanillaOptionDefinition(SPOT, EXPIRY, true);
  private static final OptionDefinition PUT = new EuropeanVanillaOptionDefinition(SPOT, EXPIRY, false);
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.05)), 0., new VolatilitySurface(ConstantDoublesSurface.from(0.2)), 100.,
      DATE);
  private static final double EPS = 1e-15;

  @Test
  public void testExerciseFunction() {
    OptionExerciseFunction<StandardOptionDataBundle> exercise = CALL.getExerciseFunction();
    assertFalse(exercise.shouldExercise(DATA, SPOT - DIFF));
    assertFalse(exercise.shouldExercise(DATA, SPOT + DIFF));
    assertFalse(exercise.shouldExercise(DATA.withSpot(SPOT + DIFF), SPOT));
    assertFalse(exercise.shouldExercise(DATA, SPOT));
    exercise = PUT.getExerciseFunction();
    assertFalse(exercise.shouldExercise(DATA, SPOT - DIFF));
    assertFalse(exercise.shouldExercise(DATA, SPOT + DIFF));
  }

  @Test
  public void testPayoffFunction() {
    OptionPayoffFunction<StandardOptionDataBundle> payoff = CALL.getPayoffFunction();
    assertEquals(payoff.getPayoff(DATA.withSpot(SPOT + DIFF), null), DIFF, EPS);
    assertEquals(payoff.getPayoff(DATA.withSpot(SPOT - DIFF), null), 0, EPS);
    payoff = PUT.getPayoffFunction();
    assertEquals(payoff.getPayoff(DATA.withSpot(SPOT + DIFF), null), 0, EPS);
    assertEquals(payoff.getPayoff(DATA.withSpot(SPOT - DIFF), null), DIFF, EPS);
  }

  @Test
  public void testEqualsAndHashCode() {
    assertFalse(CALL.equals(PUT));
    OptionDefinition call = new EuropeanVanillaOptionDefinition(SPOT, EXPIRY, true);
    final OptionDefinition put = new EuropeanVanillaOptionDefinition(SPOT, EXPIRY, false);
    assertEquals(call, CALL);
    assertEquals(call.hashCode(), CALL.hashCode());
    assertEquals(put, PUT);
    assertEquals(put.hashCode(), PUT.hashCode());
    call = new EuropeanVanillaOptionDefinition(SPOT + 1, EXPIRY, true);
    assertFalse(call.equals(CALL));
    call = new EuropeanVanillaOptionDefinition(SPOT, new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 2)), true);
    assertFalse(call.equals(CALL));
    call = new AmericanVanillaOptionDefinition(SPOT, EXPIRY, true);
    assertFalse(call.equals(CALL));
  }
}
