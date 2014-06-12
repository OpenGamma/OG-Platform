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
public class AsymmetricPowerOptionDefinitionTest {
  private static final double DIFF = 30;
  private static final double STRIKE = 50;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 5, 1);
  private static final Expiry EXPIRY = new Expiry(DATE);
  private static final double POWER = 1.1;
  private static final OptionDefinition CALL = new AsymmetricPowerOptionDefinition(STRIKE, EXPIRY, POWER, true);
  private static final OptionDefinition PUT = new AsymmetricPowerOptionDefinition(STRIKE, EXPIRY, POWER, false);
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.05)), 0.05, new VolatilitySurface(ConstantDoublesSurface.from(0.1)),
      STRIKE, DateUtils.getUTCDate(2010, 1, 1));

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDataBundle() {
    CALL.getPayoffFunction().getPayoff(null, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativePower() {
    new AsymmetricPowerOptionDefinition(STRIKE, EXPIRY, -POWER, true);
  }

  @Test
  public void testExercise() {
    assertFalse(CALL.getExerciseFunction().shouldExercise(DATA, STRIKE + 10));
    assertFalse(CALL.getExerciseFunction().shouldExercise(DATA, STRIKE - 10));
    assertFalse(CALL.getExerciseFunction().shouldExercise(DATA.withSpot(STRIKE + DIFF), STRIKE));
    assertFalse(CALL.getExerciseFunction().shouldExercise(DATA.withSpot(STRIKE - DIFF), STRIKE));
    assertFalse(PUT.getExerciseFunction().shouldExercise(DATA, STRIKE + 10));
    assertFalse(PUT.getExerciseFunction().shouldExercise(DATA, STRIKE - 10));
    assertFalse(PUT.getExerciseFunction().shouldExercise(DATA.withSpot(STRIKE + DIFF), STRIKE));
    assertFalse(PUT.getExerciseFunction().shouldExercise(DATA.withSpot(STRIKE - DIFF), STRIKE));
  }

  @Test
  public void testPayoff() {
    final double eps = 1e-15;
    OptionPayoffFunction<StandardOptionDataBundle> payoff = CALL.getPayoffFunction();
    assertEquals(payoff.getPayoff(DATA.withSpot(STRIKE - DIFF), 0.), 0, eps);
    assertEquals(payoff.getPayoff(DATA.withSpot(STRIKE + DIFF), 0.), Math.pow(STRIKE + DIFF, POWER) - STRIKE, eps);
    payoff = PUT.getPayoffFunction();
    assertEquals(payoff.getPayoff(DATA.withSpot(STRIKE + DIFF), 0.), 0, eps);
    assertEquals(payoff.getPayoff(DATA.withSpot(STRIKE - DIFF), 0.), STRIKE - Math.pow(STRIKE - DIFF, POWER), eps);
  }

  @Test
  public void testEqualsAndHashCode() {
    final OptionDefinition call1 = new AsymmetricPowerOptionDefinition(STRIKE, EXPIRY, POWER, true);
    final OptionDefinition put1 = new AsymmetricPowerOptionDefinition(STRIKE, EXPIRY, POWER, false);
    final OptionDefinition call2 = new AsymmetricPowerOptionDefinition(STRIKE + DIFF, EXPIRY, POWER, true);
    final OptionDefinition put2 = new AsymmetricPowerOptionDefinition(STRIKE + DIFF, EXPIRY, POWER, false);
    final OptionDefinition call3 = new AsymmetricPowerOptionDefinition(STRIKE, new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 3)), POWER, true);
    final OptionDefinition put3 = new AsymmetricPowerOptionDefinition(STRIKE, new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, 3)), POWER, false);
    final OptionDefinition call4 = new AsymmetricPowerOptionDefinition(STRIKE, EXPIRY, POWER + 1, true);
    final OptionDefinition put4 = new AsymmetricPowerOptionDefinition(STRIKE, EXPIRY, POWER + 1, false);
    assertFalse(CALL.equals(PUT));
    assertEquals(call1, CALL);
    assertEquals(put1, PUT);
    assertEquals(call1.hashCode(), CALL.hashCode());
    assertEquals(put1.hashCode(), PUT.hashCode());
    assertFalse(call2.equals(CALL));
    assertFalse(put2.equals(PUT));
    assertFalse(call3.equals(CALL));
    assertFalse(put3.equals(PUT));
    assertFalse(call4.equals(CALL));
    assertFalse(put4.equals(PUT));
  }
}
