/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.TriDiagMatrix;

/**
 * Tests the tridiag matrix class
 */
public class TriDiagMatrixTest {
   double[][] _trid = {{1,2,0,0,0,0,0},{3,4,5,0,0,0,0},{0,6,7,8,0,0,0},{0,0,9,10,11,0,0},{0,0,0,12,13,14,0},{0,0,0,0,15,16,17},{0,0,0,0,0,18,19}};
   double[][] _badtrid1 = {{1,2,0,1,0,0,0},{3,4,5,0,0,0,0},{0,6,7,8,0,0,0},{0,0,9,10,11,0,0},{0,0,0,12,13,14,0},{0,0,0,0,15,16,17},{0,0,0,0,0,18,19}};
   double[][] _badtrid2 = {{1,2,0,0,0,0,0},{3,4,5,0,1,0,0},{0,6,7,8,0,0,0},{0,0,9,10,11,0,0},{0,0,0,12,13,14,0},{0,0,0,0,15,16,17},{0,0,0,0,0,18,19}};
   double[][] _badtrid3 = {{1,2,0,0,0,0,0},{3,4,5,0,0,0,0},{0,6,7,8,0,0,0},{0,0,9,10,11,0,0},{0,0,0,12,13,14,0},{0,0,0,0,15,16,17},{0,0,1,0,0,18,19}};

  @Test
  public void testConstructFromArrayOfArrays() {
    new TriDiagMatrix(_trid);
  }

  @Test
  public void testConstructFromDoubleMatrix2D() {
    new TriDiagMatrix(new DoubleMatrix2D(_trid));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testArgExceptionFromBadData() {
    new TriDiagMatrix(_badtrid1);
    new TriDiagMatrix(_badtrid2);
    new TriDiagMatrix(_badtrid3);
  }

} // end of class

