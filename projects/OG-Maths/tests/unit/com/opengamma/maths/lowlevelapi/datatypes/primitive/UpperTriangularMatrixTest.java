/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.UpperTriangularMatrix;

/**
 * Tests the Upper Triangular matrix type
 */
public class UpperTriangularMatrixTest {
  private double[][] dataSquare = {{1,2,3,4},{0,5,6,7},{0,0,8,9},{0,0,0,10}};
  private double[][] dataBad = {{1,2,3,4},{0,5,6,7},{0,0,8,9},{0,0,1,10}};

/**
 * Test constructors
 */
@Test
public void testConstructionFromArrayOfArrays() {
  new UpperTriangularMatrix(dataSquare);
}

@Test
public void testConstructionFromDoubleMatrix2D() {
  new UpperTriangularMatrix(new DoubleMatrix2D(dataSquare));
}

@Test(expectedExceptions =  IllegalArgumentException.class)
public void testConstructionFromBadData() {
  new UpperTriangularMatrix(dataBad);
}

}
