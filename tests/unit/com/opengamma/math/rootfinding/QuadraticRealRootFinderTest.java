/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.opengamma.math.function.PolynomialFunction1D;

/**
 * 
 */
public class QuadraticRealRootFinderTest {
  private static final double EPS = 1e-9;
  private static final PolynomialFunction1D F = new PolynomialFunction1D(new double[] {12., 7., 1.});
  private static final Polynomial1DRootFinder<Double> FINDER = new QuadraticRealRootFinder();

  @Test
  public void test() {
    try {
      FINDER.getRoots(null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      FINDER.getRoots(new PolynomialFunction1D(new double[] {1., 2., 3., 4.}));
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      FINDER.getRoots(new PolynomialFunction1D(new double[] {12., 1., 12.}));
      fail();
    } catch (final RootNotFoundException e) {
      // Expected
    }
    final Double[] roots = FINDER.getRoots(F);
    assertEquals(roots[0], -4.0, EPS);
    assertEquals(roots[1], -3.0, EPS);
  }
}
