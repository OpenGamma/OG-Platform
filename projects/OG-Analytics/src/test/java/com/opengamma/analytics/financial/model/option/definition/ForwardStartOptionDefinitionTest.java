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
import com.opengamma.analytics.financial.model.option.Moneyness;
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
public class ForwardStartOptionDefinitionTest {
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 6, 10);
  private static final Expiry EXPIRY = new Expiry(DateUtils.getUTCDate(2010, 7, 1));
  private static final Expiry START = new Expiry(DateUtils.getUTCDate(2010, 6, 1));
  private static final double PERCENT = 0.4;
  private static final Moneyness MONEYNESS = Moneyness.ATM;
  private static final double SPOT = 100;
  private static final ForwardStartOptionDefinition ATM_CALL = new ForwardStartOptionDefinition(EXPIRY, true, START, PERCENT, MONEYNESS);
  private static final ForwardStartOptionDefinition ATM_PUT = new ForwardStartOptionDefinition(EXPIRY, false, START, PERCENT, MONEYNESS);
  private static final ForwardStartOptionDefinition ITM_CALL = new ForwardStartOptionDefinition(EXPIRY, true, START, PERCENT, Moneyness.ITM);
  private static final ForwardStartOptionDefinition ITM_PUT = new ForwardStartOptionDefinition(EXPIRY, false, START, PERCENT, Moneyness.ITM);
  private static final ForwardStartOptionDefinition OTM_CALL = new ForwardStartOptionDefinition(EXPIRY, true, START, PERCENT, Moneyness.OTM);
  private static final ForwardStartOptionDefinition OTM_PUT = new ForwardStartOptionDefinition(EXPIRY, false, START, PERCENT, Moneyness.OTM);
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.03)), 0, new VolatilitySurface(ConstantDoublesSurface.from(0.2)), SPOT,
      DATE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStartTime() {
    new ForwardStartOptionDefinition(EXPIRY, true, null, PERCENT, MONEYNESS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativePercent() {
    new ForwardStartOptionDefinition(EXPIRY, true, START, -PERCENT, MONEYNESS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMoneyness() {
    new ForwardStartOptionDefinition(EXPIRY, true, START, PERCENT, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongStartTime() {
    new ForwardStartOptionDefinition(START, true, EXPIRY, PERCENT, MONEYNESS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadPayoffTime() {
    ATM_CALL.getPayoffFunction().getPayoff(DATA.withDate(DateUtils.getUTCDate(2009, 1, 1)), 0.);
  }

  @Test
  public void testGetters() {
    assertEquals(ATM_CALL.getStartTime(), START);
    assertEquals(ATM_CALL.getPercent(), PERCENT, 0);
    assertEquals(ATM_CALL.getMoneyness(), MONEYNESS);
  }

  @Test
  public void testHashCodeAndEquals() {
    ForwardStartOptionDefinition other = new ForwardStartOptionDefinition(EXPIRY, true, START, PERCENT, MONEYNESS);
    assertEquals(other, ATM_CALL);
    assertEquals(other.hashCode(), ATM_CALL.hashCode());
    other = new ForwardStartOptionDefinition(new Expiry(DateUtils.getUTCDate(2011, 1, 1)), true, START, PERCENT, MONEYNESS);
    assertFalse(other.equals(ATM_CALL));
    other = new ForwardStartOptionDefinition(EXPIRY, false, START, PERCENT, MONEYNESS);
    assertFalse(other.equals(ATM_CALL));
    other = new ForwardStartOptionDefinition(EXPIRY, true, new Expiry(DATE), PERCENT, MONEYNESS);
    assertFalse(other.equals(ATM_CALL));
    other = new ForwardStartOptionDefinition(EXPIRY, true, START, PERCENT + 0.1, MONEYNESS);
    assertFalse(other.equals(ATM_CALL));
    other = new ForwardStartOptionDefinition(EXPIRY, true, START, PERCENT, Moneyness.OTM);
    assertFalse(other.equals(ATM_CALL));
  }

  @Test
  public void testAlpha() {
    assertEquals(ATM_CALL.getAlpha(), 1, 0);
    assertEquals(ATM_PUT.getAlpha(), 1, 0);
    assertEquals(ITM_CALL.getAlpha(), 0.6, 0);
    assertEquals(ITM_PUT.getAlpha(), 1.4, 0);
    assertEquals(OTM_CALL.getAlpha(), 1.4, 0);
    assertEquals(OTM_PUT.getAlpha(), 0.6, 0);
  }

  @Test
  public void testPayoffFunction() {
    assertEquals(ATM_CALL.getPayoffFunction().getPayoff(DATA, 0.), 0, 0);
    assertEquals(ATM_PUT.getPayoffFunction().getPayoff(DATA, 0.), 0, 0);
    assertEquals(ITM_CALL.getPayoffFunction().getPayoff(DATA, 0.), SPOT * PERCENT, 0);
    assertEquals(ITM_PUT.getPayoffFunction().getPayoff(DATA, 0.), SPOT * PERCENT, 0);
    assertEquals(OTM_CALL.getPayoffFunction().getPayoff(DATA, 0.), 0, 0);
    assertEquals(OTM_CALL.getPayoffFunction().getPayoff(DATA, 0.), 0, 0);
  }

  @Test
  public void testExerciseFunction() {
    assertFalse(ATM_CALL.getExerciseFunction().shouldExercise(DATA, 0.));
    assertFalse(ATM_PUT.getExerciseFunction().shouldExercise(DATA, 0.));
    assertFalse(ITM_CALL.getExerciseFunction().shouldExercise(DATA, 0.));
    assertFalse(ITM_PUT.getExerciseFunction().shouldExercise(DATA, 0.));
    assertFalse(OTM_CALL.getExerciseFunction().shouldExercise(DATA, 0.));
    assertFalse(OTM_PUT.getExerciseFunction().shouldExercise(DATA, 0.));
  }
}
