/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * The UpperTriangular class is a very basic matrix storage class that implements methods to convert and store data in the Upper Triangle of a matrix.
 * The zero entries are not stored to save space.
 */
public class UpperTriangularMatrix extends TriangularMatrixType {

  /**
   * @param aMatrix an array of arrays representation of an upper triangular matrix
   */
  public UpperTriangularMatrix(double[][] aMatrix) {
    super(MatrixPrimitiveUtils.checkIsUpperTriangular(aMatrix));
  }

  /**
   * @param aMatrix a DoubleMatrix2D representation of an upper triangular matrix
   */
  public UpperTriangularMatrix(DoubleMatrix2D aMatrix) {
    this(aMatrix.toArray());
  }

}

