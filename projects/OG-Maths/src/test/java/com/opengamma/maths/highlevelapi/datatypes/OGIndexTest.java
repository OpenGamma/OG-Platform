/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.highlevelapi.datatypes;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.apache.commons.lang.NotImplementedException;
import org.testng.annotations.Test;

import com.opengamma.maths.highlevelapi.datatypes.primitive.OGIndexType;
/**
 *
 */
public class OGIndexTest {

  int _ragged[][]={{1,2,3,4},{5,6,7},{11,12,13,14,15}};
  int _square[][]={{1,2,3},{4,5,6},{7,8,9}};
  int _tallRectangle[][]={{1,2,3},{4,5,6},{7,8,9},{10,11,12}};
  int _wideRectangle[][]={{1,2,3,4},{5,6,7,8},{9,10,11,12}};
  int _withZeros[][]={{0,2,3,0},{0,0,7,8},{9,0,0,0}};


  /**
   * Test constructors.
   */
  @Test(expectedExceptions =  NotImplementedException.class)
  public void testContructFromRaggedArray()
  {
    new OGIndexType(_ragged);
  }

  @Test
  public void testContructFromSquareArray()
  {
    new OGIndexType(_square);
  }

  @Test
  public void testContructFromTallRectangle()
  {
    new OGIndexType(_tallRectangle);
  }

  @Test
  public void testContructFromWideRectangle()
  {
    new OGIndexType(_wideRectangle);
  }


  /**
   * Test methods
   */
  @Test
  public void testGetNumberOfElements() {
    OGIndexType tmp = new OGIndexType(_square);
    assertTrue(tmp.getNumberOfElements()==9);
    }

  @Test
  public void testGetEntryDualIndex() {
    OGIndexType tmp = new OGIndexType(_square);
    for (int i = 0; i < _square.length; i++) {
      for (int j = 0; j < _square.length; j++) {
        assertTrue(tmp.getEntry(i,j) == _square[i][j]);
      }
    }
  }

  @Test
  public void testGetFullRow() {
    OGIndexType tmp = new OGIndexType(_square);
    for (int i = 0; i < _square.length; i++) {
        int[][] st = {_square[i]};
        OGIndexType obj = new OGIndexType(st);
        assertTrue(tmp.getFullRow(i).equals(obj));
    }
  }

  @Test
  public void testGetFullColumn() {
    OGIndexType tmp = new OGIndexType(_square);
    int[] colmangle = new int[_square.length];
    for (int i = 0; i < _square.length; i++) {
      for (int j = 0; j < _square.length; j++) {
        colmangle[j]=_square[j][i];
      }
      int[][] st = {colmangle};
      OGIndexType obj = new OGIndexType(st);
      assertTrue(tmp.getFullColumn(i).equals(obj));
    }
  }

  @Test
  public void testGetRowElements() {
    OGIndexType tmp = new OGIndexType(_square);
    for (int i = 0; i < _square.length; i++) {
      int[][] st = {_square[i]};
      OGIndexType obj = new OGIndexType(st);
      assertTrue(tmp.getRowElements(i).equals(obj));
    }
  }

  @Test
  public void testGetColumnElements() {
    OGIndexType tmp = new OGIndexType(_square);
    int[] colmangle = new int[_square.length];
    for (int i = 0; i < _square.length; i++) {
      for (int j = 0; j < _square.length; j++) {
        colmangle[j]=_square[j][i];
      }
      int[][] st = {colmangle};
      OGIndexType obj = new OGIndexType(st);
      assertTrue(tmp.getColumnElements(i).equals(obj));
    }
  }

  @Test
  public void testGetNumberOfNonZeroElements() {
    OGIndexType tmp = new OGIndexType(_withZeros);
    assertTrue(tmp.getNumberOfNonZeroElements()==5);
    tmp = new OGIndexType(_square);
    assertTrue(tmp.getNumberOfNonZeroElements()==9);
  }


  @Test
  public void testEqualsAndHashCode() {
    OGIndexType N;
    OGIndexType M = new OGIndexType(_square);
    assertTrue(M.equals(M)); // test this = obj
    assertFalse(M.equals(null)); // test obj != null
    assertFalse(M.equals(M.getClass())); // test obj class

    // false, cols too long
    N = new OGIndexType(_tallRectangle);
    assertFalse(M.equals(N));

    // false, rows too long
    N = new OGIndexType(_tallRectangle);
    assertFalse(M.equals(N));

    //
    int [][] _squareDiff = {{1,1,3},{4,5,6},{7,8,9}};
    N = new OGIndexType(_squareDiff);
    assertFalse(M.equals(N));

    // hash
    N = new OGIndexType(_square);
    assertTrue(M.equals(N));
    assertEquals(M.hashCode(), N.hashCode());
  }


  }
