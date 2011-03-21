/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * Contains the results of QR matrix decomposition.
 */
public interface QRDecompositionResult extends DecompositionResult {

  /**
   * Returns the matrix {@latex.inline $\\mathbf{R}$} of the decomposition.
   * <p>
   * {@latex.inline $\\mathbf{R}$} is an upper-triangular matrix.
   * @return the {@latex.inline $\\mathbf{R}$} matrix
   */
  DoubleMatrix2D getR();

  /**
   * Returns the matrix {@latex.inline $\\mathbf{Q}$} of the decomposition.
   * <p>
   * {@latex.inline $\\mathbf{Q}$} is an orthogonal matrix.
   * @return the {@latex.inline $\\mathbf{Q}$} matrix
   */
  DoubleMatrix2D getQ();

  /**
   * Returns the transpose of the matrix {@latex.inline $\\mathbf{Q}$} of the decomposition.
   * <p>
   * {@latex.inline $\\mathbf{Q}$} is an orthogonal matrix.
   * @return the {@latex.inline $\\mathbf{Q}$} matrix
   */
  DoubleMatrix2D getQT();

  /**
   * Returns the Householder reflector vectors.
   * <p>
   * {@latex.inline $\\mathbf{H}$} is a lower trapezoidal matrix whose columns represent
   * each successive Householder reflector vector. This matrix is used
   * to compute {@latex.inline $\\mathbf{Q}$}.
   * @return a matrix containing the Householder reflector vectors
   */
  DoubleMatrix2D getH();

}
