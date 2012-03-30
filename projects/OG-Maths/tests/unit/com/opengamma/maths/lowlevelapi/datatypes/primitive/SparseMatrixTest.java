/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;

import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.CompressedSparseColumnFormatMatrix;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.CompressedSparseRowFormatMatrix;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.SparseCoordinateFormatMatrix;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.SparseMatrix;

/**
 *
 */
public class SparseMatrixTest {
  double _densedata[][]={{1,2,3,4},{5,6,7,8},{9,10,11,12},{13,14,15,0}};
  double _sparsedata[][]={{1,0,2,3},{4,5,0,6},{7,8,9,0},{0,10,11,0},{0,0,0,0}};
  double _sparserdata[][]={{1,0,0,0},{0,2,0,0},{0,0,3,0},{0,0,0,4},{0,0,0,0}};

@Test
public void testContructFromArrayOfArraysWithDimension() {
  new SparseMatrix(_sparsedata, 10, 8);
}

@Test(expectedExceptions = IllegalArgumentException.class) // tests for bad dimension request
public void testContructFromArrayOfArraysWithBADROWDimension() {
  new SparseMatrix(_sparsedata, 1, 6);
}

@Test(expectedExceptions = IllegalArgumentException.class) // tests for bad dimension request
public void testContructFromArrayOfArraysWithBADCOLDimension() {
  new SparseMatrix(_sparsedata, 6, 1);
}

@Test
public void testContructFromArrayOfArraysNODimension() {
  new SparseMatrix(_sparsedata);
}

@Test
public void testContructFromDoubleMatrix2DWithDimension() {
  new SparseMatrix(new DoubleMatrix2D(_sparsedata), 10, 8);
}

@Test
public void testContructFromDoubleMatrix2DNODimension() {
  new SparseMatrix(new DoubleMatrix2D(_sparsedata));
}

@Test
public void testContructFromArrayOfArraysWithDimensionAndType() {
  SparseMatrix tmp = new SparseMatrix(_sparsedata, 10, 8, SparseMatrix.majorness.column);
  assertTrue(tmp.getSparseObject() instanceof CompressedSparseColumnFormatMatrix);
}

@Test
public void testContructFromArrayOfArraysNODimensionAndType() {
  SparseMatrix tmp = new SparseMatrix(_sparsedata, SparseMatrix.majorness.column);
  assertTrue(tmp.getSparseObject() instanceof CompressedSparseColumnFormatMatrix);
}

@Test
public void testGetSparseObject() {
  SparseMatrix tmp;
  tmp = new SparseMatrix(_densedata);
  assertTrue(tmp.getSparseObject() instanceof SparseCoordinateFormatMatrix);
  tmp = new SparseMatrix(_sparsedata);
  assertTrue(tmp.getSparseObject() instanceof CompressedSparseRowFormatMatrix);
  tmp = new SparseMatrix(_sparserdata);
  assertTrue(tmp.getSparseObject() instanceof CompressedSparseRowFormatMatrix);
}

// everything else is inherited and tested already
}
