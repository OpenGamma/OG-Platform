/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * A Toeplitz (or diagonal constant matrix) is a matrix which has the form:
 *
 * |a b c d e|
 * |f a b c d|
 * |g f a b c|
 * |h g f a b|
 * |i h g f a|
 *
 * such that the diagonals of the matrix have constant values.
 *
 * If it is know a priori that a Full Matrix will have entries of the Toeplitz matrix form and the Toeplitz form
 * is immutable throughout the computation, then the matrix should be expressed using the VandermondeMatrix class. This definition
 * allows a number of special algorithms to be used for linear algebra operations that have considerably lower Landau order
 * than their Full Matrix counterparts.
 *
 */
public class ToeplitzMatrix extends DenseMatrix {
  public ToeplitzMatrix(double[][] aMatrix) {
    super(aMatrix);
  }

  public ToeplitzMatrix(DoubleMatrix2D aMatrix) {
    super(aMatrix);
  }

  public ToeplitzMatrix(DenseMatrix aMatrix) {
    super(aMatrix.toArray());
  }
}

