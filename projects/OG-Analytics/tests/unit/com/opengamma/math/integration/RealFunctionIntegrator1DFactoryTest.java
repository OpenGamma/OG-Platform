/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * 
 */
public class RealFunctionIntegrator1DFactoryTest {

  @Test(expected = IllegalArgumentException.class)
  public void testBadName() {
    RealFunctionIntegrator1DFactory.getIntegrator("a");
  }

  @Test
  public void testNullCalculator() {
    assertNull(RealFunctionIntegrator1DFactory.getIntegratorName(null));
  }

  @Test
  public void test() {
    assertEquals(RealFunctionIntegrator1DFactory.EXTENDED_TRAPEZOID, RealFunctionIntegrator1DFactory.getIntegratorName(RealFunctionIntegrator1DFactory
        .getIntegrator(RealFunctionIntegrator1DFactory.EXTENDED_TRAPEZOID)));
    assertEquals(RealFunctionIntegrator1DFactory.ROMBERG, RealFunctionIntegrator1DFactory.getIntegratorName(RealFunctionIntegrator1DFactory.getIntegrator(RealFunctionIntegrator1DFactory.ROMBERG)));
    assertEquals(RealFunctionIntegrator1DFactory.SIMPSON, RealFunctionIntegrator1DFactory.getIntegratorName(RealFunctionIntegrator1DFactory.getIntegrator(RealFunctionIntegrator1DFactory.SIMPSON)));
  }
}
