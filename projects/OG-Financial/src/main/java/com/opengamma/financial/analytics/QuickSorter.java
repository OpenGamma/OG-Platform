/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import com.opengamma.util.ArgumentChecker;

/**
 * Abstract form of a quick sort algorithm.
 */
/* package */abstract class QuickSorter<T> {

  // TODO: This is most definately in the wrong place; there is at least one other QSort lying around
  // (ParallelArrayBinarySort).

  public abstract static class ArrayQuickSorter<T> extends QuickSorter<T> {

    private final T[] _arr;

    public ArrayQuickSorter(final T[] arr) {
      ArgumentChecker.notNull(arr, "arr");
      _arr = arr;
    }

    public void sort() {
      sort(0, _arr.length - 1);
    }

    @Override
    protected T getValue(final int index) {
      return _arr[index];
    }

    @Override
    protected void swap(final int first, final int second) {
      swap(_arr, first, second);
    }

  }

  protected final void sort(final int left, final int right) {
    if (right > left) {
      final int pivot = partition(left, right, (left + right) >> 1);
      sort(left, pivot - 1);
      sort(pivot + 1, right);
    }
  }
  
  protected static <T> void swap(final T[] arr, final int first, final int second) {
    final T tmp = arr[first];
    arr[first] = arr[second];
    arr[second] = tmp;
  }

  protected static void swap(final double[] arr, final int first, final int second) {
    final double tmp = arr[first];
    arr[first] = arr[second];
    arr[second] = tmp;
  }

  protected abstract T getValue(final int index);

  protected abstract void swap(final int first, final int second);

  protected abstract int compare(final T first, final T second);

  protected final int partition(final int left, final int right, final int pivot) {
    final T pivotValue = getValue(pivot);
    swap(pivot, right);
    int storeIndex = left;
    for (int i = left; i < right; i++) {
      if (compare(getValue(i), pivotValue) < 0) {
        swap(i, storeIndex);
        storeIndex++;
      }
    }
    swap(storeIndex, right);
    return storeIndex;
  }

}
