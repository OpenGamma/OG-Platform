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
public class LogOptionDefinitionTest {
  private static final double STRIKE = 120;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 5, 1);
  private static final Expiry EXPIRY = new Expiry(DATE);
  private static final LogOptionDefinition DEFINITION = new LogOptionDefinition(STRIKE, EXPIRY);
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.05)), 0.05, new VolatilitySurface(ConstantDoublesSurface.from(0.1)),
      STRIKE, DATE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPayoffWithNullData() {
    DEFINITION.getPayoffFunction().getPayoff(null, null);
  }

  @Test
  public void testExercise() {
    final OptionExerciseFunction<StandardOptionDataBundle> exercise = DEFINITION.getExerciseFunction();
    assertFalse(exercise.shouldExercise(DATA, STRIKE + 1));
    assertFalse(exercise.shouldExercise(DATA, STRIKE - 1));
  }

  @Test
  public void testPayoff() {
    final OptionPayoffFunction<StandardOptionDataBundle> payoff = DEFINITION.getPayoffFunction();
    assertEquals(payoff.getPayoff(DATA.withSpot(STRIKE - 10), 0.), 0, 0);
    assertEquals(payoff.getPayoff(DATA.withSpot(STRIKE + 10), 0.), Math.log((STRIKE + 10) / STRIKE), 0);
  }

  @Test
  public void testHashCodeAndEquals() {
    OptionDefinition definition = new LogOptionDefinition(STRIKE, EXPIRY);
    assertEquals(definition, DEFINITION);
    assertEquals(definition.hashCode(), DEFINITION.hashCode());
    definition = new LogOptionDefinition(STRIKE + 1, EXPIRY);
    assertFalse(definition.equals(DEFINITION));
    definition = new LogOptionDefinition(STRIKE, new Expiry(EXPIRY.getExpiry().plusDays(3)));
    assertFalse(definition.equals(DEFINITION));
    definition = new EuropeanVanillaOptionDefinition(STRIKE, EXPIRY, true);
    assertFalse(definition.equals(DEFINITION));
  }
}
