/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import org.testng.annotations.Test;

/**
 * Tests the Upper Triangular matrix type
 */
public class LowerTriangularMatrixTest {
    private double[][] dataSquare = {{1,0,0,0},{2,3,0,0},{4,5,6,0},{7,8,9,10}};
    private double[][] dataBad = {{1,1,0,0},{2,3,0,0},{4,5,6,0},{7,8,9,10}};

  /**
   * Test constructors
   */
  @Test
  public void testConstructionFromArrayOfArrays() {
    new LowerTriangularMatrix(dataSquare);
  }

  @Test
  public void testConstructionFromDoubleMatrix2D() {
    new LowerTriangularMatrix(new DoubleMatrix2D(dataSquare));
  }

  @Test(expectedExceptions =  IllegalArgumentException.class)
  public void testConstructionFromBadData() {
    new LowerTriangularMatrix(dataBad);
  }

}
