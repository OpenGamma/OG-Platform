/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

/**
 * 
 */
public class KrigingInterpolatorNDTest extends InterpolatorNDTestCase {
  private static final InterpolatorND INTERPOLATOR = new KrigingInterpolatorND(1.5);
  private static final double EPS = 1e-4;

  @Test(expected = IllegalArgumentException.class)
  public void testLowBeta() {
    new KrigingInterpolatorND(-1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighBeta() {
    new KrigingInterpolatorND(2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullValue() {
    INTERPOLATOR.interpolate(FLAT_DATA, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadData() {
    INTERPOLATOR.interpolate(FLAT_DATA, Arrays.asList(2., 3., 4., 5.));

  }

  @Test
  public void testInputs() {
    assertEquals(INTERPOLATOR.interpolate(FLAT_DATA, Arrays.asList(2., 3.4, 5.)), VALUE, EPS);
  }
}
