/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import javax.time.Instant;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class ConstantVolatilitySurfaceTest {
  private static final double EPS = 1e-15;

  @Test
  public void test() {
    double sigma = 0.3;
    VolatilitySurface surface = new ConstantVolatilitySurface(Instant.millisInstant(1000), sigma);
    try {
      surface.getInterpolator();
      fail();
    } catch (UnsupportedOperationException e) {
      // Expected
    }
    double t = 2;
    double k = 50;
    assertEquals(sigma, surface.getVolatility(t, k), EPS);
  }
}
