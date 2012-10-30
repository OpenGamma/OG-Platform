/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;


/**
 * The Upper Hessenberg matrix.
 * It holds data and properties needed to define an Upper Hessenberg matrix
 * and implements methods to access particular parts of the matrix useful to
 * more general mathematics.
 *
 * An Upper Hessenberg matrix is a square matrix with zero entries an the lower triangle
 * below the first sub diagonal. The matrix generally looks like:
 *
 *
 *  |* * * *     * * *|
 *  |* * * *     * * *|
 *  |o * * * ... * * *|
 *  |o o * *     * * *|
 *  |o o o *     * * *|
 *  |o o o o.    * * *|
 *  |   .      . * * *|
 *  |   .  o ... o * *|
 *
 * where '*' denotes entry and 'o' denotes zero.
 *
 */

public class UpperHessenbergMatrix extends HessenbergMatrixType {

  public UpperHessenbergMatrix(double[][] aMatrix) {
    super(MatrixPrimitiveUtils.checkIsUpperHessenberg(aMatrix));
  }

  public UpperHessenbergMatrix(DoubleMatrix2D aMatrix) {
    this(aMatrix.toArray());
  }


}
