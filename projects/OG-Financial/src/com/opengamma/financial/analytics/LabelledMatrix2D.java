/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * @param <S>
 * @param <T>
 */
public abstract class LabelledMatrix2D<S extends Comparable<S>, T extends Comparable<T>> {
  private final S[] _xKeys;
  private final Object[] _xLabels;
  private final T[] _yKeys;
  private final Object[] _yLabels;
  private final double[][] _values;

  public LabelledMatrix2D(final S[] xKeys, final T[] yKeys, final double[][] values) {
    Validate.notNull(xKeys, "x keys");
    final int m = xKeys.length;
    Validate.notNull(yKeys, "y keys");
    final int n = yKeys.length;
    Validate.notNull(values, "values");
    Validate.isTrue(values.length == n, "number of rows of data and y keys must be the same length");
    _xKeys = Arrays.copyOf(xKeys, m);
    _yKeys = Arrays.copyOf(yKeys, n);
    _xLabels = new Object[m];
    _yLabels = new Object[n];
    _values = new double[n][m];
    for (int i = 0; i < n; i++) {
      Validate.isTrue(values[i].length == m, "number of columns of data and x keys must be the same length");
      _yLabels[i] = yKeys[i].toString();
      for (int j = 0; j < m; j++) {
        if (i == 0) {
          _xLabels[j] = xKeys[j].toString();
        }
        _values[i][j] = values[i][j];
      }
    }
    sort(_xKeys, _xLabels, _yKeys, _yLabels, _values);
  }

  public LabelledMatrix2D(final S[] xKeys, final Object[] xLabels, final T[] yKeys, final Object[] yLabels, final double[][] values) {
    Validate.notNull(xKeys, "x keys");
    final int m = xKeys.length;
    Validate.notNull(xLabels, "x labels");
    Validate.isTrue(xLabels.length == m);
    Validate.notNull(yKeys, "y keys");
    final int n = yKeys.length;
    Validate.notNull(yLabels, "y labels");
    Validate.notNull(yLabels.length == n);
    Validate.notNull(values, "values");
    Validate.isTrue(values.length == n, "number of rows of data and y keys must be the same length");
    _xKeys = Arrays.copyOf(xKeys, m);
    _yKeys = Arrays.copyOf(yKeys, n);
    _xLabels = new Object[m];
    _yLabels = new Object[n];
    _values = new double[n][m];
    for (int i = 0; i < n; i++) {
      Validate.isTrue(values[i].length == m, "number of columns of data and x keys must be the same length");
      _yLabels[i] = yLabels[i];
      for (int j = 0; j < m; j++) {
        if (i == 0) {
          _xLabels[j] = xLabels[j];
        }
        _values[i][j] = values[i][j];
      }
    }
    sort(_xKeys, _xLabels, _yKeys, _yLabels, _values);
  }

  public S[] getXKeys() {
    return _xKeys;
  }

  public Object[] getXLabels() {
    return _xLabels;
  }

  public T[] getYKeys() {
    return _yKeys;
  }

  public Object[] getYLabels() {
    return _yLabels;
  }

  public double[][] getValues() {
    return _values;
  }

  public abstract <X> int compareX(S key1, S key2, X tolerance);

  public abstract <Y> int compareY(T key1, T key2, Y tolerance);

  public abstract LabelledMatrix2D<S, T> getMatrix(S[] xKeys, Object[] xLabels, T[] yKeys, Object[] yLabels, double[][] values);

  //TODO this needs rewriting
  public <X, Y> LabelledMatrix2D<S, T> add(final LabelledMatrix2D<S, T> other, final X xTolerance, final Y yTolerance) {
    Validate.notNull(other, "labelled matrix");
    final S[] otherXKeys = other.getXKeys();
    final Object[] otherXLabels = other.getXLabels();
    final T[] otherYKeys = other.getYKeys();
    final Object[] otherYLabels = other.getYLabels();
    final S[] originalXKeys = getXKeys();
    final Object[] originalXLabels = getXLabels();
    final T[] originalYKeys = getYKeys();
    final Object[] originalYLabels = getYLabels();
    final int m1 = originalXKeys.length;
    final int m2 = otherXKeys.length;
    final int n1 = originalYKeys.length;
    final int n2 = otherYKeys.length;
    final ObjectArrayList<S> newXKeysList = new ObjectArrayList<S>(originalXKeys);
    final ObjectArrayList<Object> newXLabelsList = new ObjectArrayList<Object>(originalXLabels);
    final ObjectArrayList<T> newYKeysList = new ObjectArrayList<T>(originalYKeys);
    final ObjectArrayList<Object> newYLabelsList = new ObjectArrayList<Object>(originalYLabels);
    for (int i = 0; i < m2; i++) {
      final int index = binarySearchInXWithTolerance(originalXKeys, otherXKeys[i], xTolerance);
      if (index < 0) {
        newXKeysList.add(otherXKeys[i]);
        newXLabelsList.add(otherXLabels[i]);
      }
    }
    for (int i = 0; i < n2; i++) {
      final int index = binarySearchInYWithTolerance(originalYKeys, otherYKeys[i], yTolerance);
      if (index < 0) {
        newYKeysList.add(otherYKeys[i]);
        newYLabelsList.add(otherYLabels[i]);
      }
    }
    final S[] newXKeys = newXKeysList.toArray(originalXKeys);
    final Object[] newXLabels = newXLabelsList.toArray();
    final T[] newYKeys = newYKeysList.toArray(originalYKeys);
    final Object[] newYLabels = newYLabelsList.toArray();
    final int totalX = newXKeys.length;
    final int totalY = newYKeys.length;
    final double[][] newValues = new double[totalY][totalX];
    for (int i = 0; i < n1; i++) {
      final int indexY = binarySearchInYWithTolerance(newYKeys, originalYKeys[i], yTolerance);
      for (int j = 0; j < m1; j++) {
        final int indexX = binarySearchInXWithTolerance(newXKeys, originalXKeys[j], xTolerance);
        newValues[indexY][indexX] = _values[i][j];
      }
    }
    for (int i = 0; i < n2; i++) {
      final int indexY = binarySearchInYWithTolerance(newYKeys, otherYKeys[i], yTolerance);
      for (int j = 0; j < m2; j++) {
        final int indexX = binarySearchInXWithTolerance(newXKeys, otherXKeys[j], xTolerance);
        newValues[indexY][indexX] += other._values[i][j];
      }
    }
    System.out.println(newXLabelsList);
    System.out.println(newYLabelsList);
    return getMatrix(newXKeys, newXLabels, newYKeys, newYLabels, newValues);
  }

  protected <X> int binarySearchInXWithTolerance(final S[] keys, final S key, final X tolerance) {
    int low = 0;
    int high = keys.length - 1;
    while (low <= high) {
      final int mid = (low + high) >>> 1;
      final S midVal = keys[mid];
      final int comparison = compareX(key, midVal, tolerance);
      if (comparison == 0) {
        return mid;
      } else if (comparison == 1) {
        low = mid + 1;
      } else {
        high = mid - 1;
      }
    }
    return -(low + 1);
  }

  protected <Y> int binarySearchInYWithTolerance(final T[] keys, final T key, final Y tolerance) {
    int low = 0;
    int high = keys.length - 1;
    while (low <= high) {
      final int mid = (low + high) >>> 1;
      final T midVal = keys[mid];
      final int comparison = compareY(key, midVal, tolerance);
      if (comparison == 0) {
        return mid;
      } else if (comparison == 1) {
        low = mid + 1;
      } else {
        high = mid - 1;
      }
    }
    return -(low + 1);
  }

  protected void sort(final S[] xKeys, final Object[] xLabels, final T[] yKeys, final Object[] yLabels, final double[][] values) {
    final int n = yKeys.length;
    final int m = xKeys.length;
    tripleArrayQuickSortInX(xKeys, xLabels, values, 0, m - 1, n);
    tripleArrayQuickSortInY(yKeys, yLabels, values, 0, n - 1, m);
  }

  private void tripleArrayQuickSortInX(final S[] keys, final Object[] labels, final double[][] values, final int left, final int right, final int n) {
    if (right > left) {
      final int pivot = (left + right) >> 1;
      final int pivotNewIndex = partitionInX(keys, labels, values, left, right, pivot, n);
      tripleArrayQuickSortInX(keys, labels, values, left, pivotNewIndex - 1, n);
      tripleArrayQuickSortInX(keys, labels, values, pivotNewIndex + 1, right, n);
    }
  }

  private void tripleArrayQuickSortInY(final T[] keys, final Object[] labels, final double[][] values, final int left, final int right, final int m) {
    if (right > left) {
      final int pivot = (left + right) >> 1;
      final int pivotNewIndex = partitionInY(keys, labels, values, left, right, pivot, m);
      tripleArrayQuickSortInY(keys, labels, values, left, pivotNewIndex - 1, m);
      tripleArrayQuickSortInY(keys, labels, values, pivotNewIndex + 1, right, m);
    }
  }

  private int partitionInX(final S[] keys, final Object[] labels, final double[][] values, final int left, final int right, final int pivot, final int n) {
    final S pivotValue = keys[pivot];
    swapInX(keys, labels, values, pivot, right, n);
    int storeIndex = left;
    for (int i = left; i < right; i++) {
      if (keys[i].compareTo(pivotValue) < 0) {
        swapInX(keys, labels, values, i, storeIndex, n);
        storeIndex++;
      }
    }
    swapInX(keys, labels, values, storeIndex, right, n);
    return storeIndex;
  }

  private int partitionInY(final T[] keys, final Object[] labels, final double[][] values, final int left, final int right, final int pivot, final int m) {
    final T pivotValue = keys[pivot];
    swapInY(keys, labels, values, pivot, right, m);
    int storeIndex = left;
    for (int i = left; i < right; i++) {
      if (keys[i].compareTo(pivotValue) < 0) {
        swapInY(keys, labels, values, i, storeIndex, m);
        storeIndex++;
      }
    }
    swapInY(keys, labels, values, storeIndex, right, m);
    return storeIndex;
  }

  private void swapInX(final S[] keys, final Object[] labels, final double[][] values, final int first, final int second, final int n) {
    final S x = keys[first];
    keys[first] = keys[second];
    keys[second] = x;
    final Object y = labels[first];
    labels[first] = labels[second];
    labels[second] = y;
    for (int i = 0; i < n; i++) {
      final double z = values[i][first];
      values[i][first] = values[i][second];
      values[i][second] = z;
    }
  }

  private void swapInY(final T[] keys, final Object[] labels, final double[][] values, final int first, final int second, final int m) {
    final T x = keys[first];
    keys[first] = keys[second];
    keys[second] = x;
    final Object y = labels[first];
    labels[first] = labels[second];
    labels[second] = y;
    for (int i = 0; i < m; i++) {
      final double z = values[first][i];
      values[first][i] = values[second][i];
      values[second][i] = z;
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_values);
    result = prime * result + Arrays.hashCode(_xKeys);
    result = prime * result + Arrays.hashCode(_xLabels);
    result = prime * result + Arrays.hashCode(_yKeys);
    result = prime * result + Arrays.hashCode(_yLabels);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof LabelledMatrix2D)) {
      return false;
    }
    @SuppressWarnings("rawtypes")
    final LabelledMatrix2D other = (LabelledMatrix2D) obj;
    final double[][] otherValues = other._values;
    for (int i = 0; i < _values.length; i++) {
      if (!Arrays.equals(_values[i], otherValues[i])) {
        return false;
      }
    }
    if (!Arrays.equals(_xKeys, other._xKeys)) {
      return false;
    }
    if (!Arrays.equals(_xLabels, other._xLabels)) {
      return false;
    }
    if (!Arrays.equals(_yKeys, other._yKeys)) {
      return false;
    }
    if (!Arrays.equals(_yLabels, other._yLabels)) {
      return false;
    }
    return true;
  }

}
