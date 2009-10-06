/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.function.special;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.function.Function2D;

/**
 * 
 * @author emcleod
 */
public class IncompleteGammaFunctionTest {
  private static final Function2D<Double, Double> FUNCTION = new IncompleteGammaFunction();
  private static final double EPS = 1e-9;

  @Test
  public void testLimits() {
    assertEquals(FUNCTION.evaluate(Math.random(), 0.), 0, EPS);
    assertEquals(FUNCTION.evaluate(Math.random(), 100.), 1, EPS);
  }
}
