/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.analytics.QuickSorter.ArrayQuickSorter;

/**
 * @param <TKey>  the type of the keys
 * @param <TValue>  the type of the values
 * @param <TTolerance>  the type of the tolerance
 */
public abstract class LabelledObjectMatrix1D<TKey extends Comparable<? super TKey>, TValue, TTolerance> {

  private final String _labelsTitle;
  private final String _valuesTitle;
  private final TKey[] _keys;
  private final Object[] _labels;
  private final TValue[] _values;
  private final TTolerance _defaultTolerance;
  
  public LabelledObjectMatrix1D(final TKey[] keys, final TValue[] values, final TTolerance defaultTolerance) {
    this(keys, LabelledMatrixUtils.toString(keys), values, defaultTolerance);
  }
  
  public LabelledObjectMatrix1D(final TKey[] keys, final Object[] labels, final TValue[] values, final TTolerance defaultTolerance) {
    this(keys, labels, null, values, null, defaultTolerance);
  }
  
  public LabelledObjectMatrix1D(final TKey[] keys, final String labelsTitle, final TValue[] values, final String valuesTitle, final TTolerance defaultTolerance) {
    this(keys, LabelledMatrixUtils.toString(keys), labelsTitle, values, valuesTitle, defaultTolerance);
  }
  
  public LabelledObjectMatrix1D(TKey[] keys, Object[] labels, String labelsTitle, TValue[] values, String valuesTitle, TTolerance defaultTolerance) {
    Validate.notNull(keys, "labels");
    Validate.notNull(labels, "label names");
    Validate.notNull(values, "values");
    final int n = keys.length;
    Validate.isTrue(n == labels.length, "length of keys array must match length of label names array");
    Validate.isTrue(n == values.length, "length of keys array must match length of values array");
    _keys = Arrays.copyOf(keys, n);
    _labels = Arrays.copyOf(labels, n);
    _labelsTitle = labelsTitle;
    _values = Arrays.copyOf(values, n);
    _valuesTitle = valuesTitle;
    _defaultTolerance = defaultTolerance;
    quickSort();
  }
  
  public TKey[] getKeys() {
    return _keys;
  }

  public Object[] getLabels() {
    return _labels;
  }
  
  public String getLabelsTitle() {
    return _labelsTitle;
  }

  public TValue[] getValues() {
    return _values;
  }
  
  public String getValuesTitle() {
    return _valuesTitle;
  }

  public int size() {
    return _keys.length;
  }

  protected TTolerance getDefaultTolerance() {
    return _defaultTolerance;
  }

  /**
   * Compares two keys and indicates whether the first would be considered less than, equal to or greater than the
   * second.
   * 
   * @param key1  the first key to compare, not null
   * @param key2  the second key to compare, not null
   * @param tolerance  the tolerance for equality of the keys
   * @return the value 0 if {@code key1} is equal to {@code key2}; a value less than 0 if {@code key1} is less than
   *         {@code key2}; and a value greater than 0 if {@code key1} is greater than {@code key2}.
   */
  public abstract int compare(TKey key1, TKey key2, TTolerance tolerance);

  /**
   * Compares two keys using the default equality tolerance, and indicates whether the first would be considered less
   * than, equal to or greater than the second.
   * 
   * @param key1  the first key to compare, not null
   * @param key2  the second key to compare, not null
   * @return the value 0 if {@code key1} is equal to {@code key2}; a value less than 0 if {@code key1} is less than
   *         {@code key2}; and a value greater than 0 if {@code key1} is greater than {@code key2}.
   */
  public int compare(final TKey key1, final TKey key2) {
    return compare(key1, key2, getDefaultTolerance());
  }
  
  private void quickSort() {
    (new ArrayQuickSorter<TKey>(_keys) {

      @Override
      protected int compare(final TKey first, final TKey second) {
        return LabelledObjectMatrix1D.this.compare(first, second);
      }

      @Override
      protected void swap(final int first, final int second) {
        super.swap(first, second);
        swap(_labels, first, second);
        swap(_values, first, second);
      }

    }).sort();
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_keys);
    result = prime * result + Arrays.hashCode(_labels);
    result = prime * result + ((_labelsTitle == null) ? 0 : _labelsTitle.hashCode());
    result = prime * result + Arrays.hashCode(_values);
    result = prime * result + ((_valuesTitle == null) ? 0 : _valuesTitle.hashCode());
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
    final LabelledObjectMatrix1D<?, ?, ?> other = (LabelledObjectMatrix1D<?, ?, ?>) obj;
    if (!Arrays.equals(_keys, other._keys)) {
      return false;
    }
    if (!Arrays.equals(_labels, other._labels)) {
      return false;
    }
    if (_labelsTitle == null) {
      if (other._labelsTitle != null) {
        return false;
      }
    } else if (!_labelsTitle.equals(other._labelsTitle)) {
      return false;
    }
    if (!Arrays.equals(_values, other._values)) {
      return false;
    }
    if (_valuesTitle == null) {
      if (other._valuesTitle != null) {
        return false;
      }
    } else if (!_valuesTitle.equals(other._valuesTitle)) {
      return false;
    }
    return true;
  }
  
}
