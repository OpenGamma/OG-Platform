/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding;

import com.opengamma.analytics.math.rootfinding.BisectionSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.RealSingleRootFinder;


/**
 * 
 */
public class BisectionSingleRootFinderTest extends RealSingleRootFinderTestCase {
  private static final RealSingleRootFinder FINDER = new BisectionSingleRootFinder();

  @Override
  protected RealSingleRootFinder getRootFinder() {
    return FINDER;
  }

}
