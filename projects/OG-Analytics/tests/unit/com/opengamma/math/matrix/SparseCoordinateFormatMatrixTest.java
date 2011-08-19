package com.opengamma.math.matrix;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;

/**
 * Tests the SparseCoordinateFormatMatrix format to make sure it is vaguely sane
 */

public class SparseCoordinateFormatMatrixTest {
  double[][]data = {{1,2,0,0},{3,0,4,0},{0,5,6,0},{0,0,7,0}};
  double[] expectedData = {1.0,2.0,3.0,4.0,5.0,6.0,7.0};
  int[] expectedX = {0,0,1,1,2,2,3};
  int[] expectedY = {0,1,0,2,1,2,2};

  // test constructors

  @Test
  public void testConstructorDoubleArrays() {
    new SparseCoordinateFormatMatrix(data);
  }

  @Test
  public void testConstructorDoubleMatrix2D() {
    DoubleMatrix2D tmp = new DoubleMatrix2D(data);
    new SparseCoordinateFormatMatrix(tmp);
  }


  // test methods

 @Test  // tests entry getter
 public void testGetEntry() {
   SparseCoordinateFormatMatrix tmp = new SparseCoordinateFormatMatrix(data);
   for (int i = 0; i < data.length; i++) {
     for (int j = 0; j < data[i].length; j++) {
     assertEquals(Double.doubleToLongBits(data[i][j]),Double.doubleToLongBits(tmp.getEntry(i,j)));
     }
   }
 }

 @Test //tests element getter
 public void testGetNumberOfElements() {
   SparseCoordinateFormatMatrix tmp = new SparseCoordinateFormatMatrix(data);
     assertEquals(16,tmp.getNumberOfElements());
 }

 @Test //test data getter
 public void testGetData(){
   SparseCoordinateFormatMatrix tmp = new SparseCoordinateFormatMatrix(data);
   Arrays.equals(tmp.getNonZeroEntries(),expectedData);
 }

 @Test //test getColumnCoordinates
 public void testGetColumnCoordinates(){
   SparseCoordinateFormatMatrix tmp = new SparseCoordinateFormatMatrix(data);
   Arrays.equals(tmp.getColumnCoordinates(),expectedY);
 }

 @Test //test getRowCoordinates
 public void testGetRowCoordinates(){
   SparseCoordinateFormatMatrix tmp = new SparseCoordinateFormatMatrix(data);
   Arrays.equals(tmp.getRowCoordinates(),expectedX);
 }

 @Test //test getNumberOfRows
 public void testGetNumberOfRows(){
   SparseCoordinateFormatMatrix tmp = new SparseCoordinateFormatMatrix(data);
   assertEquals(tmp.getNumberOfRows(),4);
 }

 @Test //test getNumberOfColumns
 public void testGetNumberOfColumns(){
   SparseCoordinateFormatMatrix tmp = new SparseCoordinateFormatMatrix(data);
   assertEquals(tmp.getNumberOfColumns(),4);
 }

 @Test //test toArray
 public void testToArray() {
   SparseCoordinateFormatMatrix tmp = new SparseCoordinateFormatMatrix(data);
   assertTrue(Arrays.deepEquals(data,tmp.toArray()));
 }

 @Test //test toFullMatrix
 public void testToFullMatrix() {
   SparseCoordinateFormatMatrix tmp = new SparseCoordinateFormatMatrix(data);
   DoubleMatrix2D N = new DoubleMatrix2D(data);
   assertTrue(N.equals(tmp.toFullMatrix()));
 }


 @Test //test full row getter
 public void testGetFullRow() {
   SparseCoordinateFormatMatrix tmp = new SparseCoordinateFormatMatrix(data);
   for(int i = 0; i < data.length; i++) {
     assertTrue(Arrays.equals(data[i],tmp.getFullRow(i)));
   }
 }

 @Test //test full column getter
 public void testGetFullColumn() {
   SparseCoordinateFormatMatrix tmp = new SparseCoordinateFormatMatrix(data);
   double[] col = new double[data.length];
   for(int i = 0; i < data[0].length; i++) {
     // assemble column
     for(int j = 0; j < data[0].length; j++) {
       col[j] = data[j][i];
     }
     assertTrue(Arrays.equals(col,tmp.getFullColumn(i)));
   }
 }

 @Test //test row element getter
 public void testGetRowElements() {
   double[][] compressed = {{1,2},{3,4},{5,6},{7}};
   SparseCoordinateFormatMatrix tmp = new SparseCoordinateFormatMatrix(data);
   for(int i = 0; i < data.length; i++) {
     assertTrue(Arrays.equals(compressed[i],tmp.getRowElements(i)));
   }
 }

 @Test //test column element getter
 public void testGetColumnElements() {
   double[][] compressed = {{1,3},{2,5},{4,6,7},{}};
   SparseCoordinateFormatMatrix tmp = new SparseCoordinateFormatMatrix(data);
   for(int i = 0; i < data[0].length; i++) {
     assertTrue(Arrays.equals(compressed[i],tmp.getColumnElements(i)));
   }
 }

 @Test //test getter for non zero element count
 public void testGetNumberOfNonZeroElements() {
   SparseCoordinateFormatMatrix tmp = new SparseCoordinateFormatMatrix(data);
   assertTrue(tmp.getNumberOfNonZeroElements() == 7);
 }


//test sanity of equals and hashcode
 @Test
 public void testEqualsAndHashCode() {
   SparseCoordinateFormatMatrix N;
   SparseCoordinateFormatMatrix M = new SparseCoordinateFormatMatrix(data);

   assertTrue(M.equals(M)); // test this = obj
   assertFalse(M.equals(null)); // test obj != null
   assertFalse(M.equals(M.getClass())); // test obj class
   double[][] differentDataValues = {{7,2,0,0},{3,0,4,0},{0,5,6,0},{0,0,7,0}};
   double[][] differentDataXOrder = {{0,1,2,0},{3,0,4,0},{0,5,6,0},{0,0,7,0}};
   // test different values
   N = new SparseCoordinateFormatMatrix(differentDataValues);
   assertFalse(M.equals(N));

   // test same values different XOrder
   N = new SparseCoordinateFormatMatrix(differentDataXOrder);
   assertFalse(M.equals(N));

   // test same values different YOrder
   double[][] differentNewDataYOrder1 = {{1,2},{0,0}};
   double[][] differentNewDataYOrder2 = {{0,0},{1,2}};
   SparseCoordinateFormatMatrix P1 = new SparseCoordinateFormatMatrix(differentNewDataYOrder1);
   SparseCoordinateFormatMatrix P2 = new SparseCoordinateFormatMatrix(differentNewDataYOrder2);
   assertFalse(P1.equals(P2));

   // test matrices that are identical mathematically are identical programatically.
   N = new SparseCoordinateFormatMatrix(data);
   assertTrue(M.equals(N));
   assertEquals(M.hashCode(), N.hashCode());
 }

}

