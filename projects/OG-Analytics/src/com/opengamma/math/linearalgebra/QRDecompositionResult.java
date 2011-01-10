/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * interface for holders of QR Decomposition Results
 */
public interface QRDecompositionResult extends DecompositionResult {

  /**
   * Returns the matrix R of the decomposition.
   * <p>R is an upper-triangular matrix</p>
   * @return the R matrix
   */
  DoubleMatrix2D getR();

  /**
   * Returns the matrix Q of the decomposition.
   * <p>Q is an orthogonal matrix</p>
   * @return the Q matrix
   */
  DoubleMatrix2D getQ();

  /**
   * Returns the transpose of the matrix Q of the decomposition.
   * <p>Q is an orthogonal matrix</p>
   * @return the Q matrix
   */
  DoubleMatrix2D getQT();

  /**
   * Returns the Householder reflector vectors.
   * <p>H is a lower trapezoidal matrix whose columns represent
   * each successive Householder reflector vector. This matrix is used
   * to compute Q.</p>
   * @return a matrix containing the Householder reflector vectors
   */
  DoubleMatrix2D getH();

}
