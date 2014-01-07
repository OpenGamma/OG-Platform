/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.analytics.QuickSorter.ArrayQuickSorter;
import com.opengamma.lambdava.functions.Function3;

/**
 *
 * @param <S> The type of the keys
 * @param <T> The type of the tolerance
 */
//TODO need to test for uniqueness of keys and labels
public abstract class LabelledMatrix1D<S extends Comparable<? super S>, T> {
  private final String _labelsTitle;
  private final String _valuesTitle;
  private final S[] _keys;
  private final Object[] _labels;
  private final double[] _values;
  private final T _defaultTolerance;

  public LabelledMatrix1D(final S[] keys, final double[] values, final T defaultTolerance) {
    this(keys, LabelledMatrixUtils.toString(keys), values, defaultTolerance);
  }

  public LabelledMatrix1D(final S[] keys, final Object[] labels, final double[] values, final T defaultTolerance) {
    this(keys, labels, null, values, null, defaultTolerance);
  }

  public LabelledMatrix1D(final S[] keys, final String labelsTitle, final double[] values, final String valuesTitle, final T defaultTolerance) {
    this(keys, LabelledMatrixUtils.toString(keys), labelsTitle, values, valuesTitle, defaultTolerance);
  }

  public LabelledMatrix1D(final S[] keys, final Object[] labels, final String labelsTitle, final double[] values, final String valuesTitle, final T defaultTolerance) {
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

  public S[] getKeys() {
    return _keys;
  }

  public Object[] getLabels() {
    return _labels;
  }

  public String getLabelsTitle() {
    return _labelsTitle;
  }

  public double[] getValues() {
    return _values;
  }

  public String getValuesTitle() {
    return _valuesTitle;
  }

  public int size() {
    return _keys.length;
  }

  /**
   * Adds a labelled matrix to this one and returns a new matrix.
   * <p>
   * Each key in the new matrix is checked to see if it is in the original; if so, the value for that key is added. If the key is not present,
   * the new key, label and value are attached to the end of the matrix. This method ignores the label - if there is a key already present but
   * the labels do not match, then the new label is the original. For example, if there is an entry (3, "3", 0.1) and an entry (3, "THREE", 0.5) in
   * the new matrix, the result will be (3, "3", 0.6)
   * @param other Another labelled matrix
   * @return The sum of the matrices
   */
  public LabelledMatrix1D<S, T> addIgnoringLabel(final LabelledMatrix1D<S, T> other) {
    return addIgnoringLabel(other, getDefaultTolerance());
  }

  /**
   * Adds a labelled matrix to this one and returns a new matrix.
   * <p>
   * Each key in the new matrix is checked to see if it is in the original; if so, the value for that key is added. If the key is not present,
   * the new key, label and value are attached to the end of the matrix. This method ignores the label - if there is a key already present but
   * the labels do not match, then the new label is the original. For example, if there is an entry (3, "3", 0.1) and an entry (3, "THREE", 0.5) in
   * the new matrix, the result will be (3, "3", 0.6).
   * @param other Another labelled matrix
   * @param tolerance The tolerance
   * @return The sum of the matrices
   */
  public LabelledMatrix1D<S, T> addIgnoringLabel(final LabelledMatrix1D<S, T> other, final T tolerance) {
    return add(other, tolerance, true);
  }

  /**
   * Adds a labelled matrix to this one and returns a new matrix.
   * <p>
   * Each key in the new matrix is checked to see if it is in the original; if so, the value for that key is added. If the key is not present,
   * the new key, label and value are attached to the end of the matrix. This method does not ignores the label - if there is a key already present but
   * the labels do not match, then an exception is thrown.
   * @param other Another labelled matrix, not null
   * @return The sum of the matrices
   */
  public LabelledMatrix1D<S, T> add(final LabelledMatrix1D<S, T> other) {
    return add(other, getDefaultTolerance());
  }

  /**
   * Adds a labelled matrix to this one and returns a new matrix.
   * <p>
   * Each key in the new matrix is checked to see if it is in the original; if so, the value for that key is added. If the key is not present,
   * the new key, label and value are attached to the end of the matrix. This method does not ignores the label - if there is a key already present but
   * the labels do not match, then an exception is thrown.
   * @param other Another labelled matrix, not null
   * @param tolerance The tolerance
   * @return The sum of the matrices
   */
  public LabelledMatrix1D<S, T> add(final LabelledMatrix1D<S, T> other, final T tolerance) {
    return add(other, tolerance, false);
  }

  /**
   * Adds a key, label and value to this matrix, returning a new matrix.
   * <p>
   * Each key in the new matrix is checked to see if it is in the original; if so, the value for that key is added. If the key is not present,
   * the new key, label and value are attached to the end of the matrix. This method ignores the label - if there is a key already present but
   * the labels do not match, then the new label is the original. For example, if there is an entry (3, "3", 0.1) and an entry (3, "THREE", 0.5) in
   * the new matrix, the result will be (3, "3", 0.6)
   * @param key The key to which a value is to be added
   * @param label The label for the key
   * @param value The value to add
   * @return The sum of the matrices
   */
  public LabelledMatrix1D<S, T> addIgnoringLabel(final S key, final Object label, final double value) {
    return addIgnoringLabel(key, label, value, getDefaultTolerance());
  }

  /**
   * Adds a key, label and value to this matrix, returning a new matrix.
   * <p>
   * Each key in the new matrix is checked to see if it is in the original; if so, the value for that key is added. If the key is not present,
   * the new key, label and value are attached to the end of the matrix. This method ignores the label - if there is a key already present but
   * the labels do not match, then the new label is the original. For example, if there is an entry (3, "3", 0.1) and an entry (3, "THREE", 0.5) in
   * the new matrix, the result will be (3, "3", 0.6)
   * @param key The key to which a value is to be added
   * @param label The label for the key
   * @param value The value to add
   * @param tolerance The tolerance
   * @return The sum of the matrices
   */
  public LabelledMatrix1D<S, T> addIgnoringLabel(final S key, final Object label, final double value, final T tolerance) {
    return add(key, label, value, tolerance, true);
  }

  /**
   * Adds a key, label and value to this matrix, returning a new matrix.
   * <p>
   * The key is checked to see if it is in the original; if so, the value for that key is added. If the key is not present,
   * the new key, label and value are attached to the end of the matrix. This method does not ignores the label - if there is a key already present but
   * the labels do not match, then an exception is thrown.
   * @param key The key to which a value is to be added
   * @param label The label for the key
   * @param value The value to add
   * @return The sum of the matrices
   */
  public LabelledMatrix1D<S, T> add(final S key, final Object label, final double value) {
    return add(key, label, value, getDefaultTolerance());
  }

  /**
   * Adds a key, label and value to this matrix, returning a new matrix.
   * <p>
   * The key is checked to see if it is in the original; if so, the value for that key is added. If the key is not present,
   * the new key, label and value are attached to the end of the matrix. This method does not ignores the label - if there is a key already present but
   * the labels do not match, then an exception is thrown.
   * @param key The key to which a value is to be added
   * @param label The label for the key
   * @param value The value to add
   * @param tolerance The tolerance
   * @return The sum of the matrices
   */
  public LabelledMatrix1D<S, T> add(final S key, final Object label, final double value, final T tolerance) {
    return add(key, label, value, tolerance, false);
  }

  protected LabelledMatrix1D<S, T> add(final LabelledMatrix1D<S, T> other, final T tolerance, final boolean ignoreLabel) {
    Validate.notNull(other, "labelled matrix");
    final S[] otherKeys = other.getKeys();
    final Object[] otherLabels = other.getLabels();
    final double[] otherValues = other.getValues();
    final S[] originalKeys = getKeys();
    final Object[] originalLabels = getLabels();
    final double[] originalValues = getValues();
    final int m = originalKeys.length;
    final int n = otherKeys.length;
    int count = m + n;
    final S[] newKeys = Arrays.copyOf(originalKeys, count);
    final Object[] newLabels = Arrays.copyOf(originalLabels, count);
    final double[] newValues = Arrays.copyOf(originalValues, count);
    for (int i = 0; i < n; i++) {
      final int index = binarySearchWithTolerance(originalKeys, otherKeys[i], tolerance);
      if (index >= 0) {
        if (!ignoreLabel && !originalLabels[index].equals(otherLabels[i])) {
          throw new IllegalArgumentException("Have a value for " + otherKeys[i] + " but the label of the value to add (" + otherLabels[i] + ") did not match the original (" + originalLabels[index]
              + ")");
        }
        count--;
        newValues[index] += otherValues[i];
      } else {
        final int j = i - n + count;
        newKeys[j] = otherKeys[i];
        newLabels[j] = otherLabels[i];
        newValues[j] = otherValues[i];
      }
    }
    return getMatrix(Arrays.copyOf(newKeys, count), Arrays.copyOf(newLabels, count), getLabelsTitle(), Arrays.copyOf(newValues, count), getValuesTitle());
  }

  protected LabelledMatrix1D<S, T> add(final S key, final Object label, final double value, final T tolerance, final boolean ignoreLabel) {
    Validate.notNull(key, "key");
    Validate.notNull(label, "label");
    final S[] originalKeys = getKeys();
    final Object[] originalLabels = getLabels();
    final double[] originalValues = getValues();
    final int n = originalKeys.length;
    final int index = binarySearchWithTolerance(originalKeys, key, tolerance);
    if (index >= 0) {
      if (!ignoreLabel && !originalLabels[index].equals(label)) {
        throw new IllegalArgumentException("Have a value for " + key + " but the label of the value to add (" + label + ") did not match the original (" + originalLabels[index] + ")");
      }
      final S[] newKeys = Arrays.copyOf(originalKeys, n);
      final Object[] newLabels = Arrays.copyOf(originalLabels, n);
      final double[] newValues = Arrays.copyOf(originalValues, n);
      newValues[index] += value;
      return getMatrix(newKeys, newLabels, getLabelsTitle(), newValues, getValuesTitle());
    }
    final S[] newKeys = Arrays.copyOf(originalKeys, n + 1);
    final Object[] newLabels = Arrays.copyOf(originalLabels, n + 1);
    final double[] newValues = Arrays.copyOf(originalValues, n + 1);
    newKeys[n] = key;
    newLabels[n] = label;
    newValues[n] = value;
    return getMatrix(newKeys, newLabels, newValues);
  }

  public T getDefaultTolerance() {
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
  public abstract int compare(S key1, S key2, T tolerance);

  /**
   * Compares two keys using the default equality tolerance, and indicates whether the first would be considered less
   * than, equal to or greater than the second.
   *
   * @param key1  the first key to compare, not null
   * @param key2  the second key to compare, not null
   * @return the value 0 if {@code key1} is equal to {@code key2}; a value less than 0 if {@code key1} is less than
   *         {@code key2}; and a value greater than 0 if {@code key1} is greater than {@code key2}.
   */
  public int compare(final S key1, final S key2) {
    return compare(key1, key2, getDefaultTolerance());
  }

  public abstract LabelledMatrix1D<S, T> getMatrix(S[] keys, Object[] labels, String labelsTitle, double[] values, String valuesTitle);

  public abstract LabelledMatrix1D<S, T> getMatrix(S[] keys, Object[] labels, double[] values);

  public abstract LabelledMatrix1D<S, T> getMatrix(S[] keys, double[] values);

  private void quickSort() {
    (new ArrayQuickSorter<S>(_keys) {

      @Override
      protected int compare(final S first, final S second) {
        return LabelledMatrix1D.this.compare(first, second);
      }

      @Override
      protected void swap(final int first, final int second) {
        super.swap(first, second);
        swap(_labels, first, second);
        swap(_values, first, second);
      }

    }).sort();
  }

  protected int binarySearchWithTolerance(final S[] keys, final S key, final T tolerance) {
    int low = 0;
    int high = keys.length - 1;
    while (low <= high) {
      final int mid = (low + high) >>> 1;
      final S midVal = keys[mid];
      final int comparison = compare(key, midVal, tolerance);
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
    final LabelledMatrix1D<?, ?> other = (LabelledMatrix1D<?, ?>) obj;
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


  public LabelledMatrix1D<S, T> mapValues(Function3<S, Double, Object, Double> mapper) {
    double[] values = new double[_values.length];
    for (int i = 0; i < _keys.length; i++) {
      S key = _keys[i];
      double value = _values[i];
      Object label = _labels[i];
      values[i] = mapper.execute(key, value, label);
    }
    return getMatrix(_keys, _labels, values);
  }

}
