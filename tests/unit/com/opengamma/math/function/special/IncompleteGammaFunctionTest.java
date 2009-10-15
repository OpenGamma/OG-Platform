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
import com.opengamma.math.function.Function2D;

/**
 * 
 * @author emcleod
 */
public class IncompleteGammaFunctionTest {
  private static final Function2D<Double, Double> FUNCTION = new IncompleteGammaFunction();
  private static final double EPS = 1e-9;

  @Test
  public void testInput() {
    try {
      FUNCTION.evaluate(-1., 2.);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void testLimits() {
    assertEquals(FUNCTION.evaluate(Math.random(), 0.), 0, EPS);
    assertEquals(FUNCTION.evaluate(Math.random(), 100.), 1, EPS);
  }

  @Test
  public void test() {
    final Function1D<Double, Double> f = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return 1 - Math.exp(-x);
      }

    };
    final double x = 4.6;
    assertEquals(f.evaluate(x), FUNCTION.evaluate(1., x), EPS);
  }
}
