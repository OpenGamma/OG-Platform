/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.DenseMatrix;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.ToeplitzMatrix;

/**
 * Tests the Toeplitz Matrix type
 */
public class ToeplitzMatrixTest {
  double[][] _data = {{1,2,3,4},{5,1,2,3},{6,5,1,2},{7,6,5,1}};

/**
 * there is no reason why these tests explicitly would fail, they rely on DenseMatrix supers
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
public void testConstructorFromDenseMatrix() {
  new ToeplitzMatrix(new DenseMatrix(_data));
}


}