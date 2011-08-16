/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;


/**
 *
 */
public class MatrixPrimitiveUtilsTest {
double _array[][]={{1,2,3,4,5},{6,7,8,9,10},{11,12,13,14,15}};
double _ragged[][]={{1,2,3,4},{5,6,7},{11,12,13,14,15}};
double _square[][]={{1,2,3},{4,5,6},{7,8,9}};
double _vectorwithzeros[]={0,1,0,2,3,0,4,0,5,0,0,6};

@Test
public void testIsRagged() {
AssertJUnit.assertTrue(MatrixPrimitiveUtils.isRagged(_ragged));
AssertJUnit.assertFalse(MatrixPrimitiveUtils.isRagged(_array));
}

@Test
public void testIsSquare() {
AssertJUnit.assertTrue(MatrixPrimitiveUtils.isSquare(_square));
AssertJUnit.assertFalse(MatrixPrimitiveUtils.isSquare(_array));
AssertJUnit.assertFalse(MatrixPrimitiveUtils.isSquare(_ragged));
}

@Test
public void testGetNumberOfElementsInArray() {
  AssertJUnit.assertEquals(15,MatrixPrimitiveUtils.getNumberOfElementsInArray(_array));
  AssertJUnit.assertEquals(12,MatrixPrimitiveUtils.getNumberOfElementsInArray(_ragged));
  AssertJUnit.assertEquals(9,MatrixPrimitiveUtils.getNumberOfElementsInArray(_square));
}

@Test
public void testNumberOfNonZeroElementsInVector() {
  AssertJUnit.assertEquals(6,MatrixPrimitiveUtils.numberOfNonZeroElementsInVector(_vectorwithzeros));
}

@Test
public void testArrayHasContiguousNonZeros() {
  double[] data1 = {0,1,1,1,0};
  double[] data2 = {0,1,0,0,0};
  double[] data3 = {1,0,0,0,0};
  double[] data4 = {1,0,0,0,1};
  double[] data5 = {0,0,1,1,1,0,0,1};
  assertTrue(MatrixPrimitiveUtils.arrayHasContiguousRowEntries(data1));
  assertTrue(MatrixPrimitiveUtils.arrayHasContiguousRowEntries(data2));
  assertTrue(MatrixPrimitiveUtils.arrayHasContiguousRowEntries(data3));
  assertFalse(MatrixPrimitiveUtils.arrayHasContiguousRowEntries(data4));
  assertFalse(MatrixPrimitiveUtils.arrayHasContiguousRowEntries(data5));
}

@Test
public void testIsUpperTriangular() {
  double[][] UT = {{1,2,3},{0,4,5},{0,0,6}};
  double[][] notUT = {{1,2,3},{0,4,5},{0,1,6}};
  assertTrue(MatrixPrimitiveUtils.isUpperTriangular(UT));
  assertFalse(MatrixPrimitiveUtils.isUpperTriangular(notUT));
}

@Test
public void testCheckIsUpperTriangular() {
  double[][] UT = {{1,2,3},{0,4,5},{0,0,6}};
  assertTrue(Arrays.deepEquals(UT,MatrixPrimitiveUtils.checkIsUpperTriangular(UT)));
}

@Test(expectedExceptions =  IllegalArgumentException.class)
public void testCheckIsUpperTriangularExceptions() {
  double[][] notUT = {{1,2,3},{0,4,5},{0,1,6}};
  MatrixPrimitiveUtils.checkIsUpperTriangular(notUT);
}

}
