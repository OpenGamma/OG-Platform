/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function.special;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DiracDeltaFunctionTest {
  private static final DiracDeltaFunction F = new DiracDeltaFunction();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    F.evaluate((Double) null);
  }

  @Test
  public void test() {
    assertEquals(Double.POSITIVE_INFINITY, F.evaluate(0.), 0);
    assertEquals(Double.POSITIVE_INFINITY, F.evaluate(1e-20), 0);
    assertEquals(Double.POSITIVE_INFINITY, F.evaluate(-1e-20), 0);
    assertEquals(0, F.evaluate(1e-15), 0);
    assertEquals(0, F.evaluate(-1e-15), 0);
  }
}
