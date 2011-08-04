/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 *
 */
public class CompressedSparseRowFormatMatrixTest {
  double[][]data = {{1,2,0,0},{3,0,4,0},{0,5,6,0},{0,0,7,0}};
  double[] expectedData = {1.0,2.0,3.0,4.0,5.0,6.0,7.0};
  int[] expectedRowPtr = {0,2,4,6,7};
  int[] expectedColIdx = {0,1,0,2,1,2,2};
  private static final Logger s_logger = LoggerFactory.getLogger(CompressedSparseRowFormatMatrixTest.class);

@Test
public void testConstructorFromDoubleMatrix2D() {
  DoubleMatrix2D tmp = new DoubleMatrix2D(data);
  CompressedSparseRowFormatMatrix M = new CompressedSparseRowFormatMatrix(tmp);
  s_logger.info(M.toString());
  assertTrue(Arrays.equals(expectedRowPtr,M.getRowPtr()));
}

}
