/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.yield;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SimpleYieldConventionTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullName() {
    new SimpleYieldConvention(null);
  }

  @Test
  public void test() {
    final String name = "CONV";
    final SimpleYieldConvention convention = new SimpleYieldConvention(name);
    assertEquals(convention.getName(), name);
  }
}
