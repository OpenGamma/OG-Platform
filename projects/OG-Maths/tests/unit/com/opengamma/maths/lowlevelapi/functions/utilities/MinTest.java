/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.utilities;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.maths.lowlevelapi.functions.utilities.Min;

/**
 * Tests the Min() class for finding Min value and index in vectors.
 */
public class MinTest {
  int[] _intnull = null;
  long[] _longnull = null;
  float[] _floatnull = null;
  double[] _doublenull = null;

  int[] _intdata = {0, 9, 2, 3, -17, -17, 24, -17, 12, -3};
  long[] _longdata = {0, 9, 2, 3, -17, -17, 24, -17, 12, -3};
  float[] _floatdata = {0, 9, 2, 3, -17, -17, 24, -17, 12, -3};
  double[] _doubledata = {0, 9, 2, 3, -17, -17, 24, -17, 12, -3};

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void MinOfNullIntsTest() {
    Min.value(_intnull);
  }

  @Test
  public void MinOfIntsTest() {
    assertEquals(-17,Min.value(_intdata));
  }

  @Test
  public void MinIdxOfIntsTest() {
    assertEquals(4,Min.index(_intdata));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void MinOfNullLongsTest() {
    Min.value(_longnull);
  }

  @Test
  public void MinOfLongsTest() {
    assertEquals(-17,Min.value(_longdata));
  }

  @Test
  public void MinIdxOfLongsTest() {
    assertEquals(4,Min.index(_longdata));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void MinOfNullFloatsTest() {
    Min.value(_floatnull);
  }

  @Test
  public void MinOfFloatsTest() {
    assertEquals(Float.floatToIntBits(-17),Float.floatToIntBits(Min.value(_floatdata)));
  }

  @Test
  public void MinIdxOfFloatsTest() {
    assertEquals(4,Min.index(_floatdata));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void MinOfNullDoublesTest() {
    Min.value(_doublenull);
  }

  @Test
  public void MinOfDoublesTest() {
    assertEquals(Double.doubleToLongBits(-17),Double.doubleToLongBits(Min.value(_floatdata)));
  }

  @Test
  public void MinIdxOfDoublessTest() {
    assertEquals(4,Min.index(_doubledata));
  }

}
