/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function.special;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class HeavisideFunctionTest {
  private static final Function1D<Double, Double> F = new HeavisideFunction();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull() {
    F.evaluate((Double) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testZero() {
    F.evaluate(0.);
  }
  @Test
  public void test() {
    assertEquals(F.evaluate(-2.), 0, 0);
    assertEquals(F.evaluate(-1e-15), 0, 0);
    assertEquals(F.evaluate(1e-15), 1, 0);
    assertEquals(F.evaluate(2.), 1, 0);
  }
}
