/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.yield;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 */
public class SimpleYieldConventionTest {

  @Test(expected = IllegalArgumentException.class)
  public void testNullName() {
    new SimpleYieldConvention(null);
  }

  @Test
  public void test() {
    final String name = "CONV";
    final SimpleYieldConvention convention = new SimpleYieldConvention(name);
    assertEquals(convention.getConventionName(), name);
  }
}
