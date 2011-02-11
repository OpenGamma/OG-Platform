/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
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
public class InverseMultiquadraticRadialBasisFunctionTest {
  private static final double X1 = 2.4;

  @Test
  public void test() {
    InverseMultiquadraticRadialBasisFunction f = new InverseMultiquadraticRadialBasisFunction(X1);
    InverseMultiquadraticRadialBasisFunction other = new InverseMultiquadraticRadialBasisFunction(X1);
    assertEquals(other, f);
    assertEquals(other.hashCode(), f.hashCode());
    f = new InverseMultiquadraticRadialBasisFunction();
    other = new InverseMultiquadraticRadialBasisFunction(1);
    assertEquals(other, f);
    other = new InverseMultiquadraticRadialBasisFunction(X1);
    assertFalse(other.equals(f));
    for (int i = 0; i < 10; i++) {
      final double x = Math.random();
      assertEquals(f.evaluate(x), 1. / Math.sqrt(x * x + 1), 0);
      assertEquals(other.evaluate(x), 1. / Math.sqrt(x * x + X1 * X1), 0);
    }
  }
}
