/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;


/**
  * SparseSymmetricMatrix class provides a storage method for sparse symmetric matrices in sparse form.
  * The default constructors attempt to guess the best method format for storage, the more advance constructors
  * allow users to select the storage pattern.
 */
public class SparseSymmetricMatrix extends SparseMatrix {

 /**
 * Construct from array of arrays, let SparseMatrix choose format.
 * @param aMatrix an array of arrays representation of the matrix
 */
  public SparseSymmetricMatrix(double[][] aMatrix) {
    super(MatrixPrimitiveUtils.removeLowerTriangle(aMatrix));
  }

  /**
   * Construct from DoubleMatrix2D, let SparseMatrix choose format.
   * @param aMatrix a DoubleMatrix2D representation of the matrix
   */
  public SparseSymmetricMatrix(DoubleMatrix2D aMatrix) {
    super(MatrixPrimitiveUtils.removeLowerTriangle(aMatrix.toArray()));
  }

}
