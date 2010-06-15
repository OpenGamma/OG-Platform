/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.Arrays;

import org.junit.Test;

/**
 * 
 */
public class ShepardInterpolatorNDTest extends InterpolatorNDTestCase {
  private static final InterpolatorND INTERPOLATOR = new ShepardInterpolatorND(1.3);

  @Test(expected = IllegalArgumentException.class)
  public void testNullValue() {
    INTERPOLATOR.interpolate(FLAT_DATA, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongDimenion() {
    INTERPOLATOR.interpolate(FLAT_DATA, Arrays.asList(1., 2., 3., 4., 5.));
  }

  @Test
  public void testInputs() {
    super.testData(INTERPOLATOR);
  }
}
