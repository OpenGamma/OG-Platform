/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.definition;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

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
public class GapOptionDefinitionTest {
  private static final double DELTA = 10;
  private static final double STRIKE = 50;
  private static final Expiry EXPIRY = new Expiry(DateUtils.getUTCDate(2010, 1, 1));
  private static final boolean IS_CALL = true;
  private static final double PAYOFF_STRIKE = 55;
  private static final GapOptionDefinition CALL = new GapOptionDefinition(STRIKE, EXPIRY, true, PAYOFF_STRIKE);
  private static final GapOptionDefinition PUT = new GapOptionDefinition(STRIKE, EXPIRY, false, PAYOFF_STRIKE);
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.03)), 0.03, new VolatilitySurface(ConstantDoublesSurface.from(0.2)),
      STRIKE, DateUtils.getUTCDate(2009, 1, 1));

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativePayoffStrike() {
    new GapOptionDefinition(STRIKE, EXPIRY, IS_CALL, -PAYOFF_STRIKE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    CALL.getPayoffFunction().getPayoff(null, null);
  }

  @Test
  public void test() {
    GapOptionDefinition other = new GapOptionDefinition(STRIKE, EXPIRY, true, PAYOFF_STRIKE);
    assertEquals(other, CALL);
    assertEquals(other.hashCode(), CALL.hashCode());
    other = new GapOptionDefinition(STRIKE + 1, EXPIRY, true, PAYOFF_STRIKE);
    assertFalse(other.equals(CALL));
    other = new GapOptionDefinition(STRIKE, new Expiry(DateUtils.getUTCDate(2010, 3, 1)), true, PAYOFF_STRIKE);
    assertFalse(other.equals(CALL));
    other = new GapOptionDefinition(STRIKE, EXPIRY, false, PAYOFF_STRIKE);
    assertFalse(other.equals(CALL));
    other = new GapOptionDefinition(STRIKE, EXPIRY, true, PAYOFF_STRIKE + 1);
    assertFalse(other.equals(CALL));
  }

  @Test
  public void testExerciseFunction() {
    StandardOptionDataBundle data = DATA.withSpot(STRIKE + DELTA);
    assertFalse(CALL.getExerciseFunction().shouldExercise(data, null));
    assertFalse(PUT.getExerciseFunction().shouldExercise(data, null));
    data = DATA.withSpot(STRIKE - DELTA);
    assertFalse(CALL.getExerciseFunction().shouldExercise(data, null));
    assertFalse(PUT.getExerciseFunction().shouldExercise(data, null));
  }

  @Test
  public void testPayoffFunction() {
    assertEquals(CALL.getPayoffFunction().getPayoff(DATA, null), 0, 0);
    assertEquals(PUT.getPayoffFunction().getPayoff(DATA, null), 0, 0);
    double spot = STRIKE + DELTA;
    StandardOptionDataBundle data = DATA.withSpot(spot);
    assertEquals(CALL.getPayoffFunction().getPayoff(data, null), spot - PAYOFF_STRIKE, 0);
    assertEquals(PUT.getPayoffFunction().getPayoff(data, null), 0, 0);
    spot = STRIKE - DELTA;
    data = DATA.withSpot(spot);
    assertEquals(CALL.getPayoffFunction().getPayoff(data, null), 0, 0);
    assertEquals(PUT.getPayoffFunction().getPayoff(data, null), PAYOFF_STRIKE - spot, 0);
  }
}
