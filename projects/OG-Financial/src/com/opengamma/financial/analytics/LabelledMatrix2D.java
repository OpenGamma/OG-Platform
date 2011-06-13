/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

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

  public LabelledMatrix2D(S[] xKeys, T[] yKeys, double[][] values) {
    Validate.notNull(xKeys, "x keys");
    int m = xKeys.length;
    Validate.notNull(yKeys, "y keys");
    int n = yKeys.length;
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

  public LabelledMatrix2D(S[] xKeys, Object[] xLabels, T[] yKeys, Object[] yLabels, double[][] values) {
    Validate.notNull(xKeys, "x keys");
    int m = xKeys.length;
    Validate.notNull(xLabels, "x labels");
    Validate.isTrue(xLabels.length == m);
    Validate.notNull(yKeys, "y keys");
    int n = yKeys.length;
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

  protected void sort(final S[] xKeys, final Object[] xLabels, T[] yKeys, Object[] yLabels, final double[][] values) {
    int n = yKeys.length;
    final int m = xKeys.length;
    tripleArrayQuickSortInX(xKeys, xLabels, values, 0, m - 1, n);
    tripleArrayQuickSortInY(yKeys, yLabels, values, 0, n - 1, m);
  }

  private void tripleArrayQuickSortInX(final S[] keys, final Object[] labels, final double[][] values, final int left, final int right, int n) {
    if (right > left) {
      final int pivot = (left + right) >> 1;
      final int pivotNewIndex = partitionInX(keys, labels, values, left, right, pivot, n);
      tripleArrayQuickSortInX(keys, labels, values, left, pivotNewIndex - 1, n);
      tripleArrayQuickSortInX(keys, labels, values, pivotNewIndex + 1, right, n);
    }
  }

  private void tripleArrayQuickSortInY(final T[] keys, final Object[] labels, final double[][] values, final int left, final int right, int m) {
    if (right > left) {
      final int pivot = (left + right) >> 1;
      final int pivotNewIndex = partitionInY(keys, labels, values, left, right, pivot, m);
      tripleArrayQuickSortInY(keys, labels, values, left, pivotNewIndex - 1, m);
      tripleArrayQuickSortInY(keys, labels, values, pivotNewIndex + 1, right, m);
    }
  }

  private int partitionInX(final S[] keys, final Object[] labels, final double[][] values, final int left, final int right, final int pivot, int n) {
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

  private int partitionInY(final T[] keys, final Object[] labels, final double[][] values, final int left, final int right, final int pivot, int m) {
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

  private void swapInX(final S[] keys, final Object[] labels, final double[][] values, final int first, final int second, int n) {
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

  private void swapInY(final T[] keys, final Object[] labels, final double[][] values, final int first, final int second, int m) {
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
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    @SuppressWarnings("rawtypes")
    LabelledMatrix2D other = (LabelledMatrix2D) obj;
    double[][] otherValues = other._values;
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
