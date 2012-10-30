/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.highlevelapi.datatypes.primitive;

import java.util.Arrays;

import com.opengamma.maths.lowlevelapi.datatypes.primitive.DenseMatrix;

/**
 * High level API.
 * Dense matrix backed array magic.
 */
public class OGArrayType extends DenseMatrix {

  /**
   * Build from double[][]
   * @param aMatrix a double[][] representation of the matrix data.
   */
  public OGArrayType(double[][] aMatrix) {
    super(aMatrix);
  }

  @Override
  public double[] getData() {
    return Arrays.copyOf(super.getData(), super.getData().length);
  }

  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer();
    final int rows = super.getNumberOfRows();
    final int cols = super.getNumberOfColumns();
    final double[] data = super.getData();
    sb.append("\n{\n");
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        sb.append(String.format("%12.8f ", data[i * cols + j]));
      }
      sb.append("\n");
    }
    sb.append("}");
    return sb.toString();
  }
}
