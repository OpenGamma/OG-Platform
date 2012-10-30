/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * The LowerTriangular class is a very basic matrix storage class that implements methods to convert and store data in the Lower Triangle of a matrix.
 * The zero entries are not stored to save space.
 */
public class LowerTriangularMatrix extends TriangularMatrixType {

  /**
   * @param aMatrix an array of arrays representation of a lower triangular matrix
   */
  public LowerTriangularMatrix(double[][] aMatrix) {
    super(MatrixPrimitiveUtils.checkIsLowerTriangular(aMatrix));
  }

  /**
   * @param aMatrix a DoubleMatrix2D representation of a lower triangular matrix
   */
  public LowerTriangularMatrix(DoubleMatrix2D aMatrix) {
    this(aMatrix.toArray());
  }


}
