/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.opengamma.math.interpolation.data.KrigingInterpolatorDataBundle;

/**
 * 
 */
public class KrigingInterpolatorNDTest extends InterpolatorNDTestCase {
  private static final double BETA = 1.5;
  private static final InterpolatorND<KrigingInterpolatorDataBundle> INTERPOLATOR = new KrigingInterpolatorND(BETA);

  @Test(expected = IllegalArgumentException.class)
  public void testLowBeta() {
    new KrigingInterpolatorND(-3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighBeta() {
    new KrigingInterpolatorND(10);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    INTERPOLATOR.interpolate(null, new double[] {1, 2});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPoint() {
    INTERPOLATOR.interpolate(INTERPOLATOR.getDataBundle(FLAT_DATA), null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongDimension() {
    INTERPOLATOR.interpolate(INTERPOLATOR.getDataBundle(FLAT_DATA), new double[] {1, 2});
  }

  @Test
  public void test() {
    InterpolatorND<KrigingInterpolatorDataBundle> other = new KrigingInterpolatorND(BETA);
    assertEquals(other, INTERPOLATOR);
    assertEquals(other.hashCode(), INTERPOLATOR.hashCode());
    other = new KrigingInterpolatorND(1.4);
    assertFalse(other.equals(INTERPOLATOR));
  }

  @Test
  //TODO if this interpolator cannot get the answer right then an exception should be thrown
  public void testFlat() {
    final double x1 = 10 * RANDOM.nextDouble();
    final double x2 = 10 * RANDOM.nextDouble();
    final double x3 = 10 * RANDOM.nextDouble();
    // Fails utterly for flat surface since the variogram function will be zero for all r
    final InterpolatorND<KrigingInterpolatorDataBundle> interpolator = new KrigingInterpolatorND(1.99);
    final KrigingInterpolatorDataBundle dataBundle = interpolator.getDataBundle(FLAT_DATA);
    assertEquals(INTERPOLATOR.interpolate(dataBundle, new double[] {x1, x2, x3}), 0, 0);
  }

  @Test
  public void testInterpolation() {
    final InterpolatorND<KrigingInterpolatorDataBundle> interpolator = new KrigingInterpolatorND(1.99);
    testCosExp(interpolator, 2e-2);
  }

}
