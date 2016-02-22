/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class ParallelArrayBinarySortTest {
  private static final Float[] F1 = new Float[] {1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f };
  private static final Double[] Y3 = new Double[] {2.1, 4.1, 6.1, 8.1, 10.1, 12.1, 14.1, 16.1, 18.1, 20.1 };
  private static final Float[] F2 = new Float[] {1f, 7f, 3f, 4f, 9f, 10f, 8f, 2f, 5f, 6f };
  private static final Double[] Y4 = new Double[] {2.1, 14.1, 6.1, 8.1, 18.1, 20.1, 16.1, 4.1, 10.1, 12.1 };

  private static final double[] X1D = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
  private static final double[] Y1D = new double[] {2, 4, 6, 8, 10, 12, 14, 16, 18, 20 };
  private static final double[] Z1D = new double[] {5, 10, 15, 20, 25, 30, 35, 40, 45, 50 };
  private static final double[] X2D = new double[] {1, 7, 3, 4, 9, 10, 8, 2, 5, 6 };
  private static final double[] Y2D = new double[] {2, 14, 6, 8, 18, 20, 16, 4, 10, 12 };
  private static final double[] Z2D = new double[] {5, 35, 15, 20, 45, 50, 40, 10, 25, 30 };
  private static final double[] X3D = new double[] {1, 3, 4, 7, 9, 10, 8, 2, 5, 6 };
  private static final double[] Y3D = new double[] {2, 6, 8, 14, 18, 20, 16, 4, 10, 12 };

  private static final int[] X1I = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
  private static final int[] Y1I = new int[] {2, 4, 6, 8, 10, 12, 14, 16, 18, 20 };
  private static final int[] X2I = new int[] {1, 7, 3, 4, 9, 10, 8, 2, 5, 6 };
  private static final int[] Y2I = new int[] {2, 14, 6, 8, 18, 20, 16, 4, 10, 12 };

  private static final long[] X1L = new long[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
  private static final long[] X2L = new long[] {1, 7, 3, 4, 9, 10, 8, 2, 5, 6 };

  private static final float[] X1F = new float[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
  private static final float[] X2F = new float[] {1, 7, 3, 4, 9, 10, 8, 2, 5, 6 };


  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDoubleNullKeys() {
    double[] t = null;
    ParallelArrayBinarySort.parallelBinarySort(t, Y1D);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDoubleNullValues() {
    ParallelArrayBinarySort.parallelBinarySort(X1D, (double[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDoubleDifferentLengths() {
    ParallelArrayBinarySort.parallelBinarySort(X1D, new double[] {2, 4, 6 });
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDoubleObjectNullKeys() {
    ParallelArrayBinarySort.parallelBinarySort((double[]) null, Y1D);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDoubleObjectNullValues() {
    ParallelArrayBinarySort.parallelBinarySort(X1D, (Object[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDoubleObjectDifferentLengths() {
    ParallelArrayBinarySort.parallelBinarySort(X1D, new Object[] {2, 4, 6 });
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testObjectNullKeys() {
    ParallelArrayBinarySort.parallelBinarySort((String[]) null, Y3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testObjectNullValues() {
    ParallelArrayBinarySort.parallelBinarySort(F1, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testObjectDifferentLengths() {
    ParallelArrayBinarySort.parallelBinarySort(F1, new Double[] {2., 4., 6. });
  }

  @Test
  public void testIntsSortByDouble() {
    final int n = X1I.length;
    final int[] x2 = Arrays.copyOf(X2I, n);
    final double[] y2 = Arrays.copyOf(Y2D, n);
    ParallelArrayBinarySort.parallelBinarySort(x2, y2);
    assertTrue(Arrays.equals(X1I, x2));
    assertTrue(Arrays.equals(Y1D, y2));
  }

  @Test
  public void testLongsSortByDouble() {
    final int n = X1L.length;
    final long[] x2 = Arrays.copyOf(X2L, n);
    final double[] y2 = Arrays.copyOf(Y2D, n);
    ParallelArrayBinarySort.parallelBinarySort(x2, y2);
    assertTrue(Arrays.equals(X1L, x2));
    assertTrue(Arrays.equals(Y1D, y2));
  }

  @Test
  public void testFloatsSortByDouble() {
    final int n = X1F.length;
    final float[] x2 = Arrays.copyOf(X2F, n);
    final double[] y2 = Arrays.copyOf(Y2D, n);
    ParallelArrayBinarySort.parallelBinarySort(x2, y2);
    assertTrue(Arrays.equals(X1F, x2));
    assertTrue(Arrays.equals(Y1D, y2));
  }

  @Test
  public void testDoublesSortByDouble() {
    final int n = X1D.length;
    final double[] x2 = Arrays.copyOf(X2D, n);
    final double[] y2 = Arrays.copyOf(Y2D, n);
    ParallelArrayBinarySort.parallelBinarySort(x2, y2);
    assertTrue(Arrays.equals(X1D, x2));
    assertTrue(Arrays.equals(Y1D, y2));
  }

  @Test
  public void testDoublesSortByDoubleSubset() {
    final int n = X1D.length;
    final double[] x2 = Arrays.copyOf(X2D, n);
    final double[] y2 = Arrays.copyOf(Y2D, n);
    ParallelArrayBinarySort.parallelBinarySort(x2, y2, 0, 4);
    assertTrue(Arrays.equals(X3D, x2));
    assertTrue(Arrays.equals(Y3D, y2));
  }

  @Test
  public void testTripleDoublesSortByDouble() {
    final int n = X1D.length;
    final double[] x2 = Arrays.copyOf(X2D, n);
    final double[] y2 = Arrays.copyOf(Y2D, n);
    final double[] z2 = Arrays.copyOf(Z2D, n);
    ParallelArrayBinarySort.parallelBinarySort(x2, y2, z2);
    assertTrue(Arrays.equals(X1D, x2));
    assertTrue(Arrays.equals(Y1D, y2));
    assertTrue(Arrays.equals(Z1D, z2));
  }


  @Test
  public void testIntsSortByInts() {
    final int n = X1I.length;
    final int[] x2 = Arrays.copyOf(X2I, n);
    final int[] y2 = Arrays.copyOf(Y2I, n);
    ParallelArrayBinarySort.parallelBinarySort(x2, y2);
    assertTrue(Arrays.equals(X1I, x2));
    assertTrue(Arrays.equals(Y1I, y2));
  }

  @Test
  public void testLongsSortByInts() {
    final int n = X1L.length;
    final long[] x2 = Arrays.copyOf(X2L, n);
    final int[] y2 = Arrays.copyOf(Y2I, n);
    ParallelArrayBinarySort.parallelBinarySort(x2, y2);
    assertTrue(Arrays.equals(X1L, x2));
    assertTrue(Arrays.equals(Y1I, y2));
  }

  @Test
  public void testFloatsSortByInts() {
    final int n = X1F.length;
    final float[] x2 = Arrays.copyOf(X2F, n);
    final int[] y2 = Arrays.copyOf(Y2I, n);
    ParallelArrayBinarySort.parallelBinarySort(x2, y2);
    assertTrue(Arrays.equals(X1F, x2));
    assertTrue(Arrays.equals(Y1I, y2));
  }

  @Test
  public void testDoublesSortByInts() {
    final int n = X1D.length;
    final double[] x2 = Arrays.copyOf(X2D, n);
    final int[] y2 = Arrays.copyOf(Y2I, n);
    ParallelArrayBinarySort.parallelBinarySort(x2, y2);
    assertTrue(Arrays.equals(X1D, x2));
    assertTrue(Arrays.equals(Y1I, y2));
  }

  @Test
  public void testObjects() {
    final int n = F2.length;
    final Float[] f2 = Arrays.copyOf(F2, n);
    final Double[] y4 = Arrays.copyOf(Y4, n);
    ParallelArrayBinarySort.parallelBinarySort(f2, y4);
    assertArrayEquals(F1, f2);
    assertArrayEquals(Y3, y4);
  }

  @Test
  public void testDoubleObject() {
    final int n = X2D.length;
    final double[] x2 = Arrays.copyOf(X2D, n);
    final Double[] y4 = Arrays.copyOf(Y4, n);
    ParallelArrayBinarySort.parallelBinarySort(x2, y4);
    assertArrayEquals(X1D, x2, 0);
    assertArrayEquals(Y3, y4);
  }
}
