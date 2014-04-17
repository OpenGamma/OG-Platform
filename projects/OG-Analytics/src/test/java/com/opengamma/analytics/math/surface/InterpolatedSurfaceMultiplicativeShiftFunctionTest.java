/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.surface;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class InterpolatedSurfaceMultiplicativeShiftFunctionTest {
  private static final double[] X = new double[] {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4 };
  private static final double[] Y = new double[] {0, 1, 2, 3, 4, 0, 1, 2, 3, 4, 0, 1, 2, 3, 4, 0, 1, 2, 3, 4, 0, 1, 2, 3, 4 };
  private static final double[] Z = new double[] {5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5 };
  private static final GridInterpolator2D INTERPOLATOR = new GridInterpolator2D(new LinearInterpolator1D(), new LinearInterpolator1D()) {

        @Override
        public Double interpolate(Map<Double, Interpolator1DDataBundle> dataBundle, DoublesPair value) {
          return value.getFirst() + value.getSecond();
        }
      };
  private static final String NAME = "K";
  private static final InterpolatedDoublesSurface SURFACE = InterpolatedDoublesSurface.from(X, Y, Z, INTERPOLATOR, NAME);
  private static final InterpolatedSurfaceMultiplicativeShiftFunction F = new InterpolatedSurfaceMultiplicativeShiftFunction();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull1() {
    F.evaluate(null, 3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull2() {
    F.evaluate(null, 3, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull3() {
    F.evaluate(null, 3, 4, 5);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull4() {
    F.evaluate(null, 3, 4, 5, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull5() {
    F.evaluate(null, new double[] {3 }, new double[] {4 }, new double[] {5 });
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNull6() {
    F.evaluate(null, new double[] {3 }, new double[] {4 }, new double[] {5 }, NAME);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthY1() {
    F.evaluate(SURFACE, new double[] {1 }, new double[] {2, 3 }, new double[] {4 });
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthY2() {
    F.evaluate(SURFACE, new double[] {1 }, new double[] {2, 3 }, new double[] {4 }, "M");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthZ1() {
    F.evaluate(SURFACE, new double[] {1 }, new double[] {2 }, new double[] {3, 4 });
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthZ2() {
    F.evaluate(SURFACE, new double[] {1 }, new double[] {2 }, new double[] {3, 4 }, "L");
  }

  @Test
  public void testParallel() {
    final double shift = 0.12;
    InterpolatedDoublesSurface surface = F.evaluate(SURFACE, shift);
    assertArrayEquals(surface.getXDataAsPrimitive(), X, 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), Y, 0);
    for (int i = 0; i < Z.length; i++) {
      assertEquals(Z[i] * (1 + shift), surface.getZDataAsPrimitive()[i], 0);
    }
    assertEquals(surface.getName(), "CONSTANT_MULTIPLIER_" + NAME);
    final String newName = "E";
    surface = F.evaluate(SURFACE, shift, newName);
    assertArrayEquals(surface.getXDataAsPrimitive(), X, 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), Y, 0);
    for (int i = 0; i < Z.length; i++) {
      assertEquals(Z[i] * (1 + shift), surface.getZDataAsPrimitive()[i], 0);
    }
    assertEquals(surface.getName(), newName);
  }

  @Test
  public void testSingle() {
    double x = 2;
    double y = 4;
    final double shift = 0.34;
    InterpolatedDoublesSurface surface = F.evaluate(SURFACE, x, y, shift);
    assertArrayEquals(surface.getXDataAsPrimitive(), X, 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), Y, 0);
    double[] z = surface.getZDataAsPrimitive();
    for (int i = 0; i < z.length; i++) {
      if (i == 14) {
        assertEquals(Z[i] * (1 + shift), z[i], 0);
      } else {
        assertEquals(Z[i], z[i], 0);
      }
    }
    assertEquals(surface.getName(), "SINGLE_MULTIPLIER_" + NAME);
    final String newName = "R";
    surface = F.evaluate(SURFACE, x, y, shift, newName);
    assertArrayEquals(surface.getXDataAsPrimitive(), X, 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), Y, 0);
    z = surface.getZDataAsPrimitive();
    for (int i = 0; i < z.length; i++) {
      if (i == 14) {
        assertEquals(Z[i] * (1 + shift), z[i], 0);
      } else {
        assertEquals(Z[i], z[i], 0);
      }
    }
    assertEquals(surface.getName(), newName);
    x = 1.5;
    y = 3.;
    final int n = X.length + 1;
    surface = F.evaluate(SURFACE, x, y, shift);
    double[] newX = surface.getXDataAsPrimitive();
    double[] newY = surface.getYDataAsPrimitive();
    z = surface.getZDataAsPrimitive();
    assertEquals(newX.length, n);
    assertEquals(newY.length, n);
    assertEquals(z.length, n);
    for (int i = 0; i < n - 1; i++) {
      assertEquals(X[i], newX[i], 0);
      assertEquals(Y[i], newY[i], 0);
      assertEquals(Z[i], z[i], 0);
    }
    assertEquals(newX[n - 1], x, 0);
    assertEquals(newY[n - 1], y, 0);
    assertEquals(z[n - 1], x + y + shift, 0);
    assertEquals(surface.getName(), "SINGLE_MULTIPLIER_" + NAME);
    surface = F.evaluate(SURFACE, x, y, shift, newName);
    newX = surface.getXDataAsPrimitive();
    newY = surface.getYDataAsPrimitive();
    z = surface.getZDataAsPrimitive();
    assertEquals(newX.length, n);
    assertEquals(newY.length, n);
    assertEquals(z.length, n);
    for (int i = 0; i < n - 1; i++) {
      assertEquals(X[i], newX[i], 0);
      assertEquals(Y[i], newY[i], 0);
      assertEquals(Z[i], z[i], 0);
    }
    assertEquals(newX[n - 1], x, 0);
    assertEquals(newY[n - 1], y, 0);
    assertEquals(z[n - 1], x + y + shift, 0);
    assertEquals(surface.getName(), newName);
  }

  @Test
  public void testMultipleNoData() {
    final double[] xShift = new double[0];
    final double[] yShift = new double[0];
    final double[] shift = new double[0];
    InterpolatedDoublesSurface surface = F.evaluate(SURFACE, xShift, yShift, shift);
    assertArrayEquals(surface.getXDataAsPrimitive(), X, 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), Y, 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), Z, 0);
    assertEquals(surface.getName(), "MULTIPLE_MULTIPLIER_" + NAME);
    surface = F.evaluate(SURFACE, xShift, yShift, shift, "A");
    assertArrayEquals(surface.getXDataAsPrimitive(), X, 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), Y, 0);
    assertArrayEquals(surface.getZDataAsPrimitive(), Z, 0);
    assertEquals(surface.getName(), "A");
  }

  @Test
  public void testMultipleOnPoints() {
    final double[] x = new double[] {1, 2 };
    final double[] y = new double[] {0, 3 };
    final double[] shift = new double[] {0.34, 0.56 };
    InterpolatedDoublesSurface surface = F.evaluate(SURFACE, x, y, shift);
    assertArrayEquals(surface.getXDataAsPrimitive(), X, 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), Y, 0);
    double[] z = surface.getZDataAsPrimitive();
    for (int i = 0; i < z.length; i++) {
      if (i == 5) {
        assertEquals(Z[i] * (1 + shift[0]), z[i], 0);
      } else if (i == 13) {
        assertEquals(Z[i] * (1 + shift[1]), z[i], 0);
      } else {
        assertEquals(Z[i], z[i], 0);
      }
    }
    assertEquals(surface.getName(), "MULTIPLE_MULTIPLIER_" + NAME);
    final String newName = "R";
    surface = F.evaluate(SURFACE, x, y, shift, newName);
    assertArrayEquals(surface.getXDataAsPrimitive(), X, 0);
    assertArrayEquals(surface.getYDataAsPrimitive(), Y, 0);
    z = surface.getZDataAsPrimitive();
    for (int i = 0; i < z.length; i++) {
      if (i == 5) {
        assertEquals(Z[i] * (1 + shift[0]), z[i], 0);
      } else if (i == 13) {
        assertEquals(Z[i] * (1 + shift[1]), z[i], 0);
      } else {
        assertEquals(Z[i], z[i], 0);
      }
    }
    assertEquals(surface.getName(), newName);
  }

  @Test
  public void testMultipleOneOnPoint() {
    final double[] x = new double[] {1, 2.3 };
    final double[] y = new double[] {0, 3.9 };
    final double[] shift = new double[] {0.34, 0.56 };
    InterpolatedDoublesSurface surface = F.evaluate(SURFACE, x, y, shift);
    final int n = X.length + 1;
    double[] newX = surface.getXDataAsPrimitive();
    double[] newY = surface.getYDataAsPrimitive();
    double[] newZ = surface.getZDataAsPrimitive();
    assertEquals(surface.size(), newX.length);
    assertEquals(surface.size(), newY.length);
    assertEquals(surface.size(), newZ.length);
    for (int i = 0; i < n - 1; i++) {
      if (i == 5) {
        assertEquals(Z[i] * (1 + shift[0]), newZ[i], 0);
      } else {
        assertEquals(Z[i], newZ[i], 0);
      }
    }
    assertEquals(newX[n - 1], x[1], 0);
    assertEquals(newY[n - 1], y[1], 0);
    assertEquals(newZ[n - 1], (x[1] + y[1]) * (1 + shift[1]), 0);
    assertEquals(surface.getName(), "MULTIPLE_MULTIPLIER_" + NAME);
    final String newName = "R";
    surface = F.evaluate(SURFACE, x, y, shift, newName);
    newX = surface.getXDataAsPrimitive();
    newY = surface.getYDataAsPrimitive();
    newZ = surface.getZDataAsPrimitive();
    assertEquals(surface.size(), newX.length);
    assertEquals(surface.size(), newY.length);
    assertEquals(surface.size(), newZ.length);
    for (int i = 0; i < n - 1; i++) {
      if (i == 5) {
        assertEquals(Z[i] * (1 + shift[0]), newZ[i], 0);
      } else {
        assertEquals(Z[i], newZ[i], 0);
      }
    }
    assertEquals(newX[n - 1], x[1], 0);
    assertEquals(newY[n - 1], y[1], 0);
    assertEquals(newZ[n - 1], (x[1] + y[1]) * (1 + shift[1]), 0);
    assertEquals(surface.getName(), newName);
  }

  @Test
  public void testMultiple() {
    final double[] x = new double[] {1.67, 2.3 };
    final double[] y = new double[] {0.15, 3.9 };
    final double[] shift = new double[] {0.34, 0.56 };
    InterpolatedDoublesSurface surface = F.evaluate(SURFACE, x, y, shift);
    final int n = X.length + 2;
    double[] newX = surface.getXDataAsPrimitive();
    double[] newY = surface.getYDataAsPrimitive();
    double[] newZ = surface.getZDataAsPrimitive();
    assertEquals(surface.size(), newX.length);
    assertEquals(surface.size(), newY.length);
    assertEquals(surface.size(), newZ.length);
    for (int i = 0; i < n - 2; i++) {
      assertEquals(Z[i], newZ[i], 0);
    }
    assertEquals(newX[n - 2], x[0], 0);
    assertEquals(newY[n - 2], y[0], 0);
    assertEquals(newZ[n - 2], (x[0] + y[0]) * (1 + shift[0]), 0);
    assertEquals(newX[n - 1], x[1], 0);
    assertEquals(newY[n - 1], y[1], 0);
    assertEquals(newZ[n - 1], (x[1] + y[1]) * (1 + shift[1]), 0);
    assertEquals(surface.getName(), "MULTIPLE_MULTIPLIER_" + NAME);
    final String newName = "R";
    surface = F.evaluate(SURFACE, x, y, shift, newName);
    newX = surface.getXDataAsPrimitive();
    newY = surface.getYDataAsPrimitive();
    newZ = surface.getZDataAsPrimitive();
    assertEquals(surface.size(), newX.length);
    assertEquals(surface.size(), newY.length);
    assertEquals(surface.size(), newZ.length);
    for (int i = 0; i < n - 2; i++) {
      assertEquals(Z[i], newZ[i], 0);
    }
    assertEquals(newX[n - 2], x[0], 0);
    assertEquals(newY[n - 2], y[0], 0);
    assertEquals(newZ[n - 2], (x[0] + y[0]) * (1 + shift[0]), 0);
    assertEquals(newX[n - 1], x[1], 0);
    assertEquals(newY[n - 1], y[1], 0);
    assertEquals(newZ[n - 1], (x[1] + y[1]) * (1 + shift[1]), 0);
    assertEquals(surface.getName(), newName);
  }
}
