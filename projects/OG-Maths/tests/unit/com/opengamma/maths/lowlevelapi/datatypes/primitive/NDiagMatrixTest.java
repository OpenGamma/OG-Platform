/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.NDiagMatrix;

/**
 * Tests the Ndiag matrix class
 * Sanity validation is done by the helper method checkIsNDiag in {@link MatrixPrimitiveUtils}.
 */
public class NDiagMatrixTest {

   double[][] badtrid1 = {{1,2,0,1,0,0,0},{3,4,5,0,0,0,0},{0,6,7,8,0,0,0},{0,0,9,10,11,0,0},{0,0,0,12,13,14,0},{0,0,0,0,15,16,17},{0,0,0,0,0,18,19}};
   double[][] badtrid2 = {{1,2,0,0,0,0,0},{3,4,5,0,1,0,0},{0,6,7,8,0,0,0},{0,0,9,10,11,0,0},{0,0,0,12,13,14,0},{0,0,0,0,15,16,17},{0,0,0,0,0,18,19}};
   double[][] badtrid3 = {{1,2,0,0,0,0,0},{3,4,5,0,0,0,0},{0,6,7,8,0,0,0},{0,0,9,10,11,0,0},{0,0,0,12,13,14,0},{0,0,0,0,15,16,17},{0,0,1,0,0,18,19}};
   double[][] diag =  {{1,0,0,0},{0,2,0,0},{0,0,3,0},{0,0,0,4}};
   double[][] ndiag = {{1,1,0,0},{0,2,0,0},{0,0,3,0},{0,0,0,4}};
   double[][] trid = {{1,2,0,0,0,0,0},{3,4,5,0,0,0,0},{0,6,7,8,0,0,0},{0,0,9,10,11,0,0},{0,0,0,12,13,14,0},{0,0,0,0,15,16,17},{0,0,0,0,0,18,19}};
   double[][] ntrid = {{1,2,0,0,0,0,0},{3,4,5,1,0,0,0},{0,6,7,8,0,0,0},{0,0,9,10,11,0,0},{0,0,0,12,13,14,0},{0,0,0,0,15,16,17},{0,0,0,0,0,18,19}};
   double[][] pentd =  {{1,2,3,0,0,0,0,0,0},{4,5,6,7,0,0,0,0,0},{8,9,10,11,12,0,0,0,0},{0,13,14,15,16,17,0,0,0},{0,0,18,19,20,21,22,0,0},
       {0,0,0,23,24,25,26,27,0},{0,0,0,0,28,29,30,31,32},{0,0,0,0,0,33,34,35,36},{0,0,0,0,0,0,37,38,39}};
   double[][] npentd = {{1,2,3,0,0,0,0,0,0},{4,5,6,7,0,0,0,0,0},{8,9,10,11,12,0,0,0,0},{0,13,14,15,16,17,0,0,0},{0,0,18,19,20,21,22,0,0},
       {0,0,0,23,24,25,26,27,0},{0,0,0,0,28,29,30,31,32},{0,0,0,0,0,33,34,35,36},{0,0,0,0,0,1,37,38,39}};

  @Test
  public void testConstructFromArrayOfArrays() {
    new NDiagMatrix(trid,3);
  }

  @Test
  public void testConstructFromDoubleMatrix2D() {
    new NDiagMatrix(new DoubleMatrix2D(trid),3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testArgExceptionFromBadData1() {
    new NDiagMatrix(badtrid1,3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testArgExceptionFromBadData2() {
    new NDiagMatrix(badtrid2,3);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testArgExceptionFromBadData3() {
    new NDiagMatrix(badtrid3,3);
  }

  /**
   * Test a few different sizes
   */
  @Test
  public void testConstructFrom1Band() {
    new NDiagMatrix(diag,1);
  }

  @Test
  public void testConstructFrom3Band() {
    new NDiagMatrix(trid,3);
  }

  @Test
  public void testConstructFrom5Band() {
    new NDiagMatrix(pentd,5);
  }

} // end of class

