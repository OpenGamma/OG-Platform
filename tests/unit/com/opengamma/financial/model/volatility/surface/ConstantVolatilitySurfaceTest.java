/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class ConstantVolatilitySurfaceTest {
  private static final double EPS = 1e-15;

  @Test
  public void test() {
    final double sigma = 0.3;
    final VolatilitySurface surface = new ConstantVolatilitySurface(sigma);
    final double t = 2;
    final double k = 50;
    assertEquals(sigma, surface.getVolatility(t, k), EPS);
  }
}
