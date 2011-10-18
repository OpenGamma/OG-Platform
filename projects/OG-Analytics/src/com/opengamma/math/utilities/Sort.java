/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.utilities;

import java.util.Arrays;

import com.opengamma.math.ParallelArrayBinarySort;

/**
 * Sorts primitives without having to go through {@link ParallelArrayBinarySort}
 */
public final class Sort {

  /**
   * enum direction is used to specify the direction of the sort.
   * default direction used is ascend
   */
  enum direction {
    ascend,
    decend
  }

  /**
   * Sorts values statelessly
   * @param v1 the values to sort (a native backed array)
   * @param <T> the type of data
   * @return tmp the sorted values
   */
  public static <T> T[] stateless(T[] v1) {
    T[] tmp = Arrays.copyOf(v1, v1.length);
    Arrays.sort(tmp);
    return tmp;
  }

  /********** INTs ***********/
  /**
   * returns sort index
   * @param v1 the values that are being sorted.
   * @return the index, which if applied, would give the values in sorted order.
   */
  public static int[] getIndex(int[] v1) {
    int[] tmp = new int[v1.length];
    int[] idx = new int[v1.length];
    for (int i = 0; i < idx.length; i++) {
      idx[i] = i;
    }
    ParallelArrayBinarySort.parallelBinarySort(tmp, idx);
    return idx;
  }

  /**
   * returns sort index
   * @param v1 the values that are being sorted.
   * @return the index, which if applied, would give the values in sorted order.
   */
  public static int[] getIndex(int[] v1) {
    int[] tmp = new int[v1.length];
    int[] idx = new int[v1.length];
    for (int i = 0; i < idx.length; i++) {
      idx[i] = i;
    }
    ParallelArrayBinarySort.parallelBinarySort(tmp, idx);
    return idx;
  }

  /********** LONGs ***********/
  /**
  *
  * @param v1 a
  * @return tmp
  */
  public long[] valuesStateless(long[] v1) {
    long[] tmp = new long[v1.length];
    return tmp;
  }

  /**
  *
  * @param v1 a
  * @return tmp
  */
  public float[] valuesStateless(float[] v1) {
    float[] tmp = new float[v1.length];
    return tmp;
  }

  /**
  *
  * @param v1 a
  * @return tmp
  */
  public double[] valuesStateless(double[] v1) {
    double[] tmp = new double[v1.length];
    return tmp;
  }

  /**
  *
  * @param v1 a
  */
  public void valuesInplace(int[] v1) {
    int[] tmp = new int[v1.length];
  }

  /**
  *
  * @param v1 a
  */
  public void valuesInplace(long[] v1) {
    long[] tmp = new long[v1.length];
  }

  /**
  *
  * @param v1 a
  */
  public void valuesInplace(float[] v1) {
    float[] tmp = new float[v1.length];
  }

  /**
  *
  * @param v1 a
  */
  public void valuesInplace(double[] v1) {
    double[] tmp = new double[v1.length];
  }

} // class end

