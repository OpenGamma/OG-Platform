/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation.data;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.math.interpolation.InterpolatorNDTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class RadialBasisFunctionInterpolatorDataBundleTest extends InterpolatorNDTestCase {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFunction() {
    new RadialBasisFunctionInterpolatorDataBundle(FLAT_DATA, null, false);
  }

  @Override
  protected RandomEngine getRandom() {
    return RANDOM;
  }

}
