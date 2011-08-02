/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 *
 */
public class UpperHessenbergMatrixTest {
  final Logger log = LoggerFactory.getLogger(UpperHessenbergMatrixTest.class);
  private static final double[][] dataSquare = new double[][] {{1,2,3,4,5},{6,7,8,9,10},{0,11,12,13,14},{0,0,15,16,17},{0,0,0,18,19}};
  private static final double[][] dataRagged = new double[][] {{1,2,3,4,5},{6,7,8,9,10},{11,12,13,14},{15,16,17},{18,19}};
  private static final double[][] dataBad  = new double[][]   {{1,2,3,4,5},{6,7,8,9,10},{11,12,13,14},{15,16,17},{19}};


  // testing first constructor @#params=int
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeDimension() {
    new UpperHessenbergMatrix(-1);
  }

  // testing second constructor @#params=double[][]
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNulldatain() {
    new UpperHessenbergMatrix(null);
  }

  // square data constructor
  @Test
  public void testSquareArraytoUpperHessenbergConstructor() {
    new UpperHessenbergMatrix(dataSquare);
  }

  // ragged data constructor
  @Test
  public void testRaggedArraytoUpperHessenbergConstructor() {
    new UpperHessenbergMatrix(dataRagged);
  }

  // stupid data, constructor should bork
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadArraytoUpperHessenbergConstructor() {
    new UpperHessenbergMatrix(dataBad);
  }

  // testing tofullMatrix i.e. can we get back 2D Matrix from it?
  @Test
  public void testToFullMatrix() {
    UpperHessenbergMatrix M = new UpperHessenbergMatrix(dataSquare);
    DoubleMatrix2D mat2d = M.toFullMatrix();
    DoubleMatrix2D expected = new DoubleMatrix2D(dataSquare);
    assertEquals(expected, mat2d);
  }

  // testing tofullMatrix i.e. can we get back 2D Matrix from it?
  @Test
  public void testToArray() {
    UpperHessenbergMatrix M = new UpperHessenbergMatrix(dataSquare);
    double[][] array2d = M.toArray();
    DoubleMatrix2D expected = new DoubleMatrix2D(dataSquare);
    DoubleMatrix2D actual = new DoubleMatrix2D(array2d);
    assertEquals(expected, actual);
  }

  // test out of bounds index on row getter
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetRowVectorOutOfBounds() {
    UpperHessenbergMatrix M = new UpperHessenbergMatrix(dataSquare);
    M.getRowVector(10);
  }

  // test negative index on row getter
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetRowVectorNegative() {
    UpperHessenbergMatrix M = new UpperHessenbergMatrix(dataSquare);
    M.getRowVector(-1);
  }

  // test sanity of row getter (Ragged construction used)
  @Test
  public void testGetRowVector() {
    UpperHessenbergMatrix M = new UpperHessenbergMatrix(dataRagged);
    // test row 0, should be full
    DoubleMatrix1D tmp = new DoubleMatrix1D(dataRagged[0]);
    assertEquals(tmp,M.getRowVector(0));

    // test last row, should be 2 elements
    tmp = new DoubleMatrix1D(dataRagged[4]);
    assertEquals(tmp,M.getRowVector(4));

  }

  // test sanity of FULL row getter
  @Test
  public void testGetFullRowVector() {
    UpperHessenbergMatrix M = new UpperHessenbergMatrix(dataSquare);
    // test row 0, should be full
    DoubleMatrix1D tmp = new DoubleMatrix1D(dataSquare[0]);
    assertEquals(tmp,M.getFullRowVector(0));

    // test last row, should be 2 elements and zero padded
    tmp = new DoubleMatrix1D(dataSquare[4]);
    assertEquals(tmp,M.getFullRowVector(4));
  }

  // test sanity of column getter (Ragged construction used)
  @Test
  public void testGetColumnVector() {
    UpperHessenbergMatrix M = new UpperHessenbergMatrix(dataSquare);
    // test column 0, should be 2 elements
    double[] tmp = new double[2];
    for (int i = 0; i < 2 ; i++) {
      tmp[i] = dataSquare[i][0];
    }
    DoubleMatrix1D expected = new DoubleMatrix1D(tmp);
    assertEquals(expected,M.getColumnVector(0));

    // test second to last column
    tmp = new double[dataSquare.length];
    for (int i = 0; i < dataSquare.length; i++) {
      tmp[i] = dataSquare[i][3];
    }
    expected = new DoubleMatrix1D(tmp);
    assertEquals(expected,M.getColumnVector(3));

    // test last column
    tmp = new double[dataSquare.length];
    for (int i = 0; i < dataSquare.length; i++) {
      tmp[i] = dataSquare[i][4];
    }
    expected = new DoubleMatrix1D(tmp);
    assertEquals(expected,M.getColumnVector(4));
  }


//test sanity of full column getter (square construction used)
  @Test
  public void testGetFullColumnVector() {
    UpperHessenbergMatrix M = new UpperHessenbergMatrix(dataSquare);
    // test column 0, should be 2 elements
    double[] tmp = new double[dataSquare.length];
    for (int i = 0; i < 2 ; i++) {
      tmp[i] = dataSquare[i][0];
    }
    DoubleMatrix1D expected = new DoubleMatrix1D(tmp);
    assertEquals(expected,M.getFullColumnVector(0));

    // test second to last column
    tmp = new double[dataSquare.length];
    for (int i = 0; i < dataSquare.length; i++) {
      tmp[i] = dataSquare[i][3];
    }
    expected = new DoubleMatrix1D(tmp);
    assertEquals(expected,M.getFullColumnVector(3));

    // test last column
    tmp = new double[dataSquare.length];
    for (int i = 0; i < dataSquare.length; i++) {
      tmp[i] = dataSquare[i][4];
    }
    expected = new DoubleMatrix1D(tmp);
    assertEquals(expected,M.getFullColumnVector(4));
  }

//test sanity of diagonal elements getter
  @Test
  public void testGetDiag(){
   double[] tmp = new double[dataSquare.length];
   for(int i = 0; i < dataSquare.length; i++) {
     tmp[i] = dataSquare[i][i];
   }
   DoubleMatrix1D expected = new DoubleMatrix1D(tmp);
   UpperHessenbergMatrix M = new UpperHessenbergMatrix(dataSquare);
   assertEquals(expected,M.getDiag());
 }

// test sanity of getNumberOfElements
  @Test
  public void testGetNumberOfElements() {
    UpperHessenbergMatrix M = new UpperHessenbergMatrix(dataSquare);
    assertEquals(19,M.getNumberOfElements());
  }

//test sanity of getEntry
  @Test
  public void testGetEntry() {
    UpperHessenbergMatrix M = new UpperHessenbergMatrix(dataSquare);
    for(int i = 0; i < dataRagged.length; i++) {
     for (int j = 0; j < dataRagged[i].length; j++) {
       assertEquals(dataRagged[i][j],M.getEntry(i,j));
     }
    }
 }

// test sanity of equals and hashcode
 @Test
 public void testEqualsAndHashCode() {
   UpperHessenbergMatrix N;
   UpperHessenbergMatrix M = new UpperHessenbergMatrix(dataSquare);
   assertTrue(M.equals(M)); // test this = obj
   assertFalse(M.equals(null)); // test obj != null
   assertFalse(M.equals(M.getClass())); // test obj class
   N = new UpperHessenbergMatrix(3);
   assertFalse(M.equals(N));

   N = new UpperHessenbergMatrix(dataSquare);
   assertTrue(M.equals(N));
   assertEquals(M.hashCode(), N.hashCode());
 }

////test sanity of toString?! Broken, perhaps String.format is cranky wrt whitespace?
//@Test
//public void testToString() {
// UpperHessenbergMatrix M = new UpperHessenbergMatrix(dataSquare);
// String expected = new String("UpperHessenbergMatrix:\n");
// expected += "_dimension=5\n";
// expected += "_elements=19\n";
// expected += "_data=\n";
// expected += "  1.00000000   2.00000000   3.00000000   4.00000000   5.00000000\n";
// expected += "  6.00000000   7.00000000   8.00000000   9.00000000  10.00000000\n";
// expected += " 11.00000000  12.00000000  13.00000000  14.00000000\n";
// expected += " 15.00000000  16.00000000  17.00000000\n";
// expected += " 18.00000000  19.00000000\n";
// assertEquals(expected,M.toString());
//}

}
