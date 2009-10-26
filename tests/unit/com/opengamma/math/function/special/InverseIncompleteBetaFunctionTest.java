/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.function.special;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 */
public class InverseIncompleteBetaFunctionTest {
  private static final double EPS = 1e-9;

  @Test
  public void testInputs() {
    try {
      new InverseIncompleteBetaFunction(-1, 0.5);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      new InverseIncompleteBetaFunction(0.5, -1.2);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    final Function1D<Double, Double> f = new InverseIncompleteBetaFunction(0.5, 0.5);
    try {
      f.evaluate(-0.5);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      f.evaluate(1.5);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void test() {
    final double a = 0.35;
    final double b = 6.7;
    final double x = Math.random();
    final Function1D<Double, Double> beta = new IncompleteBetaFunction(a, b);
    final Function1D<Double, Double> inverse = new InverseIncompleteBetaFunction(a, b);
    assertEquals(beta.evaluate(inverse.evaluate(x)), x, EPS);
    assertEquals(inverse.evaluate(beta.evaluate(x)), x, EPS);
  }
}
