/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import org.testng.annotations.Test;

/**
 * Tests the Vandermonde Matrix type
 */
public class VandermondeMatrixTest {
  double[][] _data = {{1,2,4},{1,3,9},{1,4,16},{1,5,25}};

/**
 * there is no reason why these tests explicitly would fail, they rely on FullMatrix supers
 * and so if these tests fail something is broken upstream.
 */

@Test
public void testConstructorFromArrayOfArrays() {
  new VandermondeMatrix(_data);
}

@Test
public void testConstructorFromDoubleMatrix2D() {
  new VandermondeMatrix(new DoubleMatrix2D(_data));
}

@Test
public void testConstructorFromFullMatrix() {
  new VandermondeMatrix(new FullMatrix(_data));
}


}