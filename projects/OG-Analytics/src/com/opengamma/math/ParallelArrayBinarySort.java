/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class ParallelArrayBinarySort {

  /**
   * Sort the content of keys and values simultaneously so that
   * both match the correct ordering. Alters the arrays in place
   * @param keys The keys 
   * @param values The values
   */
  public static void parallelBinarySort(final double[] keys, final double[] values) {
    Validate.notNull(keys, "x data");
    Validate.notNull(values, "y data");
    Validate.isTrue(keys.length == values.length);
    final int n = keys.length;
    dualArrayQuickSort(keys, values, 0, n - 1);
  }

  /**
   * Sort the content of keys and values simultaneously so that
   * both match the correct ordering. Alters the arrays in place
   * @param <T> The type of the keys
   * @param <U> The type of the values
   * @param keys The keys 
   * @param values The values
   */
  public static <T extends Comparable<T>, U> void parallelBinarySort(final T[] keys, final U[] values) {
    Validate.notNull(keys, "x data");
    Validate.notNull(values, "y data");
    Validate.isTrue(keys.length == values.length);
    final int n = keys.length;
    dualArrayQuickSort(keys, values, 0, n - 1);
  }

  /**
   * Sort the content of keys and values simultaneously so that
   * both match the correct ordering. Alters the arrays in place
   * @param <T> The type of the values
   * @param keys The keys 
   * @param values The values
   */
  public static <T> void parallelBinarySort(final double[] keys, final T[] values) {
    Validate.notNull(keys, "x data");
    Validate.notNull(values, "y data");
    Validate.isTrue(keys.length == values.length);
    final int n = keys.length;
    dualArrayQuickSort(keys, values, 0, n - 1);
  }

  private static void dualArrayQuickSort(final double[] keys, final double[] values, final int left, final int right) {
    if (right > left) {
      final int pivot = (left + right) >> 1;
      final int pivotNewIndex = partition(keys, values, left, right, pivot);
      dualArrayQuickSort(keys, values, left, pivotNewIndex - 1);
      dualArrayQuickSort(keys, values, pivotNewIndex + 1, right);
    }
  }

  private static <T extends Comparable<T>, U> void dualArrayQuickSort(final T[] keys, final U[] values, final int left, final int right) {
    if (right > left) {
      final int pivot = (left + right) >> 1;
      final int pivotNewIndex = partition(keys, values, left, right, pivot);
      dualArrayQuickSort(keys, values, left, pivotNewIndex - 1);
      dualArrayQuickSort(keys, values, pivotNewIndex + 1, right);
    }
  }

  private static <T> void dualArrayQuickSort(final double[] keys, final T[] values, final int left, final int right) {
    if (right > left) {
      final int pivot = (left + right) >> 1;
      final int pivotNewIndex = partition(keys, values, left, right, pivot);
      dualArrayQuickSort(keys, values, left, pivotNewIndex - 1);
      dualArrayQuickSort(keys, values, pivotNewIndex + 1, right);
    }
  }

  private static int partition(final double[] keys, final double[] values, final int left, final int right, final int pivot) {
    final double pivotValue = keys[pivot];
    swap(keys, values, pivot, right);
    int storeIndex = left;
    for (int i = left; i < right; i++) {
      if (keys[i] <= pivotValue) {
        swap(keys, values, i, storeIndex);
        storeIndex++;
      }
    }
    swap(keys, values, storeIndex, right);
    return storeIndex;
  }

  private static <T extends Comparable<T>, U> int partition(final T[] keys, final U[] values, final int left, final int right, final int pivot) {
    final T pivotValue = keys[pivot];
    swap(keys, values, pivot, right);
    int storeIndex = left;
    for (int i = left; i < right; i++) {
      if (keys[i].compareTo(pivotValue) != 1) {
        swap(keys, values, i, storeIndex);
        storeIndex++;
      }
    }
    swap(keys, values, storeIndex, right);
    return storeIndex;
  }

  private static <T> int partition(final double[] keys, final T[] values, final int left, final int right, final int pivot) {
    final double pivotValue = keys[pivot];
    swap(keys, values, pivot, right);
    int storeIndex = left;
    for (int i = left; i < right; i++) {
      if (keys[i] <= pivotValue) {
        swap(keys, values, i, storeIndex);
        storeIndex++;
      }
    }
    swap(keys, values, storeIndex, right);
    return storeIndex;
  }

  private static void swap(final double[] keys, final double[] values, final int first, final int second) {
    double t = keys[first];
    keys[first] = keys[second];
    keys[second] = t;
    t = values[first];
    values[first] = values[second];
    values[second] = t;
  }

  private static <T extends Comparable<T>, U> void swap(final T[] keys, final U[] values, final int first, final int second) {
    final T t = keys[first];
    keys[first] = keys[second];
    keys[second] = t;
    final U u = values[first];
    values[first] = values[second];
    values[second] = u;
  }

  private static <T> void swap(final double[] keys, final T[] values, final int first, final int second) {
    final double x = keys[first];
    keys[first] = keys[second];
    keys[second] = x;
    final T t = values[first];
    values[first] = values[second];
    values[second] = t;
  }
}
