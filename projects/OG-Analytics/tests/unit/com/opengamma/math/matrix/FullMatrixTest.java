/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.matrix;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.apache.commons.lang.NotImplementedException;
import org.testng.annotations.Test;

/**
 *
 */
public class FullMatrixTest {
double _ragged[][]={{1,2,3,4},{5,6,7},{11,12,13,14,15}};
double _square[][]={{1,2,3},{4,5,6},{7,8,9}};
double _tallRectangle[][]={{1,2,3},{4,5,6},{7,8,9},{10,11,12}};
double _wideRectangle[][]={{1,2,3,4},{5,6,7,8},{9,10,11,12}};
double _withZeros[][]={{0,2,3,0},{0,0,7,8},{9,0,0,0}};


/**
 * Test constructors.
 */
@Test(expectedExceptions =  NotImplementedException.class)
public void testContructFromRaggedArray()
{
  new FullMatrix(_ragged);
}

@Test
public void testContructFromSquareArray()
{
  new FullMatrix(_square);
}

@Test
public void testContructFromTallRectangle()
{
  new FullMatrix(_tallRectangle);
}

@Test
public void testContructFromWideRectangle()
{
  new FullMatrix(_wideRectangle);
}

@Test
public void testContructFromDoubleMatrix2D()
{
  new FullMatrix(new DoubleMatrix2D(_square));
}

/**
 * Test methods
 */
@Test
public void testGetNumberOfElements() {
  FullMatrix tmp = new FullMatrix(_square);
  assertEquals(9,tmp.getNumberOfElements());
  }

@Test
public void testGetEntryDualIndex() {
  FullMatrix tmp = new FullMatrix(_square);
  for (int i = 0; i < _square.length; i++) {
    for (int j = 0; j < _square.length; j++) {
      assertEquals(_square[i][j],tmp.getEntry(i,j));
    }
  }
}

@Test
public void testGetFullRow() {
  FullMatrix tmp = new FullMatrix(_square);
  for (int i = 0; i < _square.length; i++) {
      assertTrue(Arrays.equals(_square[i],tmp.getFullRow(i)));
  }
}

@Test
public void testGetFullColumn() {
  FullMatrix tmp = new FullMatrix(_square);
  double[] colmangle = new double[_square.length];
  for (int i = 0; i < _square.length; i++) {
    for (int j = 0; j < _square.length; j++) {
      colmangle[j]=_square[j][i];
    }
    assertTrue(Arrays.equals(colmangle,tmp.getFullColumn(i)));
  }
}

@Test
public void testGetRowElements() {
  FullMatrix tmp = new FullMatrix(_square);
  for (int i = 0; i < _square.length; i++) {
      assertTrue(Arrays.equals(_square[i],tmp.getRowElements(i)));
  }
}

@Test
public void testGetColumnElements() {
  FullMatrix tmp = new FullMatrix(_square);
  double[] colmangle = new double[_square.length];
  for (int i = 0; i < _square.length; i++) {
    for (int j = 0; j < _square.length; j++) {
      colmangle[j]=_square[j][i];
    }
    assertTrue(Arrays.equals(colmangle,tmp.getColumnElements(i)));
  }
}

@Test
public void testGetNumberOfNonZeroElements() {
  FullMatrix tmp = new FullMatrix(_withZeros);
  assertEquals(5,tmp.getNumberOfNonZeroElements());
  tmp = new FullMatrix(_square);
  assertEquals(9,tmp.getNumberOfNonZeroElements());
}

@Test
public void testToArray() {
  FullMatrix tmp = new FullMatrix(_tallRectangle);
  assertTrue(Arrays.deepEquals(_tallRectangle,tmp.toArray()));
}



}
