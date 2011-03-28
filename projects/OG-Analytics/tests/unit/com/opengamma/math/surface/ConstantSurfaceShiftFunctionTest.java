/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.surface;

import static org.testng.AssertJUnit.assertArrayEquals;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;

/**
 * 
 */
public class ConstantSurfaceShiftFunctionTest {
  private static final ConstantSurfaceShiftFunction F = new ConstantSurfaceShiftFunction();
  private static final double Z = 3;
  private static final ConstantDoublesSurface SURFACE = ConstantDoublesSurface.from(Z, "X");

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void test1() {
    F.evaluate(SURFACE, 2, 1, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void test2() {
    F.evaluate(SURFACE, 2, 1, 1, "A");
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void test3() {
    F.evaluate(SURFACE, new double[] {2}, new double[] {1}, new double[] {3});
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void test4() {
    F.evaluate(SURFACE, new double[] {2}, new double[] {1}, new double[] {3.}, "A");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSurface1() {
    F.evaluate(null, 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSurface2() {
    F.evaluate(null, 1, "B");
  }

  @Test
  public void test() {
    final double shift = 0.34;
    ConstantDoublesSurface shifted = F.evaluate(SURFACE, shift);
    assertArrayEquals(shifted.getZData(), new Double[] {Z + shift});
    assertEquals(shifted.getName(), "PARALLEL_SHIFT_X");
    final String newName = "Y";
    shifted = F.evaluate(SURFACE, shift, newName);
    assertArrayEquals(shifted.getZData(), new Double[] {Z + shift});
    assertEquals(shifted.getName(), newName);
  }

}
