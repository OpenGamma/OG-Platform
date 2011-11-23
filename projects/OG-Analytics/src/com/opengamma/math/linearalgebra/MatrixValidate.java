/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import com.opengamma.math.MathException;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public abstract class MatrixValidate {

  public static void noNaN(final DoubleMatrix2D x) {
    final int rows = x.getNumberOfRows();
    final int cols = x.getNumberOfColumns();
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        if (Double.isNaN(x.getEntry(i, j))) {
          throw new MathException("Matrix contains a NaN");
        }
      }
    }
  }

}
