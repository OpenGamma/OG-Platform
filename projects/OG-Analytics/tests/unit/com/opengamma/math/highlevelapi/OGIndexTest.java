/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.highlevelapi;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.apache.commons.lang.NotImplementedException;
import org.testng.annotations.Test;
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
    new OGIndex(_ragged);
  }

  @Test
  public void testContructFromSquareArray()
  {
    new OGIndex(_square);
  }

  @Test
  public void testContructFromTallRectangle()
  {
    new OGIndex(_tallRectangle);
  }

  @Test
  public void testContructFromWideRectangle()
  {
    new OGIndex(_wideRectangle);
  }


  /**
   * Test methods
   */
  @Test
  public void testGetNumberOfElements() {
    OGIndex tmp = new OGIndex(_square);
    assertTrue(tmp.getNumberOfElements()==9);
    }

  @Test
  public void testGetEntryDualIndex() {
    OGIndex tmp = new OGIndex(_square);
    for (int i = 0; i < _square.length; i++) {
      for (int j = 0; j < _square.length; j++) {
        assertTrue(tmp.getEntry(i,j) == _square[i][j]);
      }
    }
  }

  @Test
  public void testGetFullRow() {
    OGIndex tmp = new OGIndex(_square);
    for (int i = 0; i < _square.length; i++) {
        int[][] st = {_square[i]};
        OGIndex obj = new OGIndex(st);
        assertTrue(tmp.getFullRow(i).equals(obj));
    }
  }

  @Test
  public void testGetFullColumn() {
    OGIndex tmp = new OGIndex(_square);
    int[] colmangle = new int[_square.length];
    for (int i = 0; i < _square.length; i++) {
      for (int j = 0; j < _square.length; j++) {
        colmangle[j]=_square[j][i];
      }
      int[][] st = {colmangle};
      OGIndex obj = new OGIndex(st);
      assertTrue(tmp.getFullColumn(i).equals(obj));
    }
  }

  @Test
  public void testGetRowElements() {
    OGIndex tmp = new OGIndex(_square);
    for (int i = 0; i < _square.length; i++) {
      int[][] st = {_square[i]};
      OGIndex obj = new OGIndex(st);
      assertTrue(tmp.getRowElements(i).equals(obj));
    }
  }

  @Test
  public void testGetColumnElements() {
    OGIndex tmp = new OGIndex(_square);
    int[] colmangle = new int[_square.length];
    for (int i = 0; i < _square.length; i++) {
      for (int j = 0; j < _square.length; j++) {
        colmangle[j]=_square[j][i];
      }
      int[][] st = {colmangle};
      OGIndex obj = new OGIndex(st);
      assertTrue(tmp.getColumnElements(i).equals(obj));
    }
  }

  @Test
  public void testGetNumberOfNonZeroElements() {
    OGIndex tmp = new OGIndex(_withZeros);
    assertTrue(tmp.getNumberOfNonZeroElements()==5);
    tmp = new OGIndex(_square);
    assertTrue(tmp.getNumberOfNonZeroElements()==9);
  }


  @Test
  public void testEqualsAndHashCode() {
    OGIndex N;
    OGIndex M = new OGIndex(_square);
    assertTrue(M.equals(M)); // test this = obj
    assertFalse(M.equals(null)); // test obj != null
    assertFalse(M.equals(M.getClass())); // test obj class

    // false, cols too long
    N = new OGIndex(_tallRectangle);
    assertFalse(M.equals(N));

    // false, rows too long
    N = new OGIndex(_tallRectangle);
    assertFalse(M.equals(N));

    //
    int [][] _squareDiff = {{1,1,3},{4,5,6},{7,8,9}};
    N = new OGIndex(_squareDiff);
    assertFalse(M.equals(N));

    // hash
    N = new OGIndex(_square);
    assertTrue(M.equals(N));
    assertEquals(M.hashCode(), N.hashCode());
  }


  }
