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
public class NaturalLogGammaFunctionTest {
  private static final Function1D<Double, Double> LN_GAMMA = new NaturalLogGammaFunction();
  private static final double EPS = 1e-9;

  @Test
  public void testNegativeNumber() {
    try {
      LN_GAMMA.evaluate(-0.1);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void testRecurrence() {
    double z = 12;
    double gamma = getGammaFunction(LN_GAMMA.evaluate(z));
    assertEquals(getGammaFunction(LN_GAMMA.evaluate(z + 1)), z * gamma, gamma * EPS);
    z = 11.34;
    gamma = getGammaFunction(LN_GAMMA.evaluate(z));
    assertEquals(getGammaFunction(LN_GAMMA.evaluate(z + 1)), z * gamma, gamma * EPS);
  }

  @Test
  public void testIntegerArgument() {
    final int x = 5;
    final double factorial = 24;
    assertEquals(getGammaFunction(LN_GAMMA.evaluate(Double.valueOf(x))), factorial, EPS);
  }

  private double getGammaFunction(final double x) {
    return Math.exp(x);
  }
}
