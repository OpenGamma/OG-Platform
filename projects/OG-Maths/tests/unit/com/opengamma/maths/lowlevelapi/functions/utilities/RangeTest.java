/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.utilities;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.maths.lowlevelapi.functions.utilities.Range;

/**
 * Tests the range function
 */
public class RangeTest {
  int sSmall = 1;
  int sMed = 4;
  int sLarge = 7;
  int eSmall = 4;
  int eMed = 10;
  int eLarge = 20;
  int jSmall = 1;
  int jMed = 3;
  int jLarge = 5;

  int[] answerISmall = {1, 2, 3, 4 };
  int[] answerIMed = {4, 5, 6, 7, 8, 9, 10 };
  int[] answerILarge = {7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };

  int[] answerISteppedSmall = {1, 2, 3, 4 };
  int[] answerISteppedMed = {4, 7, 10 };
  int[] answerISteppedLarge = {7, 12, 17 };

  int[] answerISteppedReverseSmall = {4, 3, 2, 1 };
  int[] answerISteppedReverseMed = {10, 7, 4 };
  int[] answerISteppedReverseLarge = {20, 15, 10 };

  double[] answerDSmall = {1, 2, 3, 4 };
  double[] answerDMed = {4, 5, 6, 7, 8, 9, 10 };
  double[] answerDLarge = {7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };

  double[] answerDSteppedSmall = {1, 2, 3, 4 };
  double[] answerDSteppedMed = {4, 7, 10 };
  double[] answerDSteppedLarge = {7, 12, 17 };

  double[] answerDSteppedReverseSmall = {4, 3, 2, 1 };
  double[] answerDSteppedReverseMed = {10, 7, 4 };
  double[] answerDSteppedReverseLarge = {20, 15, 10 };


  @Test
  public void testRangePositiveInts() {
    assertTrue(Arrays.equals(answerISmall, Range.fromToInts(sSmall, eSmall)));
    assertTrue(Arrays.equals(answerIMed, Range.fromToInts(sMed, eMed)));
    assertTrue(Arrays.equals(answerILarge, Range.fromToInts(sLarge, eLarge)));
  }

  @Test
  public void testRangePositiveSteppedInts() {
    assertTrue(Arrays.equals(answerISteppedSmall, Range.fromToInStepInts(sSmall, eSmall, jSmall)));
    assertTrue(Arrays.equals(answerISteppedMed, Range.fromToInStepInts(sMed, eMed, jMed)));
    assertTrue(Arrays.equals(answerISteppedLarge, Range.fromToInStepInts(sLarge, eLarge, jLarge)));
  }

  @Test
  public void testRangeNegativeSteppedInts() {
    assertTrue(Arrays.equals(answerISteppedReverseSmall, Range.fromToInStepInts(eSmall, sSmall, -jSmall)));
    assertTrue(Arrays.equals(answerISteppedReverseMed, Range.fromToInStepInts(eMed, sMed, -jMed)));
    assertTrue(Arrays.equals(answerISteppedReverseLarge, Range.fromToInStepInts(eLarge, sLarge, -jLarge)));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRangePostiveBadInputInts() {
    Range.fromToInts(eSmall, sSmall);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRangeNegativeBadInputv1Ints() {
    Range.fromToInStepInts(eSmall, sSmall, 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRangeNegativeBadInputv2Ints() {
    Range.fromToInStepInts(sSmall, eSmall, -1);
  }

  @Test
  public void testRangePositiveDoubles() {
    assertTrue(Arrays.equals(answerDSmall, Range.fromToDoubles(sSmall, eSmall)));
    assertTrue(Arrays.equals(answerDMed, Range.fromToDoubles(sMed, eMed)));
    assertTrue(Arrays.equals(answerDLarge, Range.fromToDoubles(sLarge, eLarge)));
  }

  @Test
  public void testRangePositiveSteppedDoubles() {
    assertTrue(Arrays.equals(answerDSteppedSmall, Range.fromToInStepDoubles(sSmall, eSmall, jSmall)));
    assertTrue(Arrays.equals(answerDSteppedMed, Range.fromToInStepDoubles(sMed, eMed, jMed)));
    assertTrue(Arrays.equals(answerDSteppedLarge, Range.fromToInStepDoubles(sLarge, eLarge, jLarge)));
  }

  @Test
  public void testRangeNegativeSteppedDoubles() {
    assertTrue(Arrays.equals(answerDSteppedReverseSmall, Range.fromToInStepDoubles(eSmall, sSmall, -jSmall)));
    assertTrue(Arrays.equals(answerDSteppedReverseMed, Range.fromToInStepDoubles(eMed, sMed, -jMed)));
    assertTrue(Arrays.equals(answerDSteppedReverseLarge, Range.fromToInStepDoubles(eLarge, sLarge, -jLarge)));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRangePostiveBadInputDoubles() {
    Range.fromToInts(eSmall, sSmall);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRangeNegativeBadInputv1Doubles() {
    Range.fromToInStepInts(eSmall, sSmall, 1);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRangeNegativeBadInputv2Doubles() {
    Range.fromToInStepInts(sSmall, eSmall, -1);
  }

}
