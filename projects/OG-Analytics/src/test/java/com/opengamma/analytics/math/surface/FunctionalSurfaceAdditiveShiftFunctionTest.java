/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.surface;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class FunctionalSurfaceAdditiveShiftFunctionTest {
  private static final Function<Double, Double> F = new Function<Double, Double>() {

    @Override
    public Double evaluate(final Double... xy) {
      return xy[0] + xy[1];
    }

  };
  private static final FunctionalDoublesSurface SURFACE = FunctionalDoublesSurface.from(F, "A");
  private static final FunctionalSurfaceAdditiveShiftFunction SHIFT = new FunctionalSurfaceAdditiveShiftFunction();

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void test1() {
    SHIFT.evaluate(SURFACE, 2, 1, 1);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void test2() {
    SHIFT.evaluate(SURFACE, 2, 1, 1, "A");
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void test3() {
    SHIFT.evaluate(SURFACE, new double[] {2 }, new double[] {1 }, new double[] {2. });
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void test4() {
    SHIFT.evaluate(SURFACE, new double[] {2 }, new double[] {1 }, new double[] {2. }, "A");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurve1() {
    SHIFT.evaluate(null, 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurve2() {
    SHIFT.evaluate(null, 1, "B");
  }

  @Test
  public void test() {
    final double shift = 0.34;
    FunctionalDoublesSurface shifted = SHIFT.evaluate(SURFACE, shift);
    for (int i = 0; i < 10; i++) {
      final double x = Math.random();
      final double y = Math.random();
      assertEquals(shifted.getZValue(x, y), F.evaluate(x, y) + shift, 1e-15);
    }
    assertEquals(shifted.getName(), "PARALLEL_SHIFT_A");
    final String newName = "Y";
    shifted = SHIFT.evaluate(SURFACE, shift, newName);
    for (int i = 0; i < 10; i++) {
      final double x = Math.random();
      final double y = Math.random();
      assertEquals(shifted.getZValue(x, y), F.evaluate(x, y) + shift, 1e-15);
    }
    assertEquals(shifted.getName(), newName);
  }

}
