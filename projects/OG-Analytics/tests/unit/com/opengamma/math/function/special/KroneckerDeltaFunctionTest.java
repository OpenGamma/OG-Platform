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
public class KroneckerDeltaFunctionTest {
  private static final KroneckerDeltaFunction F = new KroneckerDeltaFunction();

  @Test(expected = IllegalArgumentException.class)
  public void testNull() {
    F.evaluate((Integer[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLargeArray() {
    F.evaluate(1, 2, 3);
  }

  @Test
  public void test() {
    assertEquals(F.evaluate(1, 1).intValue(), 1);
    assertEquals(F.evaluate(1, 2).intValue(), 0);
  }
}
