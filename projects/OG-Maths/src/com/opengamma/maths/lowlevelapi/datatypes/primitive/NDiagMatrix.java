/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * The N-Diag matrix class provides support for N-diagonal matrix storage.
 *
 * A N-diagonal matrix generally looks like:
 *
 * |* . . * o o o o|
 * |. * . . * o o o|
 * |. . * . . * o o|
 * |* . . * . . * o|
 * |o * . . * . . *|
 * |o o * . . * . .|
 * |o o o * . . * .|
 * |o o o o * . . *|
 *
 * where '*' denotes entry and 'o' denotes zero and the '.' denotes pattern continuation.
 *
 * N-diagonal matrices arise from methods such as simple finite differencing schemes and can be a good form
 * to which more complicated forms are be reduced.
 *
 */
public class NDiagMatrix extends PackedMatrix {

/**
 *
 * @param aMatrix an array of arrays representation of a banded matrix
 * @param n the width of the band
 */
  public NDiagMatrix(double[][] aMatrix, int n) {
    super(MatrixPrimitiveUtils.checkIsNDiag(aMatrix, n));
  }

  /**
  *
  * @param aMatrix a DoubleMatrix2D representation of a banded matrix
  * @param n the width of the band
  */
  public NDiagMatrix(DoubleMatrix2D aMatrix, int n) {
    super(MatrixPrimitiveUtils.checkIsNDiag(aMatrix.toArray(), n));
  }
}
