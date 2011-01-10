/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class TridiagonalMatrixTest {
  private static final double[] A = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
  private static final double[] B = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9};
  private static final double[] C = new double[] {2, 3, 4, 5, 6, 7, 8, 9, 10};
  private static final TridiagonalMatrix M = new TridiagonalMatrix(A, B, C);

  @Test(expected = IllegalArgumentException.class)
  public void testNullA() {
    new TridiagonalMatrix(null, B, C);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullB() {
    new TridiagonalMatrix(A, null, C);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullC() {
    new TridiagonalMatrix(A, B, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongB() {
    new TridiagonalMatrix(A, new double[] {1, 2, 3, 4, 5, 6, 7, 8}, C);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongC() {
    new TridiagonalMatrix(A, B, new double[] {1, 2, 3, 4, 5, 6, 7});
  }

  @Test
  public void testGetters() {
    assertTrue(Arrays.equals(A, M.getDiagonal()));
    assertTrue(Arrays.equals(B, M.getUpperSubDiagonal()));
    assertTrue(Arrays.equals(C, M.getLowerSubDiagonal()));
    final int n = A.length;
    final DoubleMatrix2D matrix = M.toDoubleMatrix2D();
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        if (i == j) {
          assertEquals(matrix.getEntry(i, j), A[i], 0);
        } else if (j == i + 1) {
          assertEquals(matrix.getEntry(i, j), B[j - 1], 0);
        } else if (j == i - 1) {
          assertEquals(matrix.getEntry(i, j), C[j], 0);
        } else {
          assertEquals(matrix.getEntry(i, j), 0, 0);
        }
      }
    }
  }

  @Test
  public void testHashCodeAndEquals() {
    final double[] a = Arrays.copyOf(A, A.length);
    final double[] b = Arrays.copyOf(B, B.length);
    final double[] c = Arrays.copyOf(C, C.length);
    TridiagonalMatrix other = new TridiagonalMatrix(a, b, c);
    assertEquals(other, M);
    assertEquals(other.hashCode(), M.hashCode());
    a[1] = 1000;
    other = new TridiagonalMatrix(a, B, C);
    assertFalse(other.equals(M));
    b[1] = 1000;
    other = new TridiagonalMatrix(A, b, C);
    assertFalse(other.equals(M));
    c[1] = 1000;
    other = new TridiagonalMatrix(A, B, c);
    assertFalse(other.equals(M));
  }
}
