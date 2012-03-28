/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.utilities;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.maths.lowlevelapi.functions.utilities.Abs;

import static org.testng.AssertJUnit.assertTrue;

/**
 * Tests the Abs function
 */
public class AbsTest {

  int[] _intnull = null;
  long[] _longnull = null;
  float[] _floatnull = null;
  double[] _doublenull = null;

  int[] _intdata = {0, 9, 2, 3, -17, -17, 24, -17, 12, -3 };
  long[] _longdata = {0, 9, 2, 3, -17, -17, 24, -17, 12, -3 };
  float[] _floatdata = {0, 9, 2, 3, -17, -17, 24, -17, 12, -3 };
  double[] _doubledata = {0, 9, 2, 3, -17, -17, 24, -17, 12, -3 };

  int[] _intresult = {0, 9, 2, 3, 17, 17, 24, 17, 12, 3 };
  long[] _longresult = {0, 9, 2, 3, 17, 17, 24, 17, 12, 3 };
  float[] _floatresult = {0, 9, 2, 3, 17, 17, 24, 17, 12, 3 };
  double[] _doubleresult = {0, 9, 2, 3, 17, 17, 24, 17, 12, 3 };

  // test ints
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAbsStatelessNullInt() {
    Abs.stateless(_intnull);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAbsInplaceNullInt() {
    Abs.inPlace(_intnull);
  }

  @Test
  public void testAbsStatelessInt() {
    assertTrue(Arrays.equals(_intresult, Abs.stateless(_intdata)));
  }

  @Test
  public void testAbsinPlaceInt() {
    int[] cpy = Arrays.copyOf(_intdata, _intdata.length);
    Abs.inPlace(cpy);
    assertTrue(Arrays.equals(_intresult, cpy));
  }
  
  // test longs
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAbsStatelessNullLong() {
    Abs.stateless(_longnull);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAbsInplaceNullLong() {
    Abs.inPlace(_longnull);
  }

  @Test
  public void testAbsStatelessLong() {
    assertTrue(Arrays.equals(_longresult, Abs.stateless(_longdata)));
  }

  @Test
  public void testAbsinPlaceLong() {
    long[] cpy = Arrays.copyOf(_longdata, _longdata.length);
    Abs.inPlace(cpy);
    assertTrue(Arrays.equals(_longresult, cpy));
  }  
  
  // test floats
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAbsStatelessNullFloat() {
    Abs.stateless(_floatnull);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAbsInplaceNullFloat() {
    Abs.inPlace(_floatnull);
  }

  @Test
  public void testAbsStatelessFloat() {
    assertTrue(Arrays.equals(_floatresult, Abs.stateless(_floatdata)));
  }

  @Test
  public void testAbsinPlaceFloat() {
    float[] cpy = Arrays.copyOf(_floatdata, _floatdata.length);
    Abs.inPlace(cpy);
    assertTrue(Arrays.equals(_floatresult, cpy));
  }    
  
  // test doubles
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAbsStatelessNullDouble() {
    Abs.stateless(_doublenull);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAbsInplaceNullDouble() {
    Abs.inPlace(_doublenull);
  }

  @Test
  public void testAbsStatelessDouble() {
    assertTrue(Arrays.equals(_doubleresult, Abs.stateless(_doubledata)));
  }

  @Test
  public void testAbsinPlaceDouble() {
    double[] cpy = Arrays.copyOf(_doubledata, _doubledata.length);
    Abs.inPlace(cpy);
    assertTrue(Arrays.equals(_doubleresult, cpy));
  }    

}
