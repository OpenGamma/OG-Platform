/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.utilities;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;

/**
 *
 */
public class UniqueTest {
  int[] _dataNullI = null;
  long[] _dataNullL = null;
  double[] _dataNullD = null;

  int[] _datatinyI = {19 };
  int[] _answertinyI = {19 };
  double[] _datatinyD = {19 };
  double[] _answertinyD = {19 };

  int[] _datasmallI = {19, 14, 13, 7, -9, 0, 1, 2, 3, 3, 4, 5, 5, 6 };
  int[] _answersmallI = {-9, 0, 1, 2, 3, 4, 5, 6, 7, 13, 14, 19 };
  double[] _datasmallD = {19, 14, 13, 7, -9, 0, 1, 2, 3, 3, 4, 5, 5, 6 };
  double[] _answersmallD = {-9, 0, 1, 2, 3, 4, 5, 6, 7, 13, 14, 19 };

  /* test nulls */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUniqueNullInt() {
    Unique.bitwise(_dataNullI);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUniqueNullLong() {
    Unique.bitwise(_dataNullL);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testUniqueNullDouble() {
    Unique.bitwise(_dataNullD);
  }

  /* test ints */
  @Test
  public void testUniqueIntsTinyData() {
    int[] uniqued = Unique.bitwise(_datatinyI);
    assertTrue(Arrays.equals(_answertinyI, uniqued));
  }

  @Test
  public void testUniqueIntsSmallData() {
    int[] uniqued = Unique.bitwise(_datasmallI);
    assertTrue(Arrays.equals(_answersmallI, uniqued));
  }

}
