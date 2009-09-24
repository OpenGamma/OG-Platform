/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import static org.junit.Assert.fail;

import java.util.HashMap;

import org.junit.Test;

import com.opengamma.util.Pair;

/**
 * 
 * @author emcleod
 */
public class VolatilitySurfaceTest {

  // private static final Interpolator2D INTERPOLATOR = new
  // LinearInterpolator2D();

  @Test
  public void testConstructor() {
    try {
      new VolatilitySurface(null, null);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      new VolatilitySurface(new HashMap<Pair<Double, Double>, Double>(), null);
      fail();
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }
}
