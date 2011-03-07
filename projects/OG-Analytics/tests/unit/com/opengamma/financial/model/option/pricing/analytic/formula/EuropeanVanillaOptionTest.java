/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.formula;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * 
 */
public class EuropeanVanillaOptionTest {
  private static final double K = 100;
  private static final double T = 0.5;
  private static final boolean IS_CALL = true;
  private static final EuropeanVanillaOption OPTION = new EuropeanVanillaOption(K, T, IS_CALL);

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeStrike() {
    new EuropeanVanillaOption(-K, T, IS_CALL);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeTime() {
    new EuropeanVanillaOption(K, -T, IS_CALL);
  }

  @Test
  public void test() {
    assertEquals(OPTION.getK(), K, 0);
    assertEquals(OPTION.getT(), T, 0);
    assertEquals(OPTION.isCall(), IS_CALL);
    EuropeanVanillaOption other = new EuropeanVanillaOption(K, T, IS_CALL);
    assertEquals(other, OPTION);
    assertEquals(other.hashCode(), OPTION.hashCode());
    other = new EuropeanVanillaOption(K + 1, T, IS_CALL);
    assertFalse(other.equals(OPTION));
    other = new EuropeanVanillaOption(K, T + 1, IS_CALL);
    assertFalse(other.equals(OPTION));
    other = new EuropeanVanillaOption(K, T, !IS_CALL);
    assertFalse(other.equals(OPTION));
  }
}
