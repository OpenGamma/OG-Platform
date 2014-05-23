/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ConstantCurveShiftFunctionTest {
  private static final ConstantCurveShiftFunction F = new ConstantCurveShiftFunction();
  private static final double Y = 3;
  private static final ConstantDoublesCurve CURVE = ConstantDoublesCurve.from(Y, "X");

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void test1() {
    F.evaluate(CURVE, 2, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void test2() {
    F.evaluate(CURVE, 2, 1, "A");
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void test3() {
    F.evaluate(CURVE, new double[] {2}, new double[] {1});
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void test4() {
    F.evaluate(CURVE, new double[] {2}, new double[] {1}, "A");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurve1() {
    F.evaluate(null, 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurve2() {
    F.evaluate(null, 1, "B");
  }

  @Test
  public void test() {
    final double shift = 0.34;
    ConstantDoublesCurve shifted = F.evaluate(CURVE, shift);
    assertArrayEquals(shifted.getYData(), new Double[] {Y + shift});
    assertEquals(shifted.getName(), "PARALLEL_SHIFT_X");
    final String newName = "Y";
    shifted = F.evaluate(CURVE, shift, newName);
    assertArrayEquals(shifted.getYData(), new Double[] {Y + shift});
    assertEquals(shifted.getName(), newName);
  }
}
