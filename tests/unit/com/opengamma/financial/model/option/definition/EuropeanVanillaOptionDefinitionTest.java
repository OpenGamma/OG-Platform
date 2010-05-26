/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.ConstantInterestRateDiscountCurve;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class EuropeanVanillaOptionDefinitionTest {
  private static final double DIFF = 20;
  private static final double SPOT = 100;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 5, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 1));
  private static final OptionDefinition CALL = new EuropeanVanillaOptionDefinition(SPOT, EXPIRY, true);
  private static final OptionDefinition PUT = new EuropeanVanillaOptionDefinition(SPOT, EXPIRY, false);
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(new ConstantInterestRateDiscountCurve(0.05), 0., new ConstantVolatilitySurface(0.2), 100., DATE);
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
  public void testHashCodeAndEquals() {
    final OptionDefinition definition1 = new EuropeanVanillaOptionDefinition(SPOT, EXPIRY, true);
    assertEquals(definition1, CALL);
    assertEquals(definition1.hashCode(), CALL.hashCode());
    assertFalse(definition1.equals(PUT));
  }
}
