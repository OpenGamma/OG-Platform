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
public class SupershareOptionDefinitionTest {
  private static final Expiry EXPIRY = new Expiry(DateUtils.getUTCDate(2010, 7, 1));
  private static final double LOWER = 10;
  private static final double UPPER = 30;
  private static final double SPOT = 20;
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(YieldCurve.from(ConstantDoublesCurve.from(0.1)), SPOT, new VolatilitySurface(ConstantDoublesSurface.from(0.2)), 20,
      DateUtils.getUTCDate(
          2009, 1, 1));
  private static final SupershareOptionDefinition OPTION = new SupershareOptionDefinition(EXPIRY, LOWER, UPPER);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeLowerBound() {
    new SupershareOptionDefinition(EXPIRY, -LOWER, UPPER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeUpperBound() {
    new SupershareOptionDefinition(EXPIRY, LOWER, -UPPER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadBounds() {
    new SupershareOptionDefinition(EXPIRY, UPPER, LOWER);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    OPTION.getPayoffFunction().getPayoff(null, null);
  }

  @Test
  public void test() {
    assertEquals(OPTION.getLowerBound(), LOWER, 0);
    assertEquals(OPTION.getUpperBound(), UPPER, 0);
    SupershareOptionDefinition other = new SupershareOptionDefinition(EXPIRY, LOWER, UPPER);
    assertEquals(other, OPTION);
    assertEquals(other.hashCode(), OPTION.hashCode());
    other = new SupershareOptionDefinition(new Expiry(DateUtils.getUTCDate(2011, 1, 1)), LOWER, UPPER);
    assertFalse(other.equals(OPTION));
    other = new SupershareOptionDefinition(EXPIRY, LOWER + 1, UPPER);
    assertFalse(other.equals(OPTION));
    other = new SupershareOptionDefinition(EXPIRY, LOWER, UPPER + 1);
    assertFalse(other.equals(OPTION));
  }

  @Test
  public void testExercise() {
    final OptionExerciseFunction<StandardOptionDataBundle> f = OPTION.getExerciseFunction();
    assertFalse(f.shouldExercise(DATA, null));
    StandardOptionDataBundle data = DATA.withSpot(5);
    assertFalse(f.shouldExercise(data, null));
    data = DATA.withSpot(35);
    assertFalse(f.shouldExercise(data, null));
  }

  @Test
  public void testPayoff() {
    final OptionPayoffFunction<StandardOptionDataBundle> f = OPTION.getPayoffFunction();
    assertEquals(f.getPayoff(DATA, null), SPOT / LOWER, 0);
    StandardOptionDataBundle data = DATA.withSpot(5);
    assertEquals(f.getPayoff(data, null), 0, 0);
    data = DATA.withSpot(35);
    assertEquals(f.getPayoff(data, null), 0, 0);
    data = DATA.withSpot(LOWER);
    assertEquals(f.getPayoff(data, null), LOWER / LOWER, 0);
    data = DATA.withSpot(UPPER);
    assertEquals(f.getPayoff(data, null), 0, 0);
  }
}
