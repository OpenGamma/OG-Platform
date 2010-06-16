/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * 
 */
public class InterpolationResultTest {
  private static final double X = 3;

  @Test
  public void test() {
    final InterpolationResult result1 = new InterpolationResult(X);
    final InterpolationResult result2 = new InterpolationResult(X);
    assertEquals(result1.getResult(), X, 0);
    assertEquals(result1, result2);
    assertEquals(result1.hashCode(), result2.hashCode());
    assertFalse(result1.equals(new InterpolationResult(X + 1)));
  }
}
