/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class RidderSingleRootFinderTest extends RealSingleRootFinderTestCase {
  private static final RealSingleRootFinder FINDER = new RidderSingleRootFinder();

  @Test
  public void test() {
    testInputs(FINDER);
    test(FINDER);
  }
}
