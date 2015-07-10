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
public class KrigingInterpolatorDataBundleTest extends InterpolatorNDTestCase {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    new KrigingInterpolatorDataBundle(null, 1.5);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLowBeta() {
    new KrigingInterpolatorDataBundle(COS_EXP_DATA, -1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighBeta() {
    new KrigingInterpolatorDataBundle(COS_EXP_DATA, 2);
  }

  @Override
  protected RandomEngine getRandom() {
    return RANDOM;
  }

}
