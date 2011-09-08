/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * Contains the results of Cholesky matrix decomposition.
 */
public interface CholeskyDecompositionResult extends DecompositionResult {

  /**
   * Returns the {@latex.inline $\\mathbf{L}$} matrix of the decomposition.
   * <p>
   * {@latex.inline $\\mathbf{L}$} is a lower-triangular matrix.
   * @return the {@latex.inline $\\mathbf{L}$} matrix
   */
  DoubleMatrix2D getL();

  /**
   * Returns the transpose of the matrix {@latex.inline $\\mathbf{L}$} of the decomposition.
   * <p>
   * {@latex.inline $\\mathbf{L}^T$} is a upper-triangular matrix.
   * @return the {@latex.inline $\\mathbf{L}^T$} matrix
   */
  DoubleMatrix2D getLT();

  /**
   * Return the determinant of the matrix.
   * @return determinant of the matrix
   */
  double getDeterminant();

}
