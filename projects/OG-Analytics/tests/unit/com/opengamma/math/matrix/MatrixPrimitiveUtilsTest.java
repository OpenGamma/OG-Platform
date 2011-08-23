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


@Test
public void testIsLowerTriangular() {
  double[][] LT = {{1,0,0},{2,3,0},{4,5,6}};
  double[][] notLT = {{1,1,0},{2,3,0},{4,5,6}};
  assertTrue(MatrixPrimitiveUtils.isLowerTriangular(LT));
  assertFalse(MatrixPrimitiveUtils.isLowerTriangular(notLT));
}

@Test
public void testCheckIsLowerTriangular() {
  double[][] LT = {{1,0,0},{2,3,0},{4,5,6}};
  assertTrue(Arrays.deepEquals(LT,MatrixPrimitiveUtils.checkIsLowerTriangular(LT)));
}

@Test(expectedExceptions =  IllegalArgumentException.class)
public void testCheckIsLowerTriangularExceptions() {
  double[][] notLT = {{1,1,0},{2,3,0},{4,5,6}};
  MatrixPrimitiveUtils.checkIsLowerTriangular(notLT);
}


@Test
public void testCheckIsUpperHessenberg() {
  double[][] UH = {{1,2,3,4},{5,6,7,8},{0,9,10,11},{0,0,12,13}};
  assertTrue(Arrays.deepEquals(UH,MatrixPrimitiveUtils.checkIsUpperHessenberg(UH)));
}

@Test(expectedExceptions =  IllegalArgumentException.class)
public void testCheckIsUpperHessenbergExceptions() {
  double[][] notUH = {{1,2,3,4},{5,6,7,8},{1,9,10,11},{0,0,12,13}};
  MatrixPrimitiveUtils.checkIsUpperHessenberg(notUH);
}

@Test
public void testCheckIsLowerHessenberg() {
  double[][] LH = {{1,2,0,0},{3,4,5,0},{6,7,8,9},{10,11,12,13}};
  assertTrue(Arrays.deepEquals(LH,MatrixPrimitiveUtils.checkIsLowerHessenberg(LH)));
}

@Test(expectedExceptions =  IllegalArgumentException.class)
public void testCheckIsLowerHessenbergExceptions() {
  double[][] notLH = {{1,2,0,1},{3,4,5,0},{6,7,8,9},{10,11,12,13}};
  MatrixPrimitiveUtils.checkIsLowerHessenberg(notLH);
}


@Test
public void testIsTriDiag() {
  double[][] trid = {{1,2,0,0,0,0,0},{3,4,5,0,0,0,0},{0,6,7,8,0,0,0},{0,0,9,10,11,0,0},{0,0,0,12,13,14,0},{0,0,0,0,15,16,17},{0,0,0,0,0,18,19}};
  double[][] ntrid = {{1,2,1,0,0,0,0},{3,4,5,0,0,0,0},{0,6,7,8,0,0,0},{0,0,9,10,11,0,0},{0,0,0,12,13,14,0},{0,0,0,0,15,16,17},{0,0,0,0,0,18,19}};
  assertTrue(MatrixPrimitiveUtils.isTriDiag(trid));
  assertFalse(MatrixPrimitiveUtils.isTriDiag(ntrid));
}

@Test
public void testCheckIsTriDiag() {
  double[][] trid = {{1,2,0,0,0,0,0},{3,4,5,0,0,0,0},{0,6,7,8,0,0,0},{0,0,9,10,11,0,0},{0,0,0,12,13,14,0},{0,0,0,0,15,16,17},{0,0,0,0,0,18,19}};
  assertTrue(Arrays.deepEquals(trid, MatrixPrimitiveUtils.checkIsTriDiag(trid)));
}

@Test(expectedExceptions =  IllegalArgumentException.class)
public void testCheckIsTriDiagExceptions() {
  double[][] notTrid = {{1,2,1,0,0,0,0},{3,4,5,0,0,0,0},{0,6,7,8,0,0,0},{0,0,9,10,11,0,0},{0,0,0,12,13,14,0},{0,0,0,0,15,16,17},{0,0,0,0,0,18,19}};
  MatrixPrimitiveUtils.checkIsTriDiag(notTrid);
}



} // class end


