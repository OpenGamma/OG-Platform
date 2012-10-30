/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.lowlevelapi.functions.utilities;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.ParallelArrayBinarySort;

/**
 * Sorts primitives without having to go through {@link ParallelArrayBinarySort}
 * TODO: should probably write another load of overloaded sorts that do things backwards
 */
public final class Sort {

  /**
   * enum direction is used to specify the direction of the sort.
   * default direction used is ascend
   */
  public enum direction {
    /** enumerates the sort direction as having ascending values */
    ascend,
    /** enumerates the sort direction as having descending values */
    descend
  }

  /**
   * Sorts values statelessly in ascending order
   * @param v1 the values to sort (a native backed array)
   * @return tmp the sorted values
   */
  public static int[] stateless(int[] v1) {
    Validate.notNull(v1);
    int[] tmp = Arrays.copyOf(v1, v1.length);
    Arrays.sort(tmp);
    return tmp;
  }

  /**
   * Sorts values statelessly in ascending order
   * @param v1 the values to sort (a native backed array)
   * @return tmp the sorted values
   */
  public static long[] stateless(long[] v1) {
    Validate.notNull(v1);
    long[] tmp = Arrays.copyOf(v1, v1.length);
    Arrays.sort(tmp);
    return tmp;
  }

  /**
   * Sorts values statelessly in ascending order
   * @param v1 the values to sort (a native backed array)
   * @return tmp the sorted values
   */
  public static float[] stateless(float[] v1) {
    Validate.notNull(v1);
    float[] tmp = Arrays.copyOf(v1, v1.length);
    Arrays.sort(tmp);
    return tmp;
  }

  /**
   * Sorts values statelessly in ascending order
   * @param v1 the values to sort (a native backed array)
   * @return tmp the sorted values
   */
  public static double[] stateless(double[] v1) {
    Validate.notNull(v1);
    double[] tmp = Arrays.copyOf(v1, v1.length);
    Arrays.sort(tmp);
    return tmp;
  }

  /**
   * Sorts values statelessly in order given by enumeration
   * @param v1 the values to sort (a native backed array)
   * @param d the direction in which the sorted array should be returned (based on {@link direction})
   * @return tmp the sorted values
   */
  public static int[] stateless(int[] v1, direction d) {
    Validate.notNull(v1);
    int[] tmp = Arrays.copyOf(v1, v1.length);
    Arrays.sort(tmp);
    switch (d) {
      case ascend:
        break;
      case descend:
        Reverse.inPlace(tmp);
    }
    return tmp;
  }

  /**
   * Sorts values statelessly in order given by enumeration
   * @param v1 the values to sort (a native backed array)
   * @param d the direction in which the sorted array should be returned (based on {@link direction})
   * @return tmp the sorted values
   */
  public static long[] stateless(long[] v1, direction d) {
    Validate.notNull(v1);
    long[] tmp = Arrays.copyOf(v1, v1.length);
    Arrays.sort(tmp);
    switch (d) {
      case ascend:
        break;
      case descend:
        Reverse.inPlace(tmp);
    }
    return tmp;
  }

  /**
   * Sorts values statelessly in order given by enumeration
   * @param v1 the values to sort (a native backed array)
   * @param d the direction in which the sorted array should be returned (based on {@link direction})
   * @return tmp the sorted values
   */
  public static float[] stateless(float[] v1, direction d) {
    Validate.notNull(v1);
    float[] tmp = Arrays.copyOf(v1, v1.length);
    Arrays.sort(tmp);
    switch (d) {
      case ascend:
        break;
      case descend:
        Reverse.inPlace(tmp);
    }
    return tmp;
  }

  /**
   * Sorts values statelessly in order given by enumeration
   * @param v1 the values to sort (a native backed array)
   * @param d the direction in which the sorted array should be returned (based on {@link direction})
   * @return tmp the sorted values
   */
  public static double[] stateless(double[] v1, direction d) {
    Validate.notNull(v1);
    double[] tmp = Arrays.copyOf(v1, v1.length);
    Arrays.sort(tmp);
    switch (d) {
      case ascend:
        break;
      case descend:
        Reverse.inPlace(tmp);
    }
    return tmp;
  }


  /**
   * Sorts values in place in ascending order
   * @param v1 the values to sort (a native backed array)
   */
  public static void valuesInplace(int[] v1) {
    Validate.notNull(v1);
    Arrays.sort(v1);
  }

  /**
   * Sorts values in place in ascending order
   * @param v1 the values to sort (a native backed array)
   */
  public static void valuesInplace(long[] v1) {
    Validate.notNull(v1);
    Arrays.sort(v1);
  }

  /**
   * Sorts values in place in ascending order
   * @param v1 the values to sort (a native backed array)
   */
  public static void valuesInplace(float[] v1) {
    Validate.notNull(v1);
    Arrays.sort(v1);
  }

  /**
   * Sorts values in place in ascending order
   * @param v1 the values to sort (a native backed array)
   */
  public static void valuesInplace(double[] v1) {
    Validate.notNull(v1);
    Arrays.sort(v1);
  }


  /**
   * Sorts values in place in order given by enumeration
   * @param v1 the values to sort (a native backed array)
   * @param d the direction in which the sorted array should be returned (based on {@link direction})
   */
  public static void valuesInplace(int[] v1, direction d) {
    Validate.notNull(v1);
    Arrays.sort(v1);
    switch (d) {
      case ascend:
        return;
      case descend:
        Reverse.inPlace(v1);
        return;
    }
  }

  /**
   * Sorts values in place in order given by enumeration
   * @param v1 the values to sort (a native backed array)
   * @param d the direction in which the sorted array should be returned (based on {@link direction})
   */
  public static void valuesInplace(long[] v1, direction d) {
    Validate.notNull(v1);
    Arrays.sort(v1);
    switch (d) {
      case ascend:
        return;
      case descend:
        Reverse.inPlace(v1);
        return;
    }
  }

  /**
   * Sorts values in place in order given by enumeration
   * @param v1 the values to sort (a native backed array)
   * @param d the direction in which the sorted array should be returned (based on {@link direction})
   */
  public static void valuesInplace(float[] v1, direction d) {
    Validate.notNull(v1);
    Arrays.sort(v1);
    switch (d) {
      case ascend:
        return;
      case descend:
        Reverse.inPlace(v1);
        return;
    }
  }

  /**
   * Sorts values in place in order given by enumeration
   * @param v1 the values to sort (a native backed array)
   * @param d the direction in which the sorted array should be returned (based on {@link direction})
   */
  public static void valuesInplace(double[] v1, direction d) {
    Validate.notNull(v1);
    Arrays.sort(v1);
    switch (d) {
      case ascend:
        return;
      case descend:
        Reverse.inPlace(v1);
        return;
    }
  }

  /**
   * Returns sort index (permutation required to sort a vector into ascending order)
   * @param v1 the values that are being sorted.
   * @return the index, which if applied, would give the values in sorted order.
   */
  public static int[] getIndex(int[] v1) {
    int[] tmp = Arrays.copyOf(v1, v1.length);
    int[] idx = Range.fromToInts(0, v1.length - 1);
    ParallelArrayBinarySort.parallelBinarySort(tmp, idx);
    return idx;
  }

  /**
   * Returns sort index (permutation required to sort a vector into ascending order)
   * @param v1 the values that are being sorted.
   * @return the index, which if applied, would give the values in sorted order.
   */
  public static int[] getIndex(long[] v1) {
    long[] tmp = Arrays.copyOf(v1, v1.length);
    int[] idx = Range.fromToInts(0, v1.length - 1);
    ParallelArrayBinarySort.parallelBinarySort(tmp, idx);
    return idx;
  }

  /**
   * Returns sort index (permutation required to sort a vector into ascending order)
   * @param v1 the values that are being sorted.
   * @return the index, which if applied, would give the values in sorted order.
   */
  public static int[] getIndex(float[] v1) {
    float[] tmp = Arrays.copyOf(v1, v1.length);
    int[] idx = Range.fromToInts(0, v1.length - 1);
    ParallelArrayBinarySort.parallelBinarySort(tmp, idx);
    return idx;
  }

  /**
   * Returns sort index (permutation required to sort a vector into ascending order)
   * @param v1 the values that are being sorted.
   * @return the index, which if applied, would give the values in sorted order.
   */
  public static int[] getIndex(double[] v1) {
    double[] tmp = Arrays.copyOf(v1, v1.length);
    int[] idx = Range.fromToInts(0, v1.length - 1);
    ParallelArrayBinarySort.parallelBinarySort(tmp, idx);
    return idx;
  }

} // class end
