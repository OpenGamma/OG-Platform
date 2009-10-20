/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class ShepardInterpolatorNDTest extends InterpolatorNDTest {
  private static final InterpolatorND INTERPOLATOR = new ShepardInterpolatorND(1.3);
  private static final double EPS = 1e-1;

  @Test
  public void testInputs() {
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
    final List<Double> l = Arrays.asList(0.34, 0.21);
    assertEquals(INTERPOLATOR.interpolate(FLAT_DATA, l).getResult(), VALUE, EPS);
    final Double[] a = new Double[] { 0.89, 0.54, 0.34 };
    assertEquals(INTERPOLATOR.interpolate(DATA2, Arrays.asList(a)).getResult(), F2.evaluate(a), EPS);
  }
}
