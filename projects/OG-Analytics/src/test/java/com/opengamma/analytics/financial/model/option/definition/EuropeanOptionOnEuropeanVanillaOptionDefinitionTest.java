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
public class EuropeanOptionOnEuropeanVanillaOptionDefinitionTest {
  private static final double UNDERLYING_STRIKE = 70;
  private static final Expiry UNDERLYING_EXPIRY = new Expiry(DateUtils.getUTCDate(2011, 7, 1));
  private static final double STRIKE = 4;
  private static final double SPOT = 50;
  private static final Expiry EXPIRY = new Expiry(DateUtils.getUTCDate(2011, 6, 1));
  private static final EuropeanVanillaOptionDefinition UNDERLYING = new EuropeanVanillaOptionDefinition(UNDERLYING_STRIKE, UNDERLYING_EXPIRY, true);
  private static final EuropeanOptionOnEuropeanVanillaOptionDefinition OPTION = new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, true, UNDERLYING);
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.)), 0, new VolatilitySurface(ConstantDoublesSurface.from(0.)), SPOT,
      DateUtils.getUTCDate(2010, 7, 1));

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying() {
    new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, true, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongUnderlying() {
    new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, true, new EuropeanVanillaOptionDefinition(UNDERLYING_STRIKE, new Expiry(DateUtils.getUTCDate(2010, 12, 1)), false));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    OPTION.getPayoffFunction().getPayoff(null, null);
  }

  @Test
  public void testHashCodeAndEquals() {
    assertEquals(UNDERLYING, OPTION.getUnderlyingOption());
    EuropeanOptionOnEuropeanVanillaOptionDefinition other = new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, true, UNDERLYING);
    assertEquals(OPTION, other);
    assertEquals(OPTION.hashCode(), other.hashCode());
    other = new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, true, UNDERLYING_STRIKE, UNDERLYING_EXPIRY, true);
    assertEquals(OPTION, other);
    assertEquals(OPTION.hashCode(), other.hashCode());
    other = new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, true, new EuropeanVanillaOptionDefinition(STRIKE, UNDERLYING_EXPIRY, false));
    assertFalse(OPTION.equals(other));
  }

  @Test
  public void testExerciseFunction() {
    assertFalse(OPTION.getExerciseFunction().shouldExercise(DATA, null));
    assertFalse(new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, false, UNDERLYING).getExerciseFunction().shouldExercise(DATA, null));
  }

  @Test
  public void testPayoffFunction() {
    final double delta = 10;
    EuropeanVanillaOptionDefinition underlying = new EuropeanVanillaOptionDefinition(SPOT + delta, UNDERLYING_EXPIRY, true);
    EuropeanOptionOnEuropeanVanillaOptionDefinition option = new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, true, underlying);
    assertEquals(option.getPayoffFunction().getPayoff(DATA, null), 0, 0);
    option = new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, false, underlying);
    assertEquals(option.getPayoffFunction().getPayoff(DATA, null), STRIKE, 0);
    underlying = new EuropeanVanillaOptionDefinition(SPOT - delta, UNDERLYING_EXPIRY, true);
    option = new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, true, underlying);
    assertEquals(option.getPayoffFunction().getPayoff(DATA, null), delta - STRIKE, 0);
    option = new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, false, underlying);
    assertEquals(option.getPayoffFunction().getPayoff(DATA, null), 0, 0);
    underlying = new EuropeanVanillaOptionDefinition(SPOT + delta, UNDERLYING_EXPIRY, false);
    option = new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, true, underlying);
    assertEquals(option.getPayoffFunction().getPayoff(DATA, null), delta - STRIKE, 0);
    option = new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, false, underlying);
    assertEquals(option.getPayoffFunction().getPayoff(DATA, null), 0, 0);
    underlying = new EuropeanVanillaOptionDefinition(SPOT + delta, UNDERLYING_EXPIRY, false);
    option = new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, true, underlying);
    assertEquals(option.getPayoffFunction().getPayoff(DATA, null), delta - STRIKE, 0);
    option = new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, false, underlying);
    assertEquals(option.getPayoffFunction().getPayoff(DATA, null), 0, 0);
  }
}
