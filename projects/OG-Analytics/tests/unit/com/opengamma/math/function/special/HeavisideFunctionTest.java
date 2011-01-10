/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.function.special;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class HeavisideFunctionTest {
  private static final Function1D<Double, Double> F = new HeavisideFunction();

  @Test(expected = IllegalArgumentException.class)
  public void testNull() {
    F.evaluate((Double) null);
  }

  @Test
  public void test() {
    assertEquals(F.evaluate(-2.), 0, 0);
    assertEquals(F.evaluate(-1e-15), 0, 0);
    assertEquals(F.evaluate(0.), 1, 0);
    assertEquals(F.evaluate(1e-15), 1, 0);
    assertEquals(F.evaluate(2.), 1, 0);
  }
}
