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
public class KroneckerDeltaFunctionTest {
  private static final KroneckerDeltaFunction F = new KroneckerDeltaFunction();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull1() {
    F.evaluate(null, 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull2() {
    F.evaluate(1, null);
  }

  @Test
  public void test() {
    assertEquals(F.evaluate(1, 1).intValue(), 1);
    assertEquals(F.evaluate(1, 2).intValue(), 0);
  }
}
