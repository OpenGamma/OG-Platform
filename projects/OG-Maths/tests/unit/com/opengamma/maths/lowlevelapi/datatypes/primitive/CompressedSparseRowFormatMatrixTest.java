/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.datatypes.primitive;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.CompressedSparseRowFormatMatrix;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.SparseCoordinateFormatMatrix;

/**
 * Tests the CompressedSparseRowFormatMatrix format to make sure it is vaguely sane
 */
public class CompressedSparseRowFormatMatrixTest {
  double[][]data = {{1,2,0,0},{3,0,4,0},{0,5,6,0},{0,0,7,0}};
  double[] expectedData = {1.0,2.0,3.0,4.0,5.0,6.0,7.0};
  int[] expectedRowPtr = {0,2,4,6,7};
  int[] expectedColIdx = {0,1,0,2,1,2,2};

  int [] tupleX = {0,1,0,2,1,2,2};
  int [] tupleY = {0,0,1,1,2,2,3};
  double [] tupleV = {1,2,3,4,5,6,7};

//Test constructors
@Test
public void testConstructorFromDoubleMatrix2D() {
  DoubleMatrix2D tmp = new DoubleMatrix2D(data);
  CompressedSparseRowFormatMatrix M = new CompressedSparseRowFormatMatrix(tmp);
  assertTrue(Arrays.equals(expectedRowPtr,M.getRowPtr()));
  assertTrue(Arrays.equals(expectedColIdx,M.getColumnIndex()));
  assertTrue(Arrays.equals(expectedData,M.getNonZeroElements()));
}

@Test
public void testConstructorSparseCoordinateFormat() {
  SparseCoordinateFormatMatrix tmp = new SparseCoordinateFormatMatrix(data);
  CompressedSparseRowFormatMatrix M = new CompressedSparseRowFormatMatrix(tmp);
  assertTrue(Arrays.equals(expectedRowPtr,M.getRowPtr()));
  assertTrue(Arrays.equals(expectedColIdx,M.getColumnIndex()));
  assertTrue(Arrays.equals(expectedData,M.getNonZeroElements()));
}

@Test
public void testConstructorDoubleArrays() {
  CompressedSparseRowFormatMatrix M = new CompressedSparseRowFormatMatrix(data);
  assertTrue(Arrays.equals(expectedRowPtr,M.getRowPtr()));
  assertTrue(Arrays.equals(expectedColIdx,M.getColumnIndex()));
  assertTrue(Arrays.equals(expectedData,M.getNonZeroElements()));
}


@Test
public void testConstructorTupleComputedDimension(){
  CompressedSparseRowFormatMatrix M = new CompressedSparseRowFormatMatrix(tupleX, tupleY, tupleV);
  assertTrue(Arrays.equals(expectedRowPtr,M.getRowPtr()));
  assertTrue(Arrays.equals(expectedColIdx,M.getColumnIndex()));
  assertTrue(Arrays.equals(expectedData,M.getNonZeroElements()));
}

@Test
public void testConstructorTupleGivenDimension(){
  CompressedSparseRowFormatMatrix M = new CompressedSparseRowFormatMatrix(tupleX, tupleY, tupleV, 4, 4);
  assertTrue(Arrays.equals(expectedRowPtr,M.getRowPtr()));
  assertTrue(Arrays.equals(expectedColIdx,M.getColumnIndex()));
  assertTrue(Arrays.equals(expectedData,M.getNonZeroElements()));
}

// Test methods
@Test
public void testGetColumnIndex() {
  CompressedSparseRowFormatMatrix M = new CompressedSparseRowFormatMatrix(data);
  assertTrue(Arrays.equals(expectedColIdx,M.getColumnIndex()));
}

@Test
public void testGetRowPtr() {
  CompressedSparseRowFormatMatrix M = new CompressedSparseRowFormatMatrix(data);
  assertTrue(Arrays.equals(expectedRowPtr,M.getRowPtr()));
}

@Test
public void testGetNonZeroValues() {
  CompressedSparseRowFormatMatrix M = new CompressedSparseRowFormatMatrix(data);
  assertTrue(Arrays.equals(expectedData,M.getNonZeroElements()));
}

@Test
public void getNumberOfRows() {
  CompressedSparseRowFormatMatrix M = new CompressedSparseRowFormatMatrix(data);
  assertEquals(4,M.getNumberOfRows());
}

@Test
public void testGetNumberOfColumns() {
  CompressedSparseRowFormatMatrix M = new CompressedSparseRowFormatMatrix(data);
  assertEquals(4,M.getNumberOfColumns());
}

@Test
public void testGetNumberOfElements() {
  CompressedSparseRowFormatMatrix M = new CompressedSparseRowFormatMatrix(data);
  assertEquals(16,M.getNumberOfElements());
}

@Test
public void testGetNumberOfNonZeroElements() {
  CompressedSparseRowFormatMatrix M = new CompressedSparseRowFormatMatrix(data);
  assertEquals(7,M.getNumberOfNonzeroElements());
}

@Test
public void testGetEntry() {
  CompressedSparseRowFormatMatrix M = new CompressedSparseRowFormatMatrix(data);
  for (int i = 0; i < data.length; i++) {
    for (int j = 0; j < data[i].length; j++) {
      assertEquals(Double.doubleToLongBits(data[i][j]),Double.doubleToLongBits(M.getEntry(i,j)));
    }
  }
}

@Test
public void testGetFullRow() {
  CompressedSparseRowFormatMatrix M = new CompressedSparseRowFormatMatrix(data);
  for(int i = 0; i < data.length; i++) {
    assertTrue(Arrays.equals(M.getFullRow(i),data[i]));
  }
}

@Test
public void testGetFullColumn() {
  CompressedSparseRowFormatMatrix tmp = new CompressedSparseRowFormatMatrix(data);
  double[] col = new double[data.length];
  for(int i = 0; i < data[0].length; i++) {
    // assemble column
    for(int j = 0; j < data[0].length; j++) {
      col[j] = data[j][i];
    }
    assertTrue(Arrays.equals(col,tmp.getFullColumn(i)));
  }
}

@Test
public void testGetRowElements() {
  double[][] compressed = {{1,2},{3,4},{5,6},{7}};
  CompressedSparseRowFormatMatrix tmp = new CompressedSparseRowFormatMatrix(data);
  for(int i = 0; i < data.length; i++) {
    assertTrue(Arrays.equals(compressed[i],tmp.getRowElements(i)));
  }
}

@Test
public void testGetColumnElements() {
  double[][] compressed = {{1,3},{2,5},{4,6,7},{}};
  CompressedSparseRowFormatMatrix tmp = new CompressedSparseRowFormatMatrix(data);
  for(int i = 0; i < data[0].length; i++) {
    assertTrue(Arrays.equals(compressed[i],tmp.getColumnElements(i)));
  }
}

@Test
public void testGetMaxNonZerosInsignificantDirection() {
  CompressedSparseRowFormatMatrix M = new CompressedSparseRowFormatMatrix(data);
  assertEquals(2,M.getMaxNonZerosInSignificantDirection());
}


@Test
public void testToArray() {
  CompressedSparseRowFormatMatrix M = new CompressedSparseRowFormatMatrix(data);
  assertTrue(Arrays.deepEquals(data,M.toArray()));
}

@Test
public void testToDenseMatrix() {
  CompressedSparseRowFormatMatrix M = new CompressedSparseRowFormatMatrix(data);
  DoubleMatrix2D N = new DoubleMatrix2D(data);
  assertTrue(N.equals(M.toDenseMatrix()));
}

@Test
public void testEqualsAndHashCode() {
  CompressedSparseRowFormatMatrix M = new CompressedSparseRowFormatMatrix(data);
  CompressedSparseRowFormatMatrix N = new CompressedSparseRowFormatMatrix(data);

  assertTrue(M.equals(M)); // test this = obj
  assertFalse(M.equals(null)); // test obj != null
  assertFalse(M.equals(M.getClass())); // test obj class
  assertTrue(M.equals(N)); // test identical objects in all but address
  assertEquals(M.hashCode(), N.hashCode()); // test  identical objects in all but address give same hashcode

  // test same sparsity layout, different data
  double[][]tmpdata1 = {{7,2,0,0},{3,0,4,0},{0,5,6,0},{0,0,7,0}};
  N = new CompressedSparseRowFormatMatrix(tmpdata1);
  assertFalse(M.equals(N));

  // test same data different row sparsity layout
  double[][]tmpdata2 = {{1,0,0,0},{2,3,4,0},{0,5,6,0},{0,0,7,0}};
  N = new CompressedSparseRowFormatMatrix(tmpdata2);
  assertFalse(M.equals(N));

  // test same data different column sparsity layout
  double[][]tmpdata3 = {{1,0,2,0},{3,0,4,0},{0,5,6,0},{0,0,7,0}};
  N = new CompressedSparseRowFormatMatrix(tmpdata3);
  assertFalse(M.equals(N));

  // test same data different column count
  double[][]tmpdata4 = {{1,0,2},{3,0,4},{0,5,6},{0,0,7}};
  N = new CompressedSparseRowFormatMatrix(tmpdata4);
  assertFalse(M.equals(N));

  // test same data different row count
  double[][]tmpdata5 = {{1,0,2,0},{3,0,4,0},{0,5,6,7}};
  N = new CompressedSparseRowFormatMatrix(tmpdata5);
  assertFalse(M.equals(N));
}

}
