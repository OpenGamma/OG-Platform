/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.function.special;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.opengamma.math.function.Function2D;

/**
 * 
 * @author emcleod
 */
public class InverseIncompleteGammaFunctionTest {
  private static final Function2D<Double, Double> INCOMPLETE_GAMMA = new IncompleteGammaFunction();
  private static final Function2D<Double, Double> INVERSE = new InverseIncompleteGammaFunction();
  private static final double EPS = 1e-12;

  @Test
  public void test() {
    final double a = 4.5;
    try {
      INVERSE.evaluate(-a, 0.4);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      INVERSE.evaluate(a, 5.6);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    final double x = 2.4;
    assertEquals(x, INVERSE.evaluate(a, INCOMPLETE_GAMMA.evaluate(a, x)), EPS);
  }
}
