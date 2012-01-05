/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.utilities;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.maths.lowlevelapi.functions.utilities.Unique;

/**
 * Tests the unique {@link Unique} functions.
 */
public class UniqueTest {
  int[] _dataNullI = null;
  long[] _dataNullL = null;
  float[] _dataNullF = null;
  double[] _dataNullD = null;

  int[] _datatinyI = {19 };
  int[] _answertinyI = {19 };
  long[] _datatinyL = {19 };
  long[] _answertinyL = {19 };
  float[] _datatinyF = {19 };
  float[] _answertinyF = {19 };
  double[] _datatinyD = {19 };
  double[] _answertinyD = {19 };

  int[] _datasmallI = {19, 14, 13, 7, -9, 0, 1, 2, 3, 3, 4, 5, 5, 6 };
  int[] _answersmallI = {-9, 0, 1, 2, 3, 4, 5, 6, 7, 13, 14, 19 };
  long[] _datasmallL = {19, 14, 13, 7, -9, 0, 1, 2, 3, 3, 4, 5, 5, 6 };
  long[] _answersmallL = {-9, 0, 1, 2, 3, 4, 5, 6, 7, 13, 14, 19 };
  float[] _datasmallF = {19, 14, 13, 7, -9, 0, 1, 2, 3, 3, 4, 5, 5, 6 };
  float[] _answersmallF = {-9, 0, 1, 2, 3, 4, 5, 6, 7, 13, 14, 19 };
  double[] _datasmallD = {19, 14, 13, 7, -9, 0, 1, 2, 3, 3, 4, 5, 5, 6 };
  double[] _answersmallD = {-9, 0, 1, 2, 3, 4, 5, 6, 7, 13, 14, 19 };

  float[] _datasmallperturbed1F = {19, 14, 13, 7, -9, 0, 1, 2, 3 + 1e-9f, 3, 4, 5, 5 + 1e-8f, 6 };
  float[] _answersmallperturbed1F = {-9, 0, 1, 2, 3, 4, 5, 6, 7, 13, 14, 19 };
  float[] _datasmallperturbed2F = {19, 14, 13, 7, -9, 0, 1, 2, 3 + 1e-7f, 3, 4, 5, 5 + 1e-5f, 6 };
  float[] _answersmallperturbed2F = {-9, 0, 1, 2, 3, 4, 5, 5 + 1e-5f, 6, 7, 13, 14, 19 };

  double[] _datasmallperturbed1D = {19, 14, 13, 7, -9, 0, 1, 2, 3 + 1e-19d, 3, 4, 5, 5 + 1e-18d, 6 };
  double[] _answersmallperturbed1D = {-9, 0, 1, 2, 3, 4, 5, 6, 7, 13, 14, 19 };
  double[] _datasmallperturbed2D = {19, 14, 13, 7, -9, 0, 1, 2, 3 + 1e-17d, 3, 4, 5, 5 + 1e-14d, 6 };
  double[] _answersmallperturbed2D = {-9, 0, 1, 2, 3, 4, 5, 5 + 1e-14d, 6, 7, 13, 14, 19 };

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
  public void testUniqueNullFloat() {
    Unique.bitwise(_dataNullF);
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

  /* test longs */
  @Test
  public void testUniqueLongsTinyData() {
    long[] uniqued = Unique.bitwise(_datatinyL);
    assertTrue(Arrays.equals(_answertinyL, uniqued));
  }

  @Test
  public void testUniqueLongsSmallData() {
    long[] uniqued = Unique.bitwise(_datasmallL);
    assertTrue(Arrays.equals(_answersmallL, uniqued));
  }

  /* test floats */
  @Test
  public void testUniqueFloatsTinyData() {
    float[] uniqued = Unique.bitwise(_datatinyF);
    assertTrue(Arrays.equals(_answertinyF, uniqued));
  }

  @Test
  public void testUniqueFloatsSmallData() {
    float[] uniqued = Unique.bitwise(_datasmallF);
    assertTrue(Arrays.equals(_answersmallF, uniqued));
  }

  @Test
  public void testUniqueFloatsPerturbed1() {
    float[] uniqued = Unique.byTol(_datasmallperturbed1F);
    assertTrue(Arrays.equals(_answersmallperturbed1F, uniqued));
  }

  @Test
  public void testUniqueFloatsPerturbed2() {
    float[] uniqued = Unique.byTol(_datasmallperturbed2F);
    assertTrue(Arrays.equals(_answersmallperturbed2F, uniqued));
  }

  @Test
  public void testUniqueFloatsPerturbed3() {
    float[] uniqued = Unique.byTol(_datasmallperturbed1F, 1e-7f);
    assertTrue(Arrays.equals(_answersmallperturbed1F, uniqued));
  }

  @Test
  public void testUniqueFloatsPerturbed4() {
    float[] uniqued = Unique.byTol(_datasmallperturbed2F, 1e-7f);
    assertTrue(Arrays.equals(_answersmallperturbed2F, uniqued));
  }

  /* test doubles */
  @Test
  public void testUniqueDoublesTinyData() {
    double[] uniqued = Unique.bitwise(_datatinyD);
    assertTrue(Arrays.equals(_answertinyD, uniqued));
  }

  @Test
  public void testUniqueDoublesSmallData() {
    double[] uniqued = Unique.bitwise(_datasmallD);
    assertTrue(Arrays.equals(_answersmallD, uniqued));
  }

  @Test
  public void testUniqueDoublesPerturbed1() {
    double[] uniqued = Unique.byTol(_datasmallperturbed1D);
    assertTrue(Arrays.equals(_answersmallperturbed1D, uniqued));
  }

  @Test
  public void testUniqueDoublesPerturbed2() {
    double[] uniqued = Unique.byTol(_datasmallperturbed2D);
    assertTrue(Arrays.equals(_answersmallperturbed2D, uniqued));
  }

  @Test
  public void testUniqueDoublesPerturbed3() {
    double[] uniqued = Unique.byTol(_datasmallperturbed1D, 1e-15d);
    assertTrue(Arrays.equals(_answersmallperturbed1D, uniqued));
  }

  @Test
  public void testUniqueDoublesPerturbed4() {
    double[] uniqued = Unique.byTol(_datasmallperturbed2D, 1e-15d);
    assertTrue(Arrays.equals(_answersmallperturbed2D, uniqued));
  }
}
