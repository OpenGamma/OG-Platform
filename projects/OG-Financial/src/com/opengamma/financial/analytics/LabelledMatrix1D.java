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
 * @param <S> The type of the labels
 */
//TODO need to test for uniqueness of keys and labels
public abstract class LabelledMatrix1D<S extends Comparable<S>> {
  private final S[] _keys;
  private final Object[] _labels;
  private final double[] _values;

  public LabelledMatrix1D(final S[] keys, final double[] values) {
    Validate.notNull(keys, "labels");
    Validate.notNull(values, "values");
    Validate.isTrue(keys.length > 0, "keys array must not be empty");
    final int n = keys.length;
    Validate.isTrue(n == values.length, "length of keys array must match length of values array");
    _keys = Arrays.copyOf(keys, n);
    _values = Arrays.copyOf(values, n);
    sort(_keys, null, _values);
    _labels = keys;
  }

  public LabelledMatrix1D(final S[] keys, final Object[] labels, final double[] values) {
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
  public abstract LabelledMatrix1D<S> addIgnoringLabel(LabelledMatrix1D<S> other);

  /**
   * Adds a labelled matrix to this one and returns a new matrix.
   * <p>
   * Each key in the new matrix is checked to see if it is in the original; if so, the value for that key is added. If the key is not present,
   * the new key, label and value are attached to the end of the matrix. This method does not ignores the label - if there is a key already present but
   * the labels do not match, then an exception is thrown. 
   * @param other Another labelled matrix
   * @return The sum of the matrices
   */
  public abstract LabelledMatrix1D<S> add(LabelledMatrix1D<S> other);

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
  public abstract LabelledMatrix1D<S> addIgnoringLabel(S key, Object label, double value);

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
  public abstract LabelledMatrix1D<S> add(S key, Object label, double value);

  protected void sort(final S[] keys, final Object[] labels, final double[] values) {
    final int n = keys.length;
    tripleArrayQuickSort(keys, labels, values, 0, n - 1);
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
      if (keys[i].compareTo(pivotValue) != 1) {
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
    if (labels != null) {
      final Object y = labels[first];
      labels[first] = labels[second];
      labels[second] = y;
    }
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
    final LabelledMatrix1D<?> other = (LabelledMatrix1D<?>) obj;
    if (!Arrays.equals(_keys, other._keys)) {
      return false;
    }
    if (!Arrays.equals(_labels, other._labels)) {
      return false;
    }
    return Arrays.equals(_values, other._values);
  }
}
