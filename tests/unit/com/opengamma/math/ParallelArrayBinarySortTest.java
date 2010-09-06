/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

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

  @Test(expected = IllegalArgumentException.class)
  public void testDoubleNullKeys() {
    ParallelArrayBinarySort.parallelBinarySort(null, Y1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDoubleNullValues() {
    ParallelArrayBinarySort.parallelBinarySort(X1, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDoubleDifferentLengths() {
    ParallelArrayBinarySort.parallelBinarySort(X1, new double[] {2, 4, 6});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testObjectNullKeys() {
    ParallelArrayBinarySort.parallelBinarySort((String[]) null, Y3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testObjectNullValues() {
    ParallelArrayBinarySort.parallelBinarySort(F1, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testObjectDifferentLengths() {
    ParallelArrayBinarySort.parallelBinarySort(F1, new Double[] {2., 4., 6.});
  }

  @Test
  public void testDoubles() {
    ParallelArrayBinarySort.parallelBinarySort(X2, Y2);
    assertArrayEquals(X1, X2, 0);
    assertArrayEquals(Y1, Y2, 0);
  }

  @Test
  public void testObjects() {
    ParallelArrayBinarySort.parallelBinarySort(F2, Y4);
    assertArrayEquals(F1, F2);
    assertArrayEquals(Y3, Y4);
  }
}
