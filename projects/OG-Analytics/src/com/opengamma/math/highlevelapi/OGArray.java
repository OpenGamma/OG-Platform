/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.highlevelapi;

import java.util.Arrays;

import com.opengamma.math.matrix.DenseMatrix;

/**
 * High level API.
 * Dense matrix backed array magic.
 */
public class OGArray extends DenseMatrix {

  /**
   * Build from double[][]
   * @param aMatrix a double[][] representation of the matrix data.
   */
  public OGArray(double[][] aMatrix) {
    super(aMatrix);
  }

  @Override
  public double[] getData() {
    return Arrays.copyOf(super.getData() , super.getData().length);
  }

}
