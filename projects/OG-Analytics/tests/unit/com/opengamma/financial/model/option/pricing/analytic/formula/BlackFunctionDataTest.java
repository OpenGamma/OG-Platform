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
public class BlackFunctionDataTest {
  private static final double F = 100;
  private static final double DF = 0.95;
  private static final double SIGMA = 0.23;
  private static final BlackFunctionData DATA = new BlackFunctionData(F, DF, SIGMA);

  @Test(expected = IllegalArgumentException.class)
  public void testHighDiscountFactor() {
    new BlackFunctionData(F, DF + 1, SIGMA);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLowDiscountFactor() {
    new BlackFunctionData(F, 0, SIGMA);
  }

  @Test
  public void test() {
    assertEquals(DATA.getDiscountFactor(), DF, 0);
    assertEquals(DATA.getForward(), F, 0);
    assertEquals(DATA.getBlackVolatility(), SIGMA, 0);
    BlackFunctionData other = new BlackFunctionData(F, DF, SIGMA);
    assertEquals(DATA, other);
    assertEquals(DATA.hashCode(), other.hashCode());
    other = new BlackFunctionData(F + 1, DF, SIGMA);
    assertFalse(other.equals(DATA));
    other = new BlackFunctionData(F, DF * 0.5, SIGMA);
    assertFalse(other.equals(DATA));
    other = new BlackFunctionData(F, DF, SIGMA + 0.1);
    assertFalse(other.equals(DATA));
  }
}
