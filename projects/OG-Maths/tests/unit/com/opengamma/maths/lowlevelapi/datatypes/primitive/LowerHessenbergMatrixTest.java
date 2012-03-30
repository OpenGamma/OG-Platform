/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.LowerHessenbergMatrix;

/**
 * tests Lower Hessenberg matrix type
 */
public class LowerHessenbergMatrixTest {
  private static final double[][] dataSquare = new double[][] {{1,2,0,0,0},{3,4,5,0,0},{6,7,8,9,0},{10,11,12,13,14},{15,16,17,18,19}};
  private static final double[][] dataBad = new double[][] {{1,2,0,0,1},{3,4,5,0,0},{6,7,8,9,0},{10,11,12,13,14},{15,16,17,18,19}};

  // array of arrays constructor
  @Test
  public void testSquareArraytoLowerHessenbergConstructor() {
    new LowerHessenbergMatrix(dataSquare);
  }

  // array of arrays constructor
  @Test
  public void testDoubleMatrix2DtoLowerHessenbergConstructor() {
    new LowerHessenbergMatrix(new DoubleMatrix2D(dataSquare));
  }


  // bad data
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadArraytoLowerHessenbergConstructor() {
    new LowerHessenbergMatrix(dataBad);
  }
}
