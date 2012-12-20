/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * The Lower Hessenberg matrix.
 * It holds data and properties needed to define a Lower Hessenberg matrix
 * and implements methods to access particular parts of the matrix useful to
 * more general mathematics.
 *
 * A Lower Hessenberg matrix is a square matrix with zero entries an the upper triangle
 * above the first super diagonal. The matrix generally looks like:
 *
 *  |* * o o     o o o|
 *  |* * * o     o o o|
 *  |* * * * ... o o o|
 *  |* * * *     o o o|
 *  |* * * *     * o o|
 *  |* * * *.    * * o|
 *  |   .      . * * *|
 *  |   .  * ... * * *|
 *
 * where '*' denotes entry and 'o' denotes zero.
 *
 */

public class LowerHessenbergMatrix extends HessenbergMatrixType {

  public LowerHessenbergMatrix(double[][] aMatrix) {
    super(MatrixPrimitiveUtils.checkIsLowerHessenberg(aMatrix));
  }

  public LowerHessenbergMatrix(DoubleMatrix2D aMatrix) {
    this(aMatrix.toArray());
  }


}
