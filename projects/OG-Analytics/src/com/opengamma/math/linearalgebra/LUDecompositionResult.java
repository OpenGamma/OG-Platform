/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * interface for holders of LU Decomposition Results
 */
public interface LUDecompositionResult extends DecompositionResult {
  /**
   * Returns the matrix L of the decomposition.
   * <p>L is an lower-triangular matrix</p>
   * @return the L matrix (or null if decomposed matrix is singular)
   */
  DoubleMatrix2D getL();

  /**
   * Returns the matrix U of the decomposition.
   * <p>U is an upper-triangular matrix</p>
   * @return the U matrix (or null if decomposed matrix is singular)
   */
  DoubleMatrix2D getU();

  /**
   * Returns the P rows permutation matrix.
   * <p>P is a sparse matrix with exactly one element set to 1.0 in
   * each row and each column, all other elements being set to 0.0.</p>
   * <p>The positions of the 1 elements are given by the {@link #getPivot()
   * pivot permutation vector}.</p>
   * @return the P rows permutation matrix (or null if decomposed matrix is singular)
   * @see #getPivot()
   */
  DoubleMatrix2D getP();

  /**
   * Returns the pivot permutation vector.
   * @return the pivot permutation vector
   * @see #getP()
   */
  int[] getPivot();

  /**
   * Return the determinant of the matrix
   * @return determinant of the matrix
   */
  double getDeterminant();

}
