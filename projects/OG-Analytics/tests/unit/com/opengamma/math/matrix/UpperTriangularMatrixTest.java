/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import org.testng.annotations.Test;

/**
 *
 */
public class UpperTriangularMatrixTest {
private double[][] dataSquare = {{1,2,3,4},{0,5,6,7},{0,0,8,9},{0,0,0,10}};

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

}
