/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * Contains the results of SV matrix decomposition.
 */
public interface SVDecompositionResult extends DecompositionResult {

  /**
   * Returns the matrix {@latex.inline $\\mathbf{U}$} of the decomposition.
   * <p>
   * {@latex.inline $\\mathbf{U}$} is an orthogonal matrix, i.e. its transpose is also its inverse.
   * @return the {@latex.inline $\\mathbf{U}$} matrix
   */
  DoubleMatrix2D getU();

  /**
   * Returns the transpose of the matrix {@latex.inline $\\mathbf{U}$} of the decomposition.
   * <p>
   * {@latex.inline $\\mathbf{U}$} is an orthogonal matrix, i.e. its transpose is also its inverse.
   * @return the U matrix (or null if decomposed matrix is singular)
   */
  DoubleMatrix2D getUT();

  /**
   * Returns the diagonal matrix {@latex.inline $\\mathbf{\\Sigma}$} of the decomposition.
   * <p>
   * {@latex.inline $\\mathbf{\\Sigma}$} is a diagonal matrix. The singular values are provided in
   * non-increasing order.
   * @return the {@latex.inline $\\mathbf{\\Sigma}$} matrix
   */
  DoubleMatrix2D getS();

  /**
   * Returns the diagonal elements of the matrix {@latex.inline $\\mathbf{\\Sigma}$} of the decomposition.
   * <p>
   * The singular values are provided in non-increasing order.
   * @return the diagonal elements of the {@latex.inline $\\mathbf{\\Sigma}$} matrix
   */
  double[] getSingularValues();

  /**
   * Returns the matrix {@latex.inline $\\mathbf{V}$} of the decomposition.
   * <p>
   * {@latex.inline $\\mathbf{V}$} is an orthogonal matrix, i.e. its transpose is also its inverse.
   * @return the {@latex.inline $\\mathbf{V}$} matrix
   */
  DoubleMatrix2D getV();

  /**
   * Returns the transpose of the matrix {@latex.inline $\\mathbf{V}$} of the decomposition.
   * <p>
   * {@latex.inline $\\mathbf{V}$} is an orthogonal matrix, i.e. its transpose is also its inverse.
   * @return the {@latex.inline $\\mathbf{V}$} matrix
   */
  DoubleMatrix2D getVT();

  /**
   * Returns the {@latex.inline $L_2$} norm of the matrix.
   * <p>
   * The {@latex.inline $L_2$} norm is {@latex.inline $\\max\\left(\\frac{|\\mathbf{A} \\times U|_2}{|U|_2}\\right)$}, where {@latex.inline $|.|_2$} denotes the vectorial 2-norm
   * (i.e. the traditional Euclidian norm).
   * @return norm
   */
  double getNorm();

  /**
   * Returns the condition number of the matrix.
   * @return condition number of the matrix
   */
  double getConditionNumber();

  /**
   * Returns the effective numerical matrix rank.
   * <p>The effective numerical rank is the number of non-negligible
   * singular values. The threshold used to identify non-negligible
   * terms is {@latex.inline $\\max(m, n) \\times \\mathrm{ulp}(S_1)$}, where {@latex.inline $\\mathrm{ulp}(S_1)$}  
   * is the least significant bit of the largest singular value.
   * @return effective numerical matrix rank
   */
  int getRank();

}
