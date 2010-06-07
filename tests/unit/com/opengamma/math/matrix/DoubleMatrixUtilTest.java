/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 */
public class DoubleMatrixUtilTest {

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeDimension() {
    DoubleMatrixUtil.getIdentityMatrix2D(-3);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullVector() {
    DoubleMatrixUtil.getTwoDimensionalDiagonalMatrix((DoubleMatrix1D) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullArray() {
    DoubleMatrixUtil.getTwoDimensionalDiagonalMatrix((double[]) null);
  }

  @Test
  public void testIdentity() {
    assertEquals(DoubleMatrixUtil.getIdentityMatrix2D(0), DoubleMatrix2D.EMPTY_MATRIX);
    assertEquals(DoubleMatrixUtil.getIdentityMatrix2D(1), new DoubleMatrix2D(new double[][] {new double[] {1}}));
    assertEquals(DoubleMatrixUtil.getIdentityMatrix2D(4), new DoubleMatrix2D(
        new double[][] {new double[] {1, 0, 0, 0}, new double[] {0, 1, 0, 0}, new double[] {0, 0, 1, 0}, new double[] {0, 0, 0, 1}}));
  }

  @Test
  public void testDiagonalMatrix() {
    assertEquals(DoubleMatrixUtil.getTwoDimensionalDiagonalMatrix(DoubleMatrix1D.EMPTY_MATRIX), DoubleMatrix2D.EMPTY_MATRIX);
    assertEquals(DoubleMatrixUtil.getTwoDimensionalDiagonalMatrix(new DoubleMatrix1D(new double[] {1, 1, 1, 1})), DoubleMatrixUtil.getIdentityMatrix2D(4));
    assertEquals(DoubleMatrixUtil.getTwoDimensionalDiagonalMatrix(new double[0]), DoubleMatrix2D.EMPTY_MATRIX);
    assertEquals(DoubleMatrixUtil.getTwoDimensionalDiagonalMatrix(new double[] {1, 1, 1, 1}), DoubleMatrixUtil.getIdentityMatrix2D(4));
  }
}
