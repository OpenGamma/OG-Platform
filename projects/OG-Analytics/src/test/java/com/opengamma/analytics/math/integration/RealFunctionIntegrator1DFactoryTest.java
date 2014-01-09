/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.integration;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class RealFunctionIntegrator1DFactoryTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
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
