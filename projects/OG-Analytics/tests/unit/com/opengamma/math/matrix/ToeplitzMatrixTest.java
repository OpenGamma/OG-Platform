/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import org.testng.annotations.Test;

/**
 * Tests the Toeplitz Matrix type
 */
public class ToeplitzMatrixTest {
  double[][] _data = {{1,2,3,4},{5,1,2,3},{6,5,1,2},{7,6,5,1}};

/**
 * there is no reason why these tests explicitly would fail, they rely on FullMatrix supers
 * and so if these tests fail something is broken upstream.
 */

@Test
public void testConstructorFromArrayOfArrays() {
  new ToeplitzMatrix(_data);
}

@Test
public void testConstructorFromDoubleMatrix2D() {
  new ToeplitzMatrix(new DoubleMatrix2D(_data));
}

@Test
public void testConstructorFromFullMatrix() {
  new ToeplitzMatrix(new FullMatrix(_data));
}


}