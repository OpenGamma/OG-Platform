/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 */
public class UtilFunctionsTest {
  private static final double EPS = 1e-15;

  @Test
  public void testSquare() {
    for (int i = 0; i < 100; i++) {
      final double x = Math.random();
      assertEquals(UtilFunctions.square(x), x * x, EPS);
    }
  }

  @Test
  public void testCube() {
    for (int i = 0; i < 100; i++) {
      final double x = Math.random();
      assertEquals(UtilFunctions.cube(x), x * x * x, EPS);
    }
  }
}
