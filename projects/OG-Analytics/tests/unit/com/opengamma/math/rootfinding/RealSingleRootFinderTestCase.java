/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;

/**
 * 
 */
public abstract class RealSingleRootFinderTestCase {
  private static final Function1D<Double, Double> F = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return x * x * x - 4 * x * x + x + 6;
    }

  };
  private static final double EPS = 1e-9;

  public void testInputs(final RealSingleRootFinder finder) {
    try {
      finder.checkInputs(null, 1., 2.);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      finder.checkInputs(F, null, 2.);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      finder.checkInputs(F, 1., null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }

  public void test(final RealSingleRootFinder finder) {
    try {
      finder.getRoot(F, 10., 100.);
      fail();
    } catch (final MathException e) {
      // Expected
    }
    try {
      finder.getRoot(F, 1.5, 3.5);
      fail();
    } catch (final MathException e) {
      // Expected
    }
    assertEquals(finder.getRoot(F, 2.5, 3.5), 3, EPS);
    assertEquals(finder.getRoot(F, 1.5, 2.5), 2, EPS);
    assertEquals(finder.getRoot(F, 0.5, -1.5), -1, EPS);
  }
}
