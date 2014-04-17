/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.test.TestGroup;

/**
 * Abstract test.
 */
@Test(groups = TestGroup.UNIT)
public abstract class RealSingleRootFinderTestCase {
  protected static final Function1D<Double, Double> F = new Function1D<Double, Double>() {

    @Override
    public Double evaluate(final Double x) {
      return x * x * x - 4 * x * x + x + 6;
    }

  };
  protected static final double EPS = 1e-9;

  protected abstract RealSingleRootFinder getRootFinder();
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFunction() {
    getRootFinder().checkInputs(null, 1., 2.);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullLower() {
    getRootFinder().checkInputs(F, null, 2.);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUpper() {
    getRootFinder().checkInputs(F, 1., null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOutsideRoots() {
    getRootFinder().getRoot(F, 10., 100.);
  }
  
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBracketTwoRoots() {
    getRootFinder().getRoot(F, 1.5, 3.5);
  }
  
  @Test
  public void test() {
    RealSingleRootFinder finder = getRootFinder();
    assertEquals(finder.getRoot(F, 2.5, 3.5), 3, EPS);
    assertEquals(finder.getRoot(F, 1.5, 2.5), 2, EPS);
    assertEquals(finder.getRoot(F, -1.5, 0.5), -1, EPS);
  }
}
