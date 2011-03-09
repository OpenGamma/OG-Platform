/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 */
public class BisectionSingleRootFinderTest extends RealSingleRootFinderTestCase {
  private static final RealSingleRootFinder FINDER = new BisectionSingleRootFinder();

  @Test
  public void test() {
    testInputs(FINDER);
    assertEquals(FINDER.getRoot(F, 2.5, 3.5), 3, EPS);
    assertEquals(FINDER.getRoot(F, 1.5, 2.5), 2, EPS);
    assertEquals(FINDER.getRoot(F, 0.5, -1.5), -1, EPS);
  }
}
