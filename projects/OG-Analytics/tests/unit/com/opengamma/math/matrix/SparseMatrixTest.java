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
public class SparseMatrixTest {
  double _data[][]={{1,2,3},{4,5,6}};

@Test
public void testContructFromArrayOfArrays() {
  new SparseMatrix(_data, 10, 8);
}


}
