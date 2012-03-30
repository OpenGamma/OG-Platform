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
import com.opengamma.maths.lowlevelapi.datatypes.primitive.CompressedSparseColumnFormatMatrix;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.SparseCoordinateFormatMatrix;

/**
 * Tests the CompressedSparseColumnFormatMatrix format to make sure it is vaguely sane
 */
public class CompressedSparseColumnFormatMatrixTest {
  double[][] data = { {1, 2, 0, 0 }, {3, 0, 4, 0 }, {0, 5, 6, 0 }, {0, 0, 7, 0 } };
  double[] expectedData = {1.0, 3.0, 2.0, 5.0, 4.0, 6.0, 7.0 };
  int[] expectedColPtr = {0, 2, 4, 7, 7 };
  int[] expectedColPtrComputed = {0, 2, 4, 7};
  int[] expectedRowIdx = {0, 1, 0, 2, 1, 2, 3 };

  int[] tupleX = {0, 1, 0, 2, 1, 2, 2 };
  int[] tupleY = {0, 0, 1, 1, 2, 2, 3 };
  double[] tupleV = {1, 2, 3, 4, 5, 6, 7 };

  //Test constructors
  @Test
  public void testConstructorFromDoubleMatrix2D() {
    DoubleMatrix2D tmp = new DoubleMatrix2D(data);
    CompressedSparseColumnFormatMatrix M = new CompressedSparseColumnFormatMatrix(tmp);
    assertTrue(Arrays.equals(expectedColPtr, M.getColumnPtr()));
    assertTrue(Arrays.equals(expectedRowIdx, M.getRowIndex()));
    assertTrue(Arrays.equals(expectedData, M.getNonZeroElements()));
  }

  @Test
  public void testConstructorSparseCoordinateFormat() {
    SparseCoordinateFormatMatrix tmp = new SparseCoordinateFormatMatrix(data);
    CompressedSparseColumnFormatMatrix M = new CompressedSparseColumnFormatMatrix(tmp);
    assertTrue(Arrays.equals(expectedColPtr, M.getColumnPtr()));
    assertTrue(Arrays.equals(expectedRowIdx, M.getRowIndex()));
    assertTrue(Arrays.equals(expectedData, M.getNonZeroElements()));
  }

  @Test
  public void testConstructorDoubleArrays() {
    CompressedSparseColumnFormatMatrix M = new CompressedSparseColumnFormatMatrix(data);
    assertTrue(Arrays.equals(expectedColPtr, M.getColumnPtr()));
    assertTrue(Arrays.equals(expectedRowIdx, M.getRowIndex()));
    assertTrue(Arrays.equals(expectedData, M.getNonZeroElements()));
  }

  @Test
  public void testConstructorTupleComputedDimension() {
    CompressedSparseColumnFormatMatrix M = new CompressedSparseColumnFormatMatrix(tupleX, tupleY, tupleV);
    assertTrue(Arrays.equals(expectedColPtrComputed, M.getColumnPtr()));
    assertTrue(Arrays.equals(expectedRowIdx, M.getRowIndex()));
    assertTrue(Arrays.equals(expectedData, M.getNonZeroElements()));
  }

  @Test
  public void testConstructorTupleGivenDimension() {
    CompressedSparseColumnFormatMatrix M = new CompressedSparseColumnFormatMatrix(tupleX, tupleY, tupleV, 4, 4);
    assertTrue(Arrays.equals(expectedColPtr, M.getColumnPtr()));
    assertTrue(Arrays.equals(expectedRowIdx, M.getRowIndex()));
    assertTrue(Arrays.equals(expectedData, M.getNonZeroElements()));
  }

  // Test methods
  @Test
  public void testGetColumnIndex() {
    CompressedSparseColumnFormatMatrix M = new CompressedSparseColumnFormatMatrix(data);
    assertTrue(Arrays.equals(expectedRowIdx, M.getRowIndex()));
  }

  @Test
  public void testGetRowPtr() {
    CompressedSparseColumnFormatMatrix M = new CompressedSparseColumnFormatMatrix(data);
    assertTrue(Arrays.equals(expectedColPtr, M.getColumnPtr()));
  }

  @Test
  public void testGetNonZeroValues() {
    CompressedSparseColumnFormatMatrix M = new CompressedSparseColumnFormatMatrix(data);
    assertTrue(Arrays.equals(expectedData, M.getNonZeroElements()));
  }

  @Test
  public void getNumberOfRows() {
    CompressedSparseColumnFormatMatrix M = new CompressedSparseColumnFormatMatrix(data);
    assertEquals(4, M.getNumberOfRows());
  }

  @Test
  public void testGetNumberOfColumns() {
    CompressedSparseColumnFormatMatrix M = new CompressedSparseColumnFormatMatrix(data);
    assertEquals(4, M.getNumberOfColumns());
  }

  @Test
  public void testGetNumberOfElements() {
    CompressedSparseColumnFormatMatrix M = new CompressedSparseColumnFormatMatrix(data);
    assertEquals(16, M.getNumberOfElements());
  }

  @Test
  public void testGetNumberOfNonZeroElements() {
    CompressedSparseColumnFormatMatrix M = new CompressedSparseColumnFormatMatrix(data);
    assertEquals(7, M.getNumberOfNonzeroElements());
  }

  @Test
  public void testGetEntry() {
    CompressedSparseColumnFormatMatrix M = new CompressedSparseColumnFormatMatrix(data);
    for (int i = 0; i < data.length; i++) {
      for (int j = 0; j < data[i].length; j++) {
        assertEquals(Double.doubleToLongBits(data[i][j]), Double.doubleToLongBits(M.getEntry(i, j)));
      }
    }
  }

  @Test
  public void testGetFullRow() {
    CompressedSparseColumnFormatMatrix M = new CompressedSparseColumnFormatMatrix(data);
    for (int i = 0; i < data.length; i++) {
      assertTrue(Arrays.equals(M.getFullRow(i), data[i]));
    }
  }

  @Test
  public void testGetFullColumn() {
    CompressedSparseColumnFormatMatrix tmp = new CompressedSparseColumnFormatMatrix(data);
    double[] col = new double[data.length];
    for (int i = 0; i < data[0].length; i++) {
      // assemble column
      for (int j = 0; j < data[0].length; j++) {
        col[j] = data[j][i];
      }
      assertTrue(Arrays.equals(col, tmp.getFullColumn(i)));
    }
  }

  @Test
  public void testGetRowElements() {
    double[][] compressed = { {1, 2 }, {3, 4 }, {5, 6 }, {7 } };
    CompressedSparseColumnFormatMatrix tmp = new CompressedSparseColumnFormatMatrix(data);
    for (int i = 0; i < data.length; i++) {
      assertTrue(Arrays.equals(compressed[i], tmp.getRowElements(i)));
    }
  }

  @Test
  public void testGetColumnElements() {
    double[][] compressed = { {1, 3 }, {2, 5 }, {4, 6, 7 }, {} };
    CompressedSparseColumnFormatMatrix tmp = new CompressedSparseColumnFormatMatrix(data);
    for (int i = 0; i < data[0].length; i++) {
      assertTrue(Arrays.equals(compressed[i], tmp.getColumnElements(i)));
    }
  }

  @Test
  public void testGetMaxNonZerosInsignificantDirection() {
    CompressedSparseColumnFormatMatrix M = new CompressedSparseColumnFormatMatrix(data);
    assertEquals(3, M.getMaxNonZerosInSignificantDirection());
  }

  @Test
  public void testToArray() {
    CompressedSparseColumnFormatMatrix M = new CompressedSparseColumnFormatMatrix(data);
    assertTrue(Arrays.deepEquals(data, M.toArray()));
  }

  @Test
  public void testToDenseMatrix() {
    CompressedSparseColumnFormatMatrix M = new CompressedSparseColumnFormatMatrix(data);
    DoubleMatrix2D N = new DoubleMatrix2D(data);
    assertTrue(N.equals(M.toDenseMatrix()));
  }

  @Test
  public void testEqualsAndHashCode() {
    CompressedSparseColumnFormatMatrix M = new CompressedSparseColumnFormatMatrix(data);
    CompressedSparseColumnFormatMatrix N = new CompressedSparseColumnFormatMatrix(data);

    assertTrue(M.equals(M)); // test this = obj
    assertFalse(M.equals(null)); // test obj != null
    assertFalse(M.equals(M.getClass())); // test obj class
    assertTrue(M.equals(N)); // test identical objects in all but address
    assertEquals(M.hashCode(), N.hashCode()); // test  identical objects in all but address give same hashcode

    // test same sparsity layout, different data
    double[][] tmpdata1 = { {7, 2, 0, 0 }, {3, 0, 4, 0 }, {0, 5, 6, 0 }, {0, 0, 7, 0 } };
    N = new CompressedSparseColumnFormatMatrix(tmpdata1);
    assertFalse(M.equals(N));

    // test same data different row sparsity layout
    double[][] tmpdata2 = { {1, 0, 0, 0 }, {2, 3, 4, 0 }, {0, 5, 6, 0 }, {0, 0, 7, 0 } };
    N = new CompressedSparseColumnFormatMatrix(tmpdata2);
    assertFalse(M.equals(N));

    // test same data different column sparsity layout
    double[][] tmpdata3 = { {1, 0, 2, 0 }, {3, 0, 4, 0 }, {0, 5, 6, 0 }, {0, 0, 7, 0 } };
    N = new CompressedSparseColumnFormatMatrix(tmpdata3);
    assertFalse(M.equals(N));

    // test same data different column count
    double[][] tmpdata4 = { {1, 0, 2 }, {3, 0, 4 }, {0, 5, 6 }, {0, 0, 7 } };
    N = new CompressedSparseColumnFormatMatrix(tmpdata4);
    assertFalse(M.equals(N));

    // test same data different row count
    double[][] tmpdata5 = { {1, 0, 2, 0 }, {3, 0, 4, 0 }, {0, 5, 6, 7 } };
    N = new CompressedSparseColumnFormatMatrix(tmpdata5);
    assertFalse(M.equals(N));
  }

}
