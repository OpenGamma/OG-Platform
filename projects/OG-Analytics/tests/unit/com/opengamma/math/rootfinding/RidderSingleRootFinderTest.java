/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import org.junit.Test;

/**
 * 
 */
public class RidderSingleRootFinderTest extends RealSingleRootFinderTestCase {
  private static final RealSingleRootFinder FINDER = new RidderSingleRootFinder();

  @Test
  public void test() {
    testInputs(FINDER);
    test(FINDER);
  }
}
