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
public class PackedMatrixTest {
  double _ragged[][]={{1,2,3,4},{5,6,7},{11,12,13,14,15}};
  double _banded[][]={{1,2,0,0,0},{3,4,5,0,0},{0,6,7,8,0},{0,0,9,10,11},{0,0,0,12,13}};
  double _bandedwithzerorow[][]={{1,2,0,0,0},{3,4,5,0,0},{0,0,0,0,0},{0,0,6,7,8},{0,0,0,9,10}};
  double _unwoundbanded[] = {1,2,3,4,5,6,7,8,9,10,11,12,13};
  double _unwoundbandedwithzerorow[] = {1,2,3,4,5,6,7,8,9,10};
  double _bandedBLAS[][] = {{0,1,2},{3,4,5},{6,7,8},{9,10,11},{12,13,0}};

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
public void testBandedWithZeroRowConstructor() {
  new PackedMatrix(_bandedwithzerorow);
}

@Test
public void testBandedBLASWithPaddedZerosConstructor() {
  new PackedMatrix(_bandedBLAS,true,5,5);
}

/**
 * Test methods
 */

@Test
public void testGetNumberOfElements() {
  PackedMatrix tmp = new PackedMatrix(_banded);
  assertEquals(tmp.getNumberOfElements(),_unwoundbanded.length);
}

@Test
public void testGetEntryDoubleIndex() {
  PackedMatrix tmp = new PackedMatrix(_banded);
  for(int i = 0; i < _banded.length; i++) {
    for(int j = 0; j < _banded[i].length; j++) {
      assertEquals(tmp.getEntry(i,j),_banded[i][j]);
    }
  }
}

@Test
public void testGetEntrySingleIndex() {
  PackedMatrix tmp = new PackedMatrix(_banded);
  for(int i = 0; i < _banded.length; i++) {
    for(int j = 0; j < _banded[i].length; j++) {
      assertEquals(tmp.getEntry(i*_banded[0].length + j),_banded[i][j]);
    }
  }
}

@Test
public void testGetFullRow() {
  PackedMatrix tmp = new PackedMatrix(_banded);
  for (int i = 0; i < _banded.length; i++) {
    assertTrue(Arrays.equals(_banded[i],tmp.getFullRow(i)));
  }
}

@Test
public void testGetRowElements() {
  PackedMatrix tmp = new PackedMatrix(_banded);
  double[][] answer = {{1,2},{3,4,5},{6,7,8},{9,10,11},{12,13}};
  for (int i = 0; i < _banded.length; i++) {
    assertTrue(Arrays.equals(answer[i],tmp.getRowElements(i)));
  }
}

@Test
public void testBandedBLASReturnMatrixValidity() {
  PackedMatrix tmp = new PackedMatrix(_bandedBLAS,true);
  for (int i = 0; i < _bandedBLAS.length; i++) {
    assertTrue(Arrays.equals(_bandedBLAS[i],tmp.getRowElements(i)));
  }
}

}