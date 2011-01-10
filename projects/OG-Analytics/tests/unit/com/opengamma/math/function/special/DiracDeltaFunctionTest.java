/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.function.special;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 */
public class DiracDeltaFunctionTest {
  private static final DiracDeltaFunction F = new DiracDeltaFunction();

  @Test(expected = IllegalArgumentException.class)
  public void testNull() {
    F.evaluate((Double) null);
  }

  @Test
  public void test() {
    assertEquals(Double.POSITIVE_INFINITY, F.evaluate(0.), 0);
    assertEquals(Double.POSITIVE_INFINITY, F.evaluate(1e-20), 0);
    assertEquals(Double.POSITIVE_INFINITY, F.evaluate(-1e-20), 0);
    assertEquals(0, F.evaluate(1e-15), 0);
    assertEquals(0, F.evaluate(-1e-15), 0);
  }
}
