/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class InterpolatedCurveShiftFunctionTest {
  private static final int N = 10;
  private static final double[] X = new double[N];
  private static final double[] Y = new double[N];
  private static final InterpolatedDoublesCurve CURVE;
  private static final InterpolatedCurveShiftFunction F = new InterpolatedCurveShiftFunction();
  private static final LinearInterpolator1D LINEAR = new LinearInterpolator1D();
  private static final double SHIFT = 0.12;
  private static final double EPS = 1e-15;

  static {
    for (int i = 0; i < N; i++) {
      X[i] = i;
      Y[i] = 2 * i + 1;
    }
    CURVE = InterpolatedDoublesCurve.fromSorted(X, Y, LINEAR, "A");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurve1() {
    F.evaluate(null, 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurve2() {
    F.evaluate(null, 1, "B");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurve3() {
    F.evaluate(null, 1, 5);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurve4() {
    F.evaluate(null, 1, 5, "B");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurve5() {
    F.evaluate(null, new double[] {1}, new double[] {1});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurve6() {
    F.evaluate(null, new double[] {1}, new double[] {1}, "B");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUnequalArrayLength1() {
    F.evaluate(CURVE, new double[] {1}, new double[] {3, 4});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUnequalArrayLength2() {
    F.evaluate(CURVE, new double[] {1}, new double[] {3, 4}, "S");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullXShifts1() {
    F.evaluate(CURVE, null, new double[] {1, 2, 3});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullXShifts2() {
    F.evaluate(CURVE, null, new double[] {1, 2, 3}, "A");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullYShifts1() {
    F.evaluate(CURVE, new double[] {1, 2, 3}, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullYShifts2() {
    F.evaluate(CURVE, new double[] {1, 2, 3}, null, "A");
  }

  @Test
  public void testParallel() {
    InterpolatedDoublesCurve shifted = F.evaluate(CURVE, SHIFT);
    double[] x = shifted.getXDataAsPrimitive();
    double[] y = shifted.getYDataAsPrimitive();
    for (int i = 0; i < N; i++) {
      assertEquals(x[i], X[i], EPS);
      assertEquals(y[i], Y[i] + SHIFT, EPS);
    }
    assertEquals(shifted.getName(), "PARALLEL_SHIFT_A");
    final String newName = "B";
    shifted = F.evaluate(CURVE, SHIFT, newName);
    x = shifted.getXDataAsPrimitive();
    y = shifted.getYDataAsPrimitive();
    for (int i = 0; i < N; i++) {
      assertEquals(x[i], X[i], EPS);
      assertEquals(y[i], Y[i] + SHIFT, EPS);
    }
    assertEquals(shifted.getName(), newName);
  }

  @Test
  public void testSingleShift() {
    double shiftX = 3;
    InterpolatedDoublesCurve shifted = F.evaluate(CURVE, shiftX, SHIFT);
    double[] x = shifted.getXDataAsPrimitive();
    double[] y = shifted.getYDataAsPrimitive();
    for (int i = 0; i < N; i++) {
      assertEquals(x[i], X[i], EPS);
      if (i == 3) {
        assertEquals(y[i], Y[i] + SHIFT, EPS);
      } else {
        assertEquals(y[i], Y[i], EPS);
      }
    }
    assertEquals(shifted.getName(), "SINGLE_SHIFT_A");
    final String newName = "B";
    shifted = F.evaluate(CURVE, shiftX, SHIFT, newName);
    x = shifted.getXDataAsPrimitive();
    y = shifted.getYDataAsPrimitive();
    for (int i = 0; i < N; i++) {
      assertEquals(x[i], X[i], EPS);
      if (i == 3) {
        assertEquals(y[i], Y[i] + SHIFT, EPS);
      } else {
        assertEquals(y[i], Y[i], EPS);
      }
    }
    assertEquals(shifted.getName(), newName);
    shiftX = 3.1;
    shifted = F.evaluate(CURVE, shiftX, SHIFT);
    x = shifted.getXDataAsPrimitive();
    y = shifted.getYDataAsPrimitive();
    final double[] resultX = new double[] {0, 1, 2, 3, 3.1, 4, 5, 6, 7, 8, 9};
    final double[] resultY = new double[N + 1];
    for (int i = 0; i < N + 1; i++) {
      resultY[i] = 2 * resultX[i] + 1;
    }
    resultY[4] += SHIFT;
    assertArrayEquals(resultX, x, EPS);
    assertArrayEquals(resultY, y, EPS);
  }

  @Test
  public void testMultipleShift() {
    double[] shiftX = new double[] {1, 2, 3, 4};
    double[] shiftY = new double[] {0.1, 0.2, -0.1, -0.2};
    InterpolatedDoublesCurve shifted = F.evaluate(CURVE, shiftX, shiftY);
    double[] x = shifted.getXDataAsPrimitive();
    double[] y = shifted.getYDataAsPrimitive();
    for (int i = 0; i < N; i++) {
      assertEquals(x[i], X[i], EPS);
      if (i >= 1 && i <= 4) {
        assertEquals(y[i], Y[i] + shiftY[i - 1], EPS);
      } else {
        assertEquals(y[i], Y[i], EPS);
      }
    }
    assertEquals(shifted.getName(), "MULTIPLE_POINT_SHIFT_A");
    final String newName = "B";
    shifted = F.evaluate(CURVE, shiftX, shiftY, newName);
    x = shifted.getXDataAsPrimitive();
    y = shifted.getYDataAsPrimitive();
    for (int i = 0; i < N; i++) {
      assertEquals(x[i], X[i], EPS);
      if (i >= 1 && i <= 4) {
        assertEquals(y[i], Y[i] + shiftY[i - 1], EPS);
      } else {
        assertEquals(y[i], Y[i], EPS);
      }
    }
    assertEquals(shifted.getName(), newName);
    shiftX = new double[] {1.1, 2.1, 3.1, 4.1};
    shiftY = new double[] {0.1, 0.2, -0.1, -0.2};
    shifted = F.evaluate(CURVE, shiftX, shiftY);
    x = shifted.getXDataAsPrimitive();
    y = shifted.getYDataAsPrimitive();
    final double[] resultX = new double[] {0, 1, 1.1, 2, 2.1, 3, 3.1, 4, 4.1, 5, 6, 7, 8, 9};
    final double[] resultY = new double[resultX.length];
    for (int i = 0; i < resultX.length; i++) {
      resultY[i] = 2 * resultX[i] + 1;
    }
    resultY[2] += shiftY[0];
    resultY[4] += shiftY[1];
    resultY[6] += shiftY[2];
    resultY[8] += shiftY[3];
    assertArrayEquals(x, resultX, EPS);
    assertArrayEquals(y, resultY, EPS);
    assertEquals(shifted.getName(), "MULTIPLE_POINT_SHIFT_A");
    shiftX = new double[0];
    shiftY = new double[0];
    shifted = F.evaluate(CURVE, shiftX, shiftY, "A");
    assertFalse(shifted == CURVE);
    assertEquals(shifted, CURVE);
  }

}
