/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.utilities;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.maths.lowlevelapi.functions.utilities.Max;

/**
 * Tests the Max() class for finding max value and index in vectors.
 */
public class MaxTest {
  int[] _intnull = null;
  long[] _longnull = null;
  float[] _floatnull = null;
  double[] _doublenull = null;

  int[] _intdata = {0, 9, 2, 3, 37, -17, 24, 37, 12, -3};
  long[] _longdata = {0, 9, 2, 3, 37, -17, 24, 37, 12, -3};
  float[] _floatdata = {0, 9, 2, 3, 37, -17, 24, 37, 12, -3};
  double[] _doubledata = {0, 9, 2, 3, 37, -17, 24, 37, 12, -3};

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void maxOfNullIntsTest() {
    Max.value(_intnull);
  }

  @Test
  public void maxOfIntsTest() {
    assertEquals(37,Max.value(_intdata));
  }

  @Test
  public void maxIdxOfIntsTest() {
    assertEquals(4,Max.index(_intdata));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void maxOfNullLongsTest() {
    Max.value(_longnull);
  }

  @Test
  public void maxOfLongsTest() {
    assertEquals(37,Max.value(_longdata));
  }

  @Test
  public void maxIdxOfLongsTest() {
    assertEquals(4,Max.index(_longdata));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void maxOfNullFloatsTest() {
    Max.value(_floatnull);
  }

  @Test
  public void maxOfFloatsTest() {
    assertEquals(Float.floatToIntBits(37),Float.floatToIntBits(Max.value(_floatdata)));
  }

  @Test
  public void maxIdxOfFloatsTest() {
    assertEquals(4,Max.index(_floatdata));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void maxOfNullDoublesTest() {
    Max.value(_doublenull);
  }

  @Test
  public void maxOfDoublesTest() {
    assertEquals(Double.doubleToLongBits(37),Double.doubleToLongBits(Max.value(_floatdata)));
  }

  @Test
  public void maxIdxOfDoublessTest() {
    assertEquals(4,Max.index(_doubledata));
  }

}
