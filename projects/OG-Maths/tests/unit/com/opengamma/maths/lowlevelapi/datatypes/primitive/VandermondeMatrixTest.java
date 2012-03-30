/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.DenseMatrix;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.VandermondeMatrix;

/**
 * Tests the Vandermonde Matrix type
 */
public class VandermondeMatrixTest {
  double[][] _data = {{1,2,4},{1,3,9},{1,4,16},{1,5,25}};

/**
 * there is no reason why these tests explicitly would fail, they rely on DenseMatrix supers
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
public void testConstructorFromDenseMatrix() {
  new VandermondeMatrix(new DenseMatrix(_data));
}


}