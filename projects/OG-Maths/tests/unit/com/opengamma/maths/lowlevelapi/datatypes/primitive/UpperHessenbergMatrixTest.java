/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.UpperHessenbergMatrix;

/**
 * tests Upper Hessenberg matrix type
 */
public class UpperHessenbergMatrixTest {
  private static final double[][] dataSquare = new double[][] {{1,2,3,4,5},{6,7,8,9,10},{0,11,12,13,14},{0,0,15,16,17},{0,0,0,18,19}};
  private static final double[][] dataBad = new double[][] {{1,2,3,4,5},{6,7,8,9,10},{1,11,12,13,14},{0,0,15,16,17},{0,0,0,18,19}};

  // array of arrays constructor
  @Test
  public void testSquareArraytoUpperHessenbergConstructor() {
    new UpperHessenbergMatrix(dataSquare);
  }

  // array of arrays constructor
  @Test
  public void testDoubleMatrix2DtoUpperHessenbergConstructor() {
    new UpperHessenbergMatrix(new DoubleMatrix2D(dataSquare));
  }


  // bad data
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadArraytoUpperHessenbergConstructor() {
    new UpperHessenbergMatrix(dataBad);
  }
}
