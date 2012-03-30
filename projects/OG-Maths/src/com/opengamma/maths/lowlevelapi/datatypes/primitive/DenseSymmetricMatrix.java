/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * SymmetricMatrix class provides a storage method for symmetric matrices in the PackedMatrix {@link PackedMatrix} form.
 * A matrix is symmetric if all elements a_ij = a_ji.
 * Example:
 * |1 2 3 4|
 * |2 1 5 6|
 * |3 5 1 7|
 * |4 6 7 1|
 *
 * If it is known that a matrix is symmetric then the matrix should be stored as such as a number of specific optimisations exist.
 *
 */
public class DenseSymmetricMatrix extends PackedMatrix {

  /**
   * Constructs a symmetric matrix from a symmetric array of arrays.
   * @param aMatrix the symmetric matrix
   */
  public DenseSymmetricMatrix(double[][] aMatrix) {
    super(MatrixPrimitiveUtils.removeLowerTriangle(aMatrix), PackedMatrix.allowZerosOn.rightSide);
  }

  /**
   * Constructs a symmetric matrix from a symmetric DoubleMatrix2D type.
   * @param aMatrix the symmetric matrix
   */
  public DenseSymmetricMatrix(DoubleMatrix2D aMatrix) {
    super(MatrixPrimitiveUtils.removeLowerTriangle(aMatrix.toArray()), PackedMatrix.allowZerosOn.rightSide);
  }

}
