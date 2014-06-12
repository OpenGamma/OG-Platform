/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.surface;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NodalSurfaceAdditiveShiftFunctionTest {
  private static final double[] X = new double[] {0, 0, 0, 1, 1, 1, 2, 2, 2};
  private static final double[] Y = new double[] {0, 1, 2, 0, 1, 2, 0, 1, 2};
  private static final double[] Z = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9};
  private static final NodalDoublesSurface SURFACE = NodalDoublesSurface.from(X, Y, Z, "A");
  private static final NodalSurfaceAdditiveShiftFunction F = new NodalSurfaceAdditiveShiftFunction();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull1() {
    F.evaluate(null, 3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull2() {
    F.evaluate(null, 3, "A");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull3() {
    F.evaluate(null, 3, 4, 5);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull4() {
    F.evaluate(null, 3, 4, 5, "A");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull5() {
    F.evaluate(null, new double[] {3}, new double[] {4}, new double[] {5});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull6() {
    F.evaluate(null, new double[] {3}, new double[] {4}, new double[] {5}, "A");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthY1() {
    F.evaluate(SURFACE, new double[] {1}, new double[] {2, 3}, new double[] {4});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthY2() {
    F.evaluate(SURFACE, new double[] {1}, new double[] {2, 3}, new double[] {4}, "M");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthZ1() {
    F.evaluate(SURFACE, new double[] {1}, new double[] {2}, new double[] {3, 4});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthZ2() {
    F.evaluate(SURFACE, new double[] {1}, new double[] {2}, new double[] {3, 4}, "L");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSingleShiftNoNodes() {
    F.evaluate(SURFACE, 1.5, 2.4, 0.5);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMultipleShiftNoX() {
    F.evaluate(SURFACE, new double[] {1, 1.5}, new double[] {1, 1}, new double[] {0.25, 0.5});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testMultipleShiftNoY() {
    F.evaluate(SURFACE, new double[] {0, 0}, new double[] {0, 1.2}, new double[] {0.1, 0.1});
  }

  @Test
  public void testParallelShift() {
    final double shift = 0.8;
    NodalDoublesSurface shifted = F.evaluate(SURFACE, shift);
    assertArrayEquals(shifted.getXDataAsPrimitive(), X, 0);
    assertArrayEquals(shifted.getYDataAsPrimitive(), Y, 0);
    final int n = Z.length;
    for (int i = 0; i < n; i++) {
      assertEquals(shifted.getZDataAsPrimitive()[i], Z[i] + shift, 0);
    }
    assertEquals(shifted.getName(), "PARALLEL_SHIFT_A");
    shifted = F.evaluate(SURFACE, shift, "B");
    assertArrayEquals(shifted.getXDataAsPrimitive(), X, 0);
    assertArrayEquals(shifted.getYDataAsPrimitive(), Y, 0);
    for (int i = 0; i < n; i++) {
      assertEquals(shifted.getZDataAsPrimitive()[i], Z[i] + shift, 0);
    }
    assertEquals(shifted.getName(), "B");
  }

  @Test
  public void testSingleShift() {
    final double x = 2;
    final double y = 1;
    final double shift = 0.23;
    NodalDoublesSurface shifted = F.evaluate(SURFACE, x, y, shift);
    assertArrayEquals(shifted.getXDataAsPrimitive(), X, 0);
    assertArrayEquals(shifted.getYDataAsPrimitive(), Y, 0);
    final int n = Z.length;
    for (int i = 0; i < n; i++) {
      if (i == 7) {
        assertEquals(shifted.getZDataAsPrimitive()[i], Z[i] + shift, 0);
      } else {
        assertEquals(shifted.getZDataAsPrimitive()[i], Z[i], 0);
      }
    }
    assertEquals(shifted.getName(), "SINGLE_SHIFT_A");
    shifted = F.evaluate(SURFACE, x, y, shift, "B");
    assertArrayEquals(shifted.getXDataAsPrimitive(), X, 0);
    assertArrayEquals(shifted.getYDataAsPrimitive(), Y, 0);
    for (int i = 0; i < n; i++) {
      if (i == 7) {
        assertEquals(shifted.getZDataAsPrimitive()[i], Z[i] + shift, 0);
      } else {
        assertEquals(shifted.getZDataAsPrimitive()[i], Z[i], 0);
      }
    }
    assertEquals(shifted.getName(), "B");
  }

  @Test
  public void testMultipleShiftNoShifts() {
    final double[] xShift = new double[0];
    final double[] yShift = new double[0];
    final double[] shift = new double[0];
    NodalDoublesSurface surface = F.evaluate(SURFACE, xShift, yShift, shift);
    assertArrayEquals(surface.getXDataAsPrimitive(), X, 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), Y, 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), Z, 0);
    assertEquals(surface.getName(), "MULTIPLE_SHIFT_" + "A");
    surface = F.evaluate(SURFACE, xShift, yShift, shift, "A");
    assertArrayEquals(surface.getXDataAsPrimitive(), X, 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), Y, 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), Z, 0);
    assertEquals(surface.getName(), "A");
  }

  @Test
  public void testMultipleShifts() {
    final double[] x = new double[] {0, 2};
    final double[] y = new double[] {0, 1};
    final double shift1 = 0.23;
    final double shift2 = 0.67;
    NodalDoublesSurface shifted = F.evaluate(SURFACE, x, y, new double[] {shift1, shift2});
    assertArrayEquals(shifted.getXDataAsPrimitive(), X, 0);
    assertArrayEquals(shifted.getYDataAsPrimitive(), Y, 0);
    final int n = Z.length;
    for (int i = 0; i < n; i++) {
      if (i == 0) {
        assertEquals(shifted.getZDataAsPrimitive()[i], Z[i] + shift1, 0);
      } else if (i == 7) {
        assertEquals(shifted.getZDataAsPrimitive()[i], Z[i] + shift2, 0);
      } else {
        assertEquals(shifted.getZDataAsPrimitive()[i], Z[i], 0);
      }
    }
    assertEquals(shifted.getName(), "MULTIPLE_SHIFT_A");
    shifted = F.evaluate(SURFACE, x, y, new double[] {shift1, shift2}, "B");
    assertArrayEquals(shifted.getXDataAsPrimitive(), X, 0);
    assertArrayEquals(shifted.getYDataAsPrimitive(), Y, 0);
    for (int i = 0; i < n; i++) {
      if (i == 0) {
        assertEquals(shifted.getZDataAsPrimitive()[i], Z[i] + shift1, 0);
      } else if (i == 7) {
        assertEquals(shifted.getZDataAsPrimitive()[i], Z[i] + shift2, 0);
      } else {
        assertEquals(shifted.getZDataAsPrimitive()[i], Z[i], 0);
      }
    }
    assertEquals(shifted.getName(), "B");
  }
}
