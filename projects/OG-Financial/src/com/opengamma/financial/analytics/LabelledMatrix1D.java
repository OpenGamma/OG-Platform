/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * 
 * @param <S> The type of the keys
 * @param <T> The type of the tolerance
 */
//TODO need to test for uniqueness of keys and labels
public abstract class LabelledMatrix1D<S extends Comparable<S>, T> {
  private final S[] _keys;
  private final Object[] _labels;
  private final double[] _values;
  private final T _defaultTolerance;

  public LabelledMatrix1D(final S[] keys, final double[] values, final T defaultTolerance) {
    Validate.notNull(keys, "labels");
    Validate.notNull(values, "values");
    Validate.isTrue(keys.length > 0, "keys array must not be empty");
    final int n = keys.length;
    Validate.isTrue(n == values.length, "length of keys array must match length of values array");
    _keys = Arrays.copyOf(keys, n);
    _values = Arrays.copyOf(values, n);
    _labels = new Object[n];
    int i = 0;
    for (final S s : keys) {
      _labels[i++] = s;
    }
    sort(_keys, _labels, _values);
    _defaultTolerance = defaultTolerance;
  }

  public LabelledMatrix1D(final S[] keys, final Object[] labels, final double[] values, final T defaultTolerance) {
    Validate.notNull(keys, "labels");
    Validate.notNull(labels, "label names");
    Validate.notNull(values, "values");
    Validate.isTrue(keys.length > 0, "labels array must not be empty");
    final int n = keys.length;
    Validate.isTrue(n == labels.length, "length of keys array (" + n + ") must match length of label names array (" + labels.length + ")");
    Validate.isTrue(n == values.length, "length of keys array (" + n + ") must match length of values array (" + values.length + ")");
    _keys = Arrays.copyOf(keys, n);
    _labels = Arrays.copyOf(labels, n);
    _values = Arrays.copyOf(values, n);
    sort(_keys, _labels, _values);
    _defaultTolerance = defaultTolerance;
  }

  public S[] getKeys() {
    return _keys;
  }

  public Object[] getLabels() {
    return _labels;
  }

  public double[] getValues() {
    return _values;
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
    return getMatrix(Arrays.copyOf(newKeys, count), Arrays.copyOf(newLabels, count), Arrays.copyOf(newValues, count));
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
      return getMatrix(newKeys, newLabels, newValues);
    }
    final S[] newKeys = Arrays.copyOf(originalKeys, n + 1);
    final Object[] newLabels = Arrays.copyOf(originalLabels, n + 1);
    final double[] newValues = Arrays.copyOf(originalValues, n + 1);
    newKeys[n] = key;
    newLabels[n] = label;
    newValues[n] = value;
    return getMatrix(newKeys, newLabels, newValues);
  }

  protected T getDefaultTolerance() {
    return _defaultTolerance;
  }

  protected abstract int compare(S o1, S o2, T tolerance);

  protected abstract LabelledMatrix1D<S, T> getMatrix(S[] keys, Object[] labels, double[] values);

  protected abstract LabelledMatrix1D<S, T> getMatrix(S[] keys, double[] values);

  protected void sort(final S[] keys, final Object[] labels, final double[] values) {
    final int n = keys.length;
    tripleArrayQuickSort(keys, labels, values, 0, n - 1);
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
      } else if (comparison == 1) {
        low = mid + 1;
      } else {
        high = mid - 1;
      }
    }
    return -(low + 1);
  }

  private void tripleArrayQuickSort(final S[] keys, final Object[] labels, final double[] values, final int left, final int right) {
    if (right > left) {
      final int pivot = (left + right) >> 1;
      final int pivotNewIndex = partition(keys, labels, values, left, right, pivot);
      tripleArrayQuickSort(keys, labels, values, left, pivotNewIndex - 1);
      tripleArrayQuickSort(keys, labels, values, pivotNewIndex + 1, right);
    }
  }

  private int partition(final S[] keys, final Object[] labels, final double[] values, final int left, final int right, final int pivot) {
    final S pivotValue = keys[pivot];
    swap(keys, labels, values, pivot, right);
    int storeIndex = left;
    for (int i = left; i < right; i++) {
      if (keys[i].compareTo(pivotValue) < 0) {
        swap(keys, labels, values, i, storeIndex);
        storeIndex++;
      }
    }
    swap(keys, labels, values, storeIndex, right);
    return storeIndex;
  }

  private void swap(final S[] keys, final Object[] labels, final double[] values, final int first, final int second) {
    final S x = keys[first];
    keys[first] = keys[second];
    keys[second] = x;
    final Object y = labels[first];
    labels[first] = labels[second];
    labels[second] = y;
    final double z = values[first];
    values[first] = values[second];
    values[second] = z;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_keys);
    result = prime * result + Arrays.hashCode(_labels);
    result = prime * result + Arrays.hashCode(_values);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
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
    return Arrays.equals(_values, other._values);
  }
}
