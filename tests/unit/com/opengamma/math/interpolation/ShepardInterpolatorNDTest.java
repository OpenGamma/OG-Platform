/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class ShepardInterpolatorNDTest extends InterpolatorNDTest {
  private static final InterpolatorND INTERPOLATOR = new ShepardInterpolatorND(1.3);

  @Test
  public void testInputs() {
    super.testData(INTERPOLATOR);
    try {
      INTERPOLATOR.interpolate(FLAT_DATA, null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      INTERPOLATOR.interpolate(FLAT_DATA, Arrays.asList(1., 2., 3., 4., 5.));
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
  }
}
