/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * Contains the results of LU matrix decomposition.
 */
public interface LUDecompositionResult extends DecompositionResult {

  /**
   * Returns the {@latex.inline $\\mathbf{L}$} matrix of the decomposition.
   * <p>
   * {@latex.inline $\\mathbf{L}$} is a lower-triangular matrix.
   * @return the {@latex.inline $\\mathbf{L}$} matrix
   */
  DoubleMatrix2D getL();

  /**
   * Returns the {@latex.inline $\\mathbf{U}$} matrix of the decomposition.
   * <p>
   * {@latex.inline $\\mathbf{U}$} is an upper-triangular matrix.
   * @return the U matrix
   */
  DoubleMatrix2D getU();

  /**
   * Returns the rows permutation matrix, {@latex.inline $\\mathbf{P}$}.
   * <p>
   * P is a sparse matrix with exactly one element set to 1.0 in
   * each row and each column, all other elements being set to 0.0.
   * <p>
   * The positions of the 1 elements are given by the {@link #getPivot()
   * pivot permutation vector}.
   * @return the {@latex.inline $\\mathbf{P}$} rows permutation matrix
   * @see #getPivot()
   */
  DoubleMatrix2D getP();

  /**
   * Returns the pivot permutation vector.
   * @return the pivot permutation vector
   */
  int[] getPivot();

  /**
   * Return the determinant of the matrix.
   * @return determinant of the matrix
   */
  double getDeterminant();

}
