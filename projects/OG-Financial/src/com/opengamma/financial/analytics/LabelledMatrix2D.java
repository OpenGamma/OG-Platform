/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.analytics.QuickSorter.ArrayQuickSorter;
import com.opengamma.math.ParallelArrayBinarySort;

/**
 * @param <S>
 * @param <T>
 */
public abstract class LabelledMatrix2D<S extends Comparable<S>, T extends Comparable<T>> {
  private final S[] _xKeys;
  private final Object[] _xLabels;
  private final String _xTitle;
  private final T[] _yKeys;
  private final Object[] _yLabels;
  private final String _yTitle;
  private final double[][] _values;
  private final String _valuesTitle;

  public LabelledMatrix2D(final S[] xKeys, final T[] yKeys, final double[][] values) {
    this(xKeys, LabelledMatrix1D.toString(xKeys), yKeys, LabelledMatrix1D.toString(yKeys), values);
  }
  
  public LabelledMatrix2D(final S[] xKeys, final Object[] xLabels, final T[] yKeys, final Object[] yLabels, final double[][] values) {
    this(xKeys, xLabels, null, yKeys, yLabels, null, values, null);
  }

  public LabelledMatrix2D(final S[] xKeys, final Object[] xLabels, final String xTitle, final T[] yKeys,
      final Object[] yLabels, final String yTitle, final double[][] values, final String valuesTitle) {
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
    _xTitle = xTitle;
    _yTitle = yTitle;
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
    _valuesTitle = valuesTitle;
    quickSortX();
    quickSortY();
  }

  public S[] getXKeys() {
    return _xKeys;
  }

  public Object[] getXLabels() {
    return _xLabels;
  }
  
  public String getXTitle() {
    return _xTitle;
  }

  public T[] getYKeys() {
    return _yKeys;
  }

  public Object[] getYLabels() {
    return _yLabels;
  }
  
  public String getYTitle() {
    return _yTitle;
  }

  public double[][] getValues() {
    return _values;
  }
  
  public String getValuesTitle() {
    return _valuesTitle;
  }

  public abstract <X> int compareX(S key1, S key2, X tolerance);

  public abstract <Y> int compareY(T key1, T key2, Y tolerance);

  public abstract LabelledMatrix2D<S, T> getMatrix(S[] xKeys, Object[] xLabels, String xTitle, T[] yKeys, Object[] yLabels, String yTitle, double[][] values, String valuesTitle);
  
  public abstract LabelledMatrix2D<S, T> getMatrix(S[] xKeys, Object[] xLabels, T[] yKeys, Object[] yLabels, double[][] values);

  //TODO this needs rewriting
  //TODO this ignores labels - using the original labels first and only using the labels from other when a new row / column is added
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
    ParallelArrayBinarySort.parallelBinarySort(newXKeys, newXLabels);
    ParallelArrayBinarySort.parallelBinarySort(newYKeys, newYLabels);
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
    return getMatrix(newXKeys, newXLabels, getXTitle(), newYKeys, newYLabels, getYTitle(), newValues, getValuesTitle());
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
      } else if (comparison > 0) {
        low = mid + 1;
      } else {
        high = mid - 1;
      }
    }
    return -(low + 1);
  }

  private void quickSortX() {
    (new ArrayQuickSorter<S>(_xKeys) {

      @Override
      protected int compare(final S first, final S second) {
        return first.compareTo(second);
      }

      @Override
      protected void swap(final int first, final int second) {
        super.swap(first, second);
        swap(_xLabels, first, second);
        final int y = _yKeys.length;
        for (int iy = 0; iy < y; iy++) {
          swap(_values[iy], first, second);
        }
      }

    }).sort();
  }

  private void quickSortY() {
    (new ArrayQuickSorter<T>(_yKeys) {

      @Override
      protected int compare(final T first, final T second) {
        return first.compareTo(second);
      }

      @Override
      protected void swap(final int first, final int second) {
        super.swap(first, second);
        swap(_yLabels, first, second);
        swap(_values, first, second);
      }

    }).sort();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_values);
    result = prime * result + ((_valuesTitle == null) ? 0 : _valuesTitle.hashCode());
    result = prime * result + Arrays.hashCode(_xKeys);
    result = prime * result + Arrays.hashCode(_xLabels);
    result = prime * result + ((_xTitle == null) ? 0 : _xTitle.hashCode());
    result = prime * result + Arrays.hashCode(_yKeys);
    result = prime * result + Arrays.hashCode(_yLabels);
    result = prime * result + ((_yTitle == null) ? 0 : _yTitle.hashCode());
    return result;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof LabelledMatrix2D)) {
      return false;
    }
    final LabelledMatrix2D other = (LabelledMatrix2D) obj;
    final double[][] otherValues = other._values;
    for (int i = 0; i < _values.length; i++) {
      if (!Arrays.equals(_values[i], otherValues[i])) {
        return false;
      }
    }
    if (_valuesTitle == null) {
      if (other._valuesTitle != null) {
        return false;
      }
    } else if (!_valuesTitle.equals(other._valuesTitle)) {
      return false;
    }
    if (!Arrays.equals(_xKeys, other._xKeys)) {
      return false;
    }
    if (!Arrays.equals(_xLabels, other._xLabels)) {
      return false;
    }
    if (_xTitle == null) {
      if (other._xTitle != null) {
        return false;
      }
    } else if (!_xTitle.equals(other._xTitle)) {
      return false;
    }
    if (!Arrays.equals(_yKeys, other._yKeys)) {
      return false;
    }
    if (!Arrays.equals(_yLabels, other._yLabels)) {
      return false;
    }
    if (_yTitle == null) {
      if (other._yTitle != null) {
        return false;
      }
    } else if (!_yTitle.equals(other._yTitle)) {
      return false;
    }
    return true;
  }

}
