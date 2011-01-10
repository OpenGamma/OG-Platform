/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * interface for holders of SVD Results
 */
public interface SVDecompositionResult extends DecompositionResult {

  /**
   * Returns the matrix U of the decomposition.
   * <p>U is an orthogonal matrix, i.e. its transpose is also its inverse.</p>
   * @return the U matrix
   * @see #getUT()
   */
  DoubleMatrix2D getU();

  /**
   * Returns the transpose of the matrix U of the decomposition.
   * <p>U is an orthogonal matrix, i.e. its transpose is also its inverse.</p>
   * @return the U matrix (or null if decomposed matrix is singular)
   * @see #getU()
   */
  DoubleMatrix2D getUT();

  /**
   * Returns the diagonal matrix &Sigma; of the decomposition.
   * <p>&Sigma; is a diagonal matrix. The singular values are provided in
   * non-increasing order.</p>
   * @return the &Sigma; matrix
   */
  DoubleMatrix2D getS();

  /**
   * Returns the diagonal elements of the matrix &Sigma; of the decomposition.
   * <p>The singular values are provided in non-increasing order.</p>
   * @return the diagonal elements of the &Sigma; matrix
   */
  double[] getSingularValues();

  /**
   * Returns the matrix V of the decomposition.
   * <p>V is an orthogonal matrix, i.e. its transpose is also its inverse.</p>
   * @return the V matrix (or null if decomposed matrix is singular)
   * @see #getVT()
   */
  DoubleMatrix2D getV();

  /**
   * Returns the transpose of the matrix V of the decomposition.
   * <p>V is an orthogonal matrix, i.e. its transpose is also its inverse.</p>
   * @return the V matrix (or null if decomposed matrix is singular)
   * @see #getV()
   */
  DoubleMatrix2D getVT();

  /**
   * Returns the L<sub>2</sub> norm of the matrix.
   * <p>The L<sub>2</sub> norm is max(|A &times; u|<sub>2</sub> /
   * |u|<sub>2</sub>), where |.|<sub>2</sub> denotes the vectorial 2-norm
   * (i.e. the traditional euclidian norm).</p>
   * @return norm
   */
  double getNorm();

  /**
   * Return the condition number of the matrix.
   * @return condition number of the matrix
   */
  double getConditionNumber();

  /**
   * Return the effective numerical matrix rank.
   * <p>The effective numerical rank is the number of non-negligible
   * singular values. The threshold used to identify non-negligible
   * terms is max(m,n) &times; ulp(s<sub>1</sub>) where ulp(s<sub>1</sub>)
   * is the least significant bit of the largest singular value.</p>
   * @return effective numerical matrix rank
   */
  int getRank();

}
