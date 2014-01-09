/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.math.interpolation.data.InterpolatorNDDataBundle;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class KrigingInterpolatorNDTest extends InterpolatorNDTestCase {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final double BETA = 1.5;
  private static final InterpolatorND INTERPOLATOR = new KrigingInterpolatorND(BETA);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLowBeta() {
    new KrigingInterpolatorND(-3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighBeta() {
    new KrigingInterpolatorND(10);
  }

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
  public void test() {
    InterpolatorND other = new KrigingInterpolatorND(BETA);
    assertEquals(other, INTERPOLATOR);
    assertEquals(other.hashCode(), INTERPOLATOR.hashCode());
    other = new KrigingInterpolatorND(1.4);
    assertFalse(other.equals(INTERPOLATOR));
  }

  @Test
  //TODO if this interpolator cannot get the answer right then an exception should be thrown
  public void testFlat() {
    final RandomEngine random = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
    final double x1 = 10 * random.nextDouble();
    final double x2 = 10 * random.nextDouble();
    final double x3 = 10 * random.nextDouble();
    // Fails utterly for flat surface since the variogram function will be zero for all r
    final InterpolatorND interpolator = new KrigingInterpolatorND(1.99);
    final InterpolatorNDDataBundle dataBundle = interpolator.getDataBundle(FLAT_DATA);
    assertEquals(INTERPOLATOR.interpolate(dataBundle, new double[] {x1, x2, x3}), 0, 0);
  }

  @Test
  public void testInterpolation() {
    final InterpolatorND interpolator = new KrigingInterpolatorND(1.99);
    assertCosExp(interpolator, 2e-2);
  }

  @Override
  protected RandomEngine getRandom() {
    return RANDOM;
  }
}
