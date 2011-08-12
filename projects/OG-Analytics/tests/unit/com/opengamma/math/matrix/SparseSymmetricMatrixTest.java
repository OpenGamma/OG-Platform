/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import org.testng.annotations.Test;

/**
 * Tests sparse symmetric matrix behaviour
 */
public class SparseSymmetricMatrixTest {
  /**
   * |1 0 2 3|
   * |0 4 0 5|
   * |2 0 6 7|
   * |3 5 7 8|
   */
  double _ssym[][]={{1,0,2,3},{0,4,0,5},{2,0,6,7},{3,5,7,8}};

@Test
  public void testConstructionFromFull() {
    new SparseSymmetricMatrix(_ssym);
}

@Test
public void testStringConstructFromDoubleMatrix2D() {
  new SparseSymmetricMatrix(new DoubleMatrix2D(_ssym));
}

}
