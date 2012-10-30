/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.linearalgebra;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * Contains the results of QR matrix decomposition.
 */
public interface QRDecompositionResult extends DecompositionResult {

  /**
   * Returns the matrix $\mathbf{R}$ of the decomposition.
   * <p>
   * $\mathbf{R}$ is an upper-triangular matrix.
   * @return the $\mathbf{R}$ matrix
   */
  DoubleMatrix2D getR();

  /**
   * Returns the matrix $\mathbf{Q}$ of the decomposition.
   * <p>
   * $\mathbf{Q}$ is an orthogonal matrix.
   * @return the $\mathbf{Q}$ matrix
   */
  DoubleMatrix2D getQ();

  /**
   * Returns the transpose of the matrix $\mathbf{Q}$ of the decomposition.
   * <p>
   * $\mathbf{Q}$ is an orthogonal matrix.
   * @return the $\mathbf{Q}$ matrix
   */
  DoubleMatrix2D getQT();

  /**
   * Returns the Householder reflector vectors.
   * <p>
   * $\mathbf{H}$ is a lower trapezoidal matrix whose columns represent
   * each successive Householder reflector vector. This matrix is used
   * to compute $\mathbf{Q}$.
   * @return a matrix containing the Householder reflector vectors
   */
  DoubleMatrix2D getH();

}
