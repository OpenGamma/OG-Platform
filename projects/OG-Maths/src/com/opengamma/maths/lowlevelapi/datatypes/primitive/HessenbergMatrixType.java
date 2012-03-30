/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * Essentially just a wrapper for the two Hessenberg Matrix types
 */
public abstract class HessenbergMatrixType extends PackedMatrix {

  /**
   * @param aMatrix a double matrix array of arrays representation of a triangular matrix
   */
  public HessenbergMatrixType(double[][] aMatrix) {
    super(aMatrix);
  }

  /**
   * @param aMatrix a DoubleMatrix2D representation of a triangular matrix
   */
  public HessenbergMatrixType(DoubleMatrix2D aMatrix) {
    super(aMatrix);
  }

}
