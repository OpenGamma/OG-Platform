/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.utilities;

import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.maths.lowlevelapi.functions.utilities.Sort;

/**
 * Tests the sort routines
 */
public class SortTest {
  int[] sortedI = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9 };
  int[] unsortedI = new int[] {1, 3, 5, 7, 9, 2, 4, 6, 8 };
  int[] reverseSortedI = new int[] {9, 8, 7, 6, 5, 4, 3, 2, 1};
  long[] sortedL = new long[] {1, 2, 3, 4, 5, 6, 7, 8, 9 };
  long[] unsortedL = new long[] {1, 3, 5, 7, 9, 2, 4, 6, 8 };
  long[] reverseSortedL = new long[] {9, 8, 7, 6, 5, 4, 3, 2, 1};
  float[] sortedF = new float[] {1, 2, 3, 4, 5, 6, 7, 8, 9 };
  float[] unsortedF = new float[] {1, 3, 5, 7, 9, 2, 4, 6, 8 };
  float[] reverseSortedF = new float[] {9, 8, 7, 6, 5, 4, 3, 2, 1};
  double[] sortedD = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9 };
  double[] unsortedD = new double[] {1, 3, 5, 7, 9, 2, 4, 6, 8 };
  double[] reverseSortedD = new double[] {9, 8, 7, 6, 5, 4, 3, 2, 1};
  int[] correctIdx = new int[] {0, 5, 1, 6, 2, 7, 3, 8, 4};

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStatelessNullIntSort() {
    int[] tmp=null;
    Sort.stateless(tmp);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStatelessNullLongSort() {
    long[] tmp=null;
    Sort.stateless(tmp);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStatelessNullFloatSort() {
    float[] tmp=null;
    Sort.stateless(tmp);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStatelessNullDoubleSort() {
    double[] tmp=null;
    Sort.stateless(tmp);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStatelessNullIntSortInterfaced() {
    int[] tmp=null;
    Sort.stateless(tmp,Sort.direction.ascend);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStatelessNullLongSortInterfaced() {
    long[] tmp=null;
    Sort.stateless(tmp,Sort.direction.ascend);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStatelessNullFloatSortInterfaced() {
    float[] tmp=null;
    Sort.stateless(tmp,Sort.direction.ascend);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStatelessNullDoubleSortInterfaced() {
    double[] tmp=null;
    Sort.stateless(tmp,Sort.direction.ascend);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInplaceNullIntSort() {
    int[] tmp=null;
    Sort.valuesInplace(tmp);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInplaceNullLongSort() {
    long[] tmp=null;
    Sort.valuesInplace(tmp);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInplaceNullFloatSort() {
    float[] tmp=null;
    Sort.valuesInplace(tmp);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInplaceNullDoubleSort() {
    double[] tmp=null;
    Sort.valuesInplace(tmp);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInplaceNullIntSortInterfaced() {
    int[] tmp=null;
    Sort.valuesInplace(tmp,Sort.direction.ascend);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInplaceNullLongSortInterfaced() {
    long[] tmp=null;
    Sort.valuesInplace(tmp,Sort.direction.ascend);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInplaceNullFloatSortInterfaced() {
    float[] tmp=null;
    Sort.valuesInplace(tmp,Sort.direction.ascend);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testInplaceNullDoubleSortInterfaced() {
    double[] tmp=null;
    Sort.valuesInplace(tmp,Sort.direction.ascend);
  }

  @Test
  public void testStatelessIntSort() {
    assertTrue(Arrays.equals(sortedI,Sort.stateless(unsortedI)));
  }

  @Test
  public void testStatelessLongSort() {
    assertTrue(Arrays.equals(sortedL,Sort.stateless(unsortedL)));
  }

  @Test
  public void testStatelessFloatSort() {
    assertTrue(Arrays.equals(sortedF,Sort.stateless(unsortedF)));
  }

  @Test
  public void testStatelessDoubleSort() {
    assertTrue(Arrays.equals(sortedD,Sort.stateless(unsortedD)));
  }

  @Test
  public void testStatelessIntSortInterfacedASCEND() {
    assertTrue(Arrays.equals(sortedI,Sort.stateless(unsortedI,Sort.direction.ascend)));
  }

  @Test
  public void testStatelessLongSortInterfacedASCEND() {
    assertTrue(Arrays.equals(sortedL,Sort.stateless(unsortedL,Sort.direction.ascend)));
  }

  @Test
  public void testStatelessFloatSortInterfacedASCEND() {
    assertTrue(Arrays.equals(sortedF,Sort.stateless(unsortedF,Sort.direction.ascend)));
  }

  @Test
  public void testStatelessDoubleSortInterfacedASCEND() {
    assertTrue(Arrays.equals(sortedD,Sort.stateless(unsortedD,Sort.direction.ascend)));
  }

  @Test
  public void testStatelessIntSortInterfacedDESCEND() {
    assertTrue(Arrays.equals(reverseSortedI,Sort.stateless(unsortedI,Sort.direction.descend)));
  }

  @Test
  public void testStatelessLongSortInterfacedDESCEND() {
    assertTrue(Arrays.equals(reverseSortedL,Sort.stateless(unsortedL,Sort.direction.descend)));
  }

  @Test
  public void testStatelessFloatSortInterfacedDESCEND() {
    assertTrue(Arrays.equals(reverseSortedF,Sort.stateless(unsortedF,Sort.direction.descend)));
  }

  @Test
  public void testStatelessDoubleSortInterfacedDESCEND() {
    assertTrue(Arrays.equals(reverseSortedD,Sort.stateless(unsortedD,Sort.direction.descend)));
  }

  @Test
  public void testInPlaceIntSort() {
    int[] cpy = Arrays.copyOf(unsortedI, unsortedI.length);
    Sort.valuesInplace(cpy);
    assertTrue(Arrays.equals(sortedI,cpy));
  }

  @Test
  public void testInPlaceLongSort() {
    long[] cpy = Arrays.copyOf(unsortedL, unsortedL.length);
    Sort.valuesInplace(cpy);
    assertTrue(Arrays.equals(sortedL,cpy));
  }

  @Test
  public void testInPlaceFloatSort() {
    float[] cpy = Arrays.copyOf(unsortedF, unsortedF.length);
    Sort.valuesInplace(cpy);
    assertTrue(Arrays.equals(sortedF,cpy));
  }

  @Test
  public void testInPlaceDoubleSort() {
    double[] cpy = Arrays.copyOf(unsortedD, unsortedD.length);
    Sort.valuesInplace(cpy);
    assertTrue(Arrays.equals(sortedD,cpy));
  }

  @Test
  public void testInPlaceIntSortInterfacedASCEND() {
    int[] cpy = Arrays.copyOf(unsortedI, unsortedI.length);
    Sort.valuesInplace(cpy,Sort.direction.ascend);
    assertTrue(Arrays.equals(sortedI,cpy));
  }

  @Test
  public void testInPlaceLongSortInterfacedASCEND() {
    long[] cpy = Arrays.copyOf(unsortedL, unsortedL.length);
    Sort.valuesInplace(cpy,Sort.direction.ascend);
    assertTrue(Arrays.equals(sortedL,cpy));
  }

  @Test
  public void testInPlaceFloatSortInterfacedASCEND() {
    float[] cpy = Arrays.copyOf(unsortedF, unsortedF.length);
    Sort.valuesInplace(cpy,Sort.direction.ascend);
    assertTrue(Arrays.equals(sortedF,cpy));
  }

  @Test
  public void testInPlaceDoubleSortInterfacedASCEND() {
    double[] cpy = Arrays.copyOf(unsortedD, unsortedD.length);
    Sort.valuesInplace(cpy,Sort.direction.ascend);
    assertTrue(Arrays.equals(sortedD,cpy));
  }

  @Test
  public void testInPlaceIntSortInterfacedDESCEND() {
    int[] cpy = Arrays.copyOf(unsortedI, unsortedI.length);
    Sort.valuesInplace(cpy,Sort.direction.descend);
    assertTrue(Arrays.equals(reverseSortedI,cpy));
  }

  @Test
  public void testInPlaceLongSortInterfacedDESCEND() {
    long[] cpy = Arrays.copyOf(unsortedL, unsortedL.length);
    Sort.valuesInplace(cpy,Sort.direction.descend);
    assertTrue(Arrays.equals(reverseSortedL,cpy));
  }

  @Test
  public void testInPlaceFloatSortInterfacedDESCEND() {
    float[] cpy = Arrays.copyOf(unsortedF, unsortedF.length);
    Sort.valuesInplace(cpy,Sort.direction.descend);
    assertTrue(Arrays.equals(reverseSortedF,cpy));
  }

  @Test
  public void testInPlaceDoubleSortInterfacedDESCEND() {
    double[] cpy = Arrays.copyOf(unsortedD, unsortedD.length);
    Sort.valuesInplace(cpy,Sort.direction.descend);
    assertTrue(Arrays.equals(reverseSortedD,cpy));
  }

  @Test
  public void testgetIndexInts() {
    assertTrue(Arrays.equals(correctIdx,Sort.getIndex(unsortedI)));
  }

  @Test
  public void testgetIndexLongs() {
    assertTrue(Arrays.equals(correctIdx,Sort.getIndex(unsortedL)));
  }

  @Test
  public void testgetIndexFloats() {
    assertTrue(Arrays.equals(correctIdx,Sort.getIndex(unsortedF)));
  }

  @Test
  public void testgetIndexDoubles() {
    assertTrue(Arrays.equals(correctIdx,Sort.getIndex(unsortedD)));
  }

} //end class


