/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ShepardInterpolatorNDTest extends InterpolatorNDTestCase {
  private static final InterpolatorND INTERPOLATOR = new ShepardInterpolatorND(3.0);
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    INTERPOLATOR.interpolate(null, new double[] {1, 2});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPoint() {
    INTERPOLATOR.interpolate(INTERPOLATOR.getDataBundle(FLAT_DATA), null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongDimension() {
    INTERPOLATOR.interpolate(INTERPOLATOR.getDataBundle(FLAT_DATA), new double[] {1, 2});
  }

  @Test
  public void testInterpolation() {
    assertCosExp(INTERPOLATOR, 1e-1); // fairly awful interpolator
    assertFlat(INTERPOLATOR, 1e-12);
  }

  @Override
  protected RandomEngine getRandom() {
    return RANDOM;
  }

}
