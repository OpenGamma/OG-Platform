/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import org.testng.annotations.Test;

/**
 * Tests the symmetric matrix class
 */
public class SymmetricMatrixTest {
/*
 *  use test matrix
  * |1 2 3 4|
  * |2 1 5 6|
  * |3 5 1 7|
  * |4 6 7 1|
*/
  private double[][] sym={{1,2,3,4},{2,1,5,6},{3,5,1,7},{4,6,7,1}};
@Test
  public void testConstructFromFull() {
    new SymmetricMatrix(sym);
  }

@Test
  public void testConstructFromDoubleMatrix2D() {
    new SymmetricMatrix(sym);
  }

}
