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

import org.apache.commons.lang.NotImplementedException;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.PackedMatrix;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.SparseCoordinateFormatMatrix;
import com.opengamma.maths.lowlevelapi.datatypes.primitive.PackedMatrix.allowZerosOn;

/**
 * Tests the PackedMatrix Class
 */
public class PackedMatrixTest {
  double _ragged[][] = { {1, 2, 3, 4 }, {5, 6, 7 }, {11, 12, 13, 14, 15 } };
  double _banded[][] = { {1, 2, 0, 0, 0 }, {3, 4, 5, 0, 0 }, {0, 6, 7, 8, 0 }, {0, 0, 9, 10, 11 }, {0, 0, 0, 12, 13 } };
  double _banded_zero[][] = { {1, 2, 0, 0, 0 }, {3, 4, 0, 5, 0 }, {0, 6, 7, 8, 0 }, {0, 0, 9, 10, 11 }, {0, 0, 0, 12, 13 } };
  double _bandedwithzerorow[][] = { {1, 2, 0, 0, 0 }, {3, 4, 5, 0, 0 }, {0, 0, 0, 0, 0 }, {0, 0, 6, 7, 8 }, {0, 0, 0, 9, 10 } };
  double _unwoundbanded[] = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 };
  double _unwoundbandedwithzerorow[] = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
  double _bandedBLAS[][] = { {0, 1, 2 }, {3, 4, 5 }, {6, 7, 8 }, {9, 10, 11 }, {12, 13, 0 } };
  double _bandedBLASWithZeroInMiddle[][] = { {0, 1, 2 }, {3, 0, 4 }, {5, 6, 7 }, {8, 9, 10 }, {11, 12, 0 } };

  /**
   * Test constructors
   */
  @Test(expectedExceptions = NotImplementedException.class)
  public void testRaggedConstructor() {
    new PackedMatrix(_ragged);
  }

  @Test
  public void testBandedConstructor() {
    new PackedMatrix(_banded);
  }

  @Test
  public void testFromDoubleMatrix2DConstructor() {
    new PackedMatrix(new DoubleMatrix2D(_banded));
  }

  @Test
  public void testBandedWithZeroRowConstructor() {
    new PackedMatrix(_bandedwithzerorow);
  }

  @Test
  public void testBandedWithZeroConstructor() {
    new PackedMatrix(_banded_zero);
  }


  // test all the enumerated paths
  @Test
  public void testBandedWithZeroRowRightSideZerosAllowedConstructor() {
    double[] expectedData={1.0, 2.0, 0.0, 0.0, 0.0, 3.0, 4.0, 5.0, 0.0, 0.0, 6.0, 7.0, 8.0, 9.0, 10.0};
    PackedMatrix M = new PackedMatrix(_bandedwithzerorow, allowZerosOn.rightSide, 5, 5);
    assertTrue(Arrays.equals(M.getData(),expectedData));
  }

  @Test
  public void testBandedWithZeroRowLeftSideZerosAllowedConstructor() {
    double[] expectedData={1, 2, 3, 4, 5, 0, 0, 6, 7, 8, 0, 0, 0, 9, 10};
    PackedMatrix M = new PackedMatrix(_bandedwithzerorow, allowZerosOn.leftSide, 5, 5);
    assertTrue(Arrays.equals(M.getData(),expectedData));
  }

  @Test
  public void testBandedWithZeroRowBothSideZerosAllowedConstructor() {
    double[] expectedData={1, 2, 0, 0, 0 , 3, 4, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 7, 8, 0, 0, 0, 9, 10 };
    PackedMatrix M = new PackedMatrix(_bandedwithzerorow, allowZerosOn.bothSides, 5, 5);
    assertTrue(Arrays.equals(M.getData(),expectedData));
  }

  @Test
  public void testBandedWithZeroRowNOZerosAllowedConstructor() {
    double[] expectedData={1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
    PackedMatrix M = new PackedMatrix(_bandedwithzerorow, allowZerosOn.none, 5, 5);
    assertTrue(Arrays.equals(M.getData(),expectedData));
  }


  // test some specific troublesome cases
  @Test
  public void testBandedBLASWithPaddedZerosConstructor() {
    new PackedMatrix(_bandedBLAS, allowZerosOn.bothSides, 5, 3);
  }

  @Test
  public void testBandedBLASReturnMatrixValidity() {
    PackedMatrix tmp = new PackedMatrix(_bandedBLAS, allowZerosOn.bothSides);
    for (int i = 0; i < _bandedBLAS.length; i++) {
      assertTrue(Arrays.equals(_bandedBLAS[i], tmp.getRowElements(i)));
    }
  }

  @Test
  public void testBandedBLASWithZeroInMiddleReturnMatrixValidity() {
    PackedMatrix tmp = new PackedMatrix(_bandedBLASWithZeroInMiddle, allowZerosOn.bothSides);
    for (int i = 0; i < _bandedBLASWithZeroInMiddle.length; i++) {
      assertTrue(Arrays.equals(_bandedBLASWithZeroInMiddle[i], tmp.getRowElements(i)));
    }
  }

  /**
   * Test methods
   */

  @Test
  // test sanity of getNumberOfElements
  public void testGetNumberOfElements() {
    PackedMatrix tmp = new PackedMatrix(_banded);
    assertEquals(25, tmp.getNumberOfElements());
  }

  @Test
  // test sanity of getNumberOfNonZeroElements
  public void testGetNumberOfNonZeroElements() {
    PackedMatrix tmp = new PackedMatrix(_banded);
    assertEquals(_unwoundbanded.length, tmp.getNumberOfNonZeroElements());
  }

  @Test
  // test sanity of getEntry via a coordinate index
  public void testGetEntryDoubleIndex() {
    PackedMatrix tmp = new PackedMatrix(_banded);
    for (int i = 0; i < _banded.length; i++) {
      for (int j = 0; j < _banded[i].length; j++) {
        assertEquals(tmp.getEntry(i, j), _banded[i][j]);
      }
    }
    tmp = new PackedMatrix(_bandedwithzerorow);
    for (int i = 0; i < _bandedwithzerorow.length; i++) {
      for (int j = 0; j < _bandedwithzerorow[i].length; j++) {
        assertEquals(tmp.getEntry(i, j), _bandedwithzerorow[i][j]);
      }
    }
    tmp = new PackedMatrix(_bandedBLAS, allowZerosOn.none);
    for (int i = 0; i < _bandedBLAS.length; i++) {
      for (int j = 0; j < _bandedBLAS[i].length; j++) {
        assertEquals(tmp.getEntry(i, j), _bandedBLAS[i][j]);
      }
    }
  }

  @Test
  // test sanity of getEntry via a single index
  public void testGetEntrySingleIndex() {
    PackedMatrix tmp = new PackedMatrix(_banded);
    for (int i = 0; i < _banded.length; i++) {
      for (int j = 0; j < _banded[i].length; j++) {
        assertEquals(tmp.getEntry(i * _banded[0].length + j), _banded[i][j]);
      }
    }
    tmp = new PackedMatrix(_bandedBLAS, allowZerosOn.none);
    for (int i = 0; i < _bandedBLAS.length; i++) {
      for (int j = 0; j < _bandedBLAS[i].length; j++) {
        assertEquals(tmp.getEntry(i * _bandedBLAS[0].length + j), _bandedBLAS[i][j]);
      }
    }
  }

  @Test
  // test sanity of getFullRow
  public void testGetFullRow() {
    PackedMatrix tmp = new PackedMatrix(_banded);
    for (int i = 0; i < _banded.length; i++) {
      assertTrue(Arrays.equals(_banded[i], tmp.getFullRow(i)));
    }
    tmp = new PackedMatrix(_bandedBLAS);
    for (int i = 0; i < _bandedBLAS.length; i++) {
      assertTrue(Arrays.equals(_bandedBLAS[i], tmp.getFullRow(i)));
    }
  }

  @Test
  // test sanity of getRowElements
  public void testGetRowElements() {
    PackedMatrix tmp = new PackedMatrix(_banded, allowZerosOn.none);
    double[][] answer = { {1, 2 }, {3, 4, 5 }, {6, 7, 8 }, {9, 10, 11 }, {12, 13 } };
    for (int i = 0; i < _banded.length; i++) {
      assertTrue(Arrays.equals(answer[i], tmp.getRowElements(i)));
    }
    tmp = new PackedMatrix(_bandedBLAS, allowZerosOn.bothSides);
    for (int i = 0; i < _bandedBLAS.length; i++) {
      assertTrue(Arrays.equals(_bandedBLAS[i], tmp.getRowElements(i)));
    }
    double[][] answerLHS = { {0, 1, 2 }, {3, 4, 5 }, {6, 7, 8 }, {9, 10, 11 }, {12, 13 } };
    tmp = new PackedMatrix(_bandedBLAS, allowZerosOn.leftSide);
    for (int i = 0; i < _bandedBLAS.length; i++) {
      assertTrue(Arrays.equals(answerLHS[i], tmp.getRowElements(i)));
    }
    double[][] answerRHS = { {1, 2 }, {3, 4, 5 }, {6, 7, 8 }, {9, 10, 11 }, {12, 13, 0 } };
    tmp = new PackedMatrix(_bandedBLAS, allowZerosOn.rightSide);
    for (int i = 0; i < _bandedBLAS.length; i++) {
      assertTrue(Arrays.equals(answerRHS[i], tmp.getRowElements(i)));
    }
  }

  @Test
  // test sanity of getColumnElements
  public void testGetColumnElements() {
    PackedMatrix tmp = new PackedMatrix(_banded, allowZerosOn.none);
    double[][] answer = { {1, 3 }, {2, 4, 6 }, {5, 7, 9 }, {8, 10, 12 }, {11, 13 } };
    for (int i = 0; i < _banded[0].length; i++) {
      assertTrue(Arrays.equals(answer[i], tmp.getColumnElements(i)));
    }
    tmp = new PackedMatrix(_bandedBLAS, allowZerosOn.bothSides);
    double[][] answer2 = { {0, 3, 6, 9, 12 }, {1, 4, 7, 10, 13 }, {2, 5, 8, 11, 0 } };
    for (int i = 0; i < _bandedBLAS[0].length; i++) {
      assertTrue(Arrays.equals(answer2[i], tmp.getColumnElements(i)));
    }
    tmp = new PackedMatrix(_bandedBLAS, allowZerosOn.leftSide);
    double[][] answer3 = { {0, 3, 6, 9, 12 }, {1, 4, 7, 10, 13 }, {2, 5, 8, 11} };
    for (int i = 0; i < _bandedBLAS[0].length; i++) {
      assertTrue(Arrays.equals(answer3[i], tmp.getColumnElements(i)));
    }
    tmp = new PackedMatrix(_bandedBLAS, allowZerosOn.rightSide);
    double[][] answer4 = { {3, 6, 9, 12 }, {1, 4, 7, 10, 13 }, {2, 5, 8, 11, 0 } };
    for (int i = 0; i < _bandedBLAS[0].length; i++) {
      assertTrue(Arrays.equals(answer4[i], tmp.getColumnElements(i)));
    }
  }

  @Test
  // test sanity of toArray
  public void testToArray() {
    PackedMatrix tmp = new PackedMatrix(_banded);
    assertTrue(Arrays.deepEquals(tmp.toArray(), _banded));
    tmp = new PackedMatrix(_bandedBLAS, allowZerosOn.none);
    assertTrue(Arrays.deepEquals(tmp.toArray(), _bandedBLAS));
  }

  //test sanity of equals and hashcode
  @Test
  public void testEqualsAndHashCode() {
    PackedMatrix N;
    PackedMatrix M = new PackedMatrix(_banded);

    assertTrue(M.equals(M)); // test this = obj
    assertFalse(M.equals(null)); // test obj != null
    assertFalse(M.equals(M.getClass())); // test obj class

    double[][] differentDataValues = { {10, 2, 0, 0, 0 }, {3, 4, 5, 0, 0 }, {0, 6, 7, 8, 0 }, {0, 0, 9, 10, 11 }, {0, 0, 0, 12, 13 } };
    double[][] differentDataRowPtrOrder = { {0, 1, 2, 0, 0 }, {3, 4, 5, 0, 0 }, {0, 6, 7, 8, 0 }, {0, 0, 9, 10, 11 }, {0, 0, 0, 12, 13 } };
    double[][] differentDataColCount = { {1, 2, 3, 0, 0 }, {4, 5, 0, 0, 0 }, {0, 6, 7, 8, 0 }, {0, 0, 9, 10, 11 }, {0, 0, 0, 12, 13 } };
    // test different values
    N = new PackedMatrix(differentDataValues);
    assertFalse(M.equals(N));

    // test same values different RowPtrs
    N = new PackedMatrix(differentDataRowPtrOrder);
    assertFalse(M.equals(N));

    // test same values different ColCount
    N = new PackedMatrix(differentDataColCount);
    assertFalse(M.equals(N));

    // test same values different YOrder
    double[][] differentNewDataYOrder1 = { {1, 2 }, {0, 0 } };
    double[][] differentNewDataYOrder2 = { {0, 0 }, {1, 2 } };
    SparseCoordinateFormatMatrix P1 = new SparseCoordinateFormatMatrix(differentNewDataYOrder1);
    SparseCoordinateFormatMatrix P2 = new SparseCoordinateFormatMatrix(differentNewDataYOrder2);
    assertFalse(P1.equals(P2));

    // test matrices that are identical mathematically are identical programatically.
    N = new PackedMatrix(_banded);
    assertTrue(M.equals(N));
    assertEquals(M.hashCode(), N.hashCode());

  }

}
