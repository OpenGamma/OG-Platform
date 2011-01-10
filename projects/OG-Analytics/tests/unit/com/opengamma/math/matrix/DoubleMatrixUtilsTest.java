/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 */
public class DoubleMatrixUtilsTest {

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeDimension() {
    DoubleMatrixUtils.getIdentityMatrix2D(-3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullVector() {
    DoubleMatrixUtils.getTwoDimensionalDiagonalMatrix((DoubleMatrix1D) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullArray() {
    DoubleMatrixUtils.getTwoDimensionalDiagonalMatrix((double[]) null);
  }

  @Test
  public void testIdentity() {
    assertEquals(DoubleMatrixUtils.getIdentityMatrix2D(0), DoubleMatrix2D.EMPTY_MATRIX);
    assertEquals(DoubleMatrixUtils.getIdentityMatrix2D(1), new DoubleMatrix2D(new double[][] {new double[] {1}}));
    assertEquals(DoubleMatrixUtils.getIdentityMatrix2D(4), new DoubleMatrix2D(new double[][] {new double[] {1, 0, 0, 0}, new double[] {0, 1, 0, 0}, new double[] {0, 0, 1, 0},
        new double[] {0, 0, 0, 1}}));
  }

  @Test
  public void testDiagonalMatrix() {
    assertEquals(DoubleMatrixUtils.getTwoDimensionalDiagonalMatrix(DoubleMatrix1D.EMPTY_MATRIX), DoubleMatrix2D.EMPTY_MATRIX);
    assertEquals(DoubleMatrixUtils.getTwoDimensionalDiagonalMatrix(new DoubleMatrix1D(new double[] {1, 1, 1, 1})), DoubleMatrixUtils.getIdentityMatrix2D(4));
    assertEquals(DoubleMatrixUtils.getTwoDimensionalDiagonalMatrix(new double[0]), DoubleMatrix2D.EMPTY_MATRIX);
    assertEquals(DoubleMatrixUtils.getTwoDimensionalDiagonalMatrix(new double[] {1, 1, 1, 1}), DoubleMatrixUtils.getIdentityMatrix2D(4));
  }

  @Test
  public void testTransposeMatrix() {
    final DoubleMatrix2D m = new DoubleMatrix2D(new double[][] {new double[] {1, 2, 3}, new double[] {4, 5, 6}, new double[] {7, 8, 9}});
    assertEquals(DoubleMatrixUtils.getTranspose(m), new DoubleMatrix2D(new double[][] {new double[] {1, 4, 7}, new double[] {2, 5, 8}, new double[] {3, 6, 9}}));
  }
}
