/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.surface;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.function.Function;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class FunctionalSurfaceShiftFunctionTest {
  private static final Function<DoublesPair, Double> F = new Function<DoublesPair, Double>() {

    @Override
    public Double evaluate(final DoublesPair... x) {
      final DoublesPair p = x[0];
      return p.getFirst() + p.getSecond();
    }

  };
  private static final FunctionalDoublesSurface SURFACE = FunctionalDoublesSurface.from(F, "A");
  private static final FunctionalSurfaceShiftFunction SHIFT = new FunctionalSurfaceShiftFunction();

  @Test(expected = UnsupportedOperationException.class)
  public void test1() {
    SHIFT.evaluate(SURFACE, 2, 1, 1);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void test2() {
    SHIFT.evaluate(SURFACE, 2, 1, 1, "A");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void test3() {
    SHIFT.evaluate(SURFACE, new double[] {2}, new double[] {1}, new double[] {2.});
  }

  @Test(expected = UnsupportedOperationException.class)
  public void test4() {
    SHIFT.evaluate(SURFACE, new double[] {2}, new double[] {1}, new double[] {2.}, "A");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurve1() {
    SHIFT.evaluate(null, 1);
  }

  @Test(expected = IllegalArgumentException.class)
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
      assertEquals(shifted.getZValue(x, y), F.evaluate(DoublesPair.of(x, y)) + shift, 1e-15);
    }
    assertEquals(shifted.getName(), "PARALLEL_SHIFT_A");
    final String newName = "Y";
    shifted = SHIFT.evaluate(SURFACE, shift, newName);
    for (int i = 0; i < 10; i++) {
      final double x = Math.random();
      final double y = Math.random();
      assertEquals(shifted.getZValue(x, y), F.evaluate(DoublesPair.of(x, y)) + shift, 1e-15);
    }
    assertEquals(shifted.getName(), newName);
  }

}
