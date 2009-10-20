/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class KrigingInterpolatorNDTest extends InterpolatorNDTest {
  private static final double EPS = 1e-4;

  @Test
  public void testInputs() {
    try {
      new KrigingInterpolatorND(-1);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      new KrigingInterpolatorND(2);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    final InterpolatorND interpolator = new KrigingInterpolatorND(1.5);
    super.testData(interpolator);
    try {
      interpolator.interpolate(FLAT_DATA, null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      interpolator.interpolate(FLAT_DATA, Arrays.asList(2., 3., 4., 5.));
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    assertEquals(interpolator.interpolate(FLAT_DATA, Arrays.asList(2., 3.4, 5.)).getResult(), VALUE, EPS);
  }
}
