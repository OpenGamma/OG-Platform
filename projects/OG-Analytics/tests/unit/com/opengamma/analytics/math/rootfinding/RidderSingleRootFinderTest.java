/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.rootfinding;

import com.opengamma.analytics.math.rootfinding.RealSingleRootFinder;
import com.opengamma.analytics.math.rootfinding.RidderSingleRootFinder;


/**
 * 
 */
public class RidderSingleRootFinderTest extends RealSingleRootFinderTestCase {
  private static final RealSingleRootFinder FINDER = new RidderSingleRootFinder();

  @Override
  protected RealSingleRootFinder getRootFinder() {
    return FINDER;
  }
}
