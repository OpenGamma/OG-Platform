/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math;

import static org.testng.AssertJUnit.assertArrayEquals;
import org.testng.annotations.Test;
import java.util.Arrays;

/**
 * 
 */
public class ParallelArrayBinarySortTest {
  private static final double[] X1 = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
  private static final double[] Y1 = new double[] {2, 4, 6, 8, 10, 12, 14, 16, 18, 20};
  private static final double[] X2 = new double[] {1, 7, 3, 4, 9, 10, 8, 2, 5, 6};
  private static final double[] Y2 = new double[] {2, 14, 6, 8, 18, 20, 16, 4, 10, 12};
  private static final Float[] F1 = new Float[] {1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f};
  private static final Double[] Y3 = new Double[] {2.1, 4.1, 6.1, 8.1, 10.1, 12.1, 14.1, 16.1, 18.1, 20.1};
  private static final Float[] F2 = new Float[] {1f, 7f, 3f, 4f, 9f, 10f, 8f, 2f, 5f, 6f};
  private static final Double[] Y4 = new Double[] {2.1, 14.1, 6.1, 8.1, 18.1, 20.1, 16.1, 4.1, 10.1, 12.1};

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDoubleNullKeys() {
    ParallelArrayBinarySort.parallelBinarySort(null, Y1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDoubleNullValues() {
    ParallelArrayBinarySort.parallelBinarySort(X1, (double[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDoubleDifferentLengths() {
    ParallelArrayBinarySort.parallelBinarySort(X1, new double[] {2, 4, 6});
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDoubleObjectNullKeys() {
    ParallelArrayBinarySort.parallelBinarySort((double[]) null, Y1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDoubleObjectNullValues() {
    ParallelArrayBinarySort.parallelBinarySort(X1, (Object[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDoubleObjectDifferentLengths() {
    ParallelArrayBinarySort.parallelBinarySort(X1, new Object[] {2, 4, 6});
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
    ParallelArrayBinarySort.parallelBinarySort(F1, new Double[] {2., 4., 6.});
  }

  @Test
  public void testDoubles() {
    final int n = X2.length;
    final double[] x2 = Arrays.copyOf(X2, n);
    final double[] y2 = Arrays.copyOf(Y2, n);
    ParallelArrayBinarySort.parallelBinarySort(x2, y2);
    assertArrayEquals(X1, x2, 0);
    assertArrayEquals(Y1, y2, 0);
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
    final int n = X2.length;
    final double[] x2 = Arrays.copyOf(X2, n);
    final Double[] y4 = Arrays.copyOf(Y4, n);
    ParallelArrayBinarySort.parallelBinarySort(x2, y4);
    assertArrayEquals(X1, x2, 0);
    assertArrayEquals(Y3, y4);
  }
}
