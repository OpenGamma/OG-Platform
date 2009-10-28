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
public class IncompleteBetaFunctionTest {
  private static final double EPS = 1e-9;

  @Test
  public void testInputs() {
    try {
      new IncompleteBetaFunction(-1, 0.5);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      new IncompleteBetaFunction(0.5, -1.2);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    final Function1D<Double, Double> f = new IncompleteBetaFunction(0.5, 0.5);
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
    final double a = Math.random();
    final double b = Math.random();
    final double x = Math.random();
    final Function1D<Double, Double> f1 = new IncompleteBetaFunction(a, b);
    final Function1D<Double, Double> f2 = new IncompleteBetaFunction(b, a);
    assertEquals(f1.evaluate(0.), 0, EPS);
    assertEquals(f1.evaluate(1.), 1, EPS);
    assertEquals(f1.evaluate(x), 1 - f2.evaluate(1 - x), EPS);
  }
}
