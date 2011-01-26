/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.util.CompareUtils;

/**
 * 
 */
//TODO inputs need to be sorted
public class DoubleLabelledMatrix1D extends LabelledMatrix1D<Double> {
  private static final double EPS = 1e-15;

  public DoubleLabelledMatrix1D(final Double[] keys, final double[] values) {
    super(keys, values);
  }

  public DoubleLabelledMatrix1D(final Double[] keys, final Object[] labels, final double[] values) {
    super(keys, labels, values);
  }

  @Override
  public LabelledMatrix1D<Double> addIgnoringLabel(final LabelledMatrix1D<Double> other) {
    return add(other, EPS, true);
  }

  public LabelledMatrix1D<Double> addIgnoringLabel(final LabelledMatrix1D<Double> other, final double tolerance) {
    return add(other, tolerance, true);
  }

  @Override
  public LabelledMatrix1D<Double> add(final LabelledMatrix1D<Double> other) {
    return add(other, EPS, false);
  }

  public LabelledMatrix1D<Double> add(final LabelledMatrix1D<Double> other, final double tolerance) {
    return add(other, tolerance, false);
  }

  private LabelledMatrix1D<Double> add(final LabelledMatrix1D<Double> other, final double tolerance, final boolean ignoreLabel) {
    Validate.notNull(other, "labelled matrix");
    final Double[] otherKeys = other.getKeys();
    final Object[] otherLabels = other.getLabels();
    final double[] otherValues = other.getValues();
    final Double[] originalKeys = getKeys();
    final Object[] originalLabels = getLabels();
    final double[] originalValues = getValues();
    final int m = originalKeys.length;
    final int n = otherKeys.length;
    int count = m + n;
    final Double[] newKeys = Arrays.copyOf(originalKeys, count);
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
    return new DoubleLabelledMatrix1D(Arrays.copyOf(newKeys, count), Arrays.copyOf(newLabels, count), Arrays.copyOf(newValues, count));
  }

  @Override
  public LabelledMatrix1D<Double> addIgnoringLabel(final Double key, final Object label, final double value) {
    return add(key, label, value, EPS, true);
  }

  public LabelledMatrix1D<Double> addIgnoringLabel(final Double key, final Object label, final double value, final double tolerance) {
    return add(key, label, value, tolerance, true);
  }

  @Override
  public LabelledMatrix1D<Double> add(final Double key, final Object label, final double value) {
    return add(key, label, value, EPS, false);
  }

  public LabelledMatrix1D<Double> add(final Double key, final Object label, final double value, final double tolerance) {
    return add(key, label, value, tolerance, false);
  }

  private LabelledMatrix1D<Double> add(final Double key, final Object label, final double value, final double tolerance, final boolean ignoreLabel) {
    Validate.notNull(key, "key");
    Validate.notNull(label, "label");
    final Double[] originalKeys = getKeys();
    final Object[] originalLabels = getLabels();
    final double[] originalValues = getValues();
    final int n = originalKeys.length;
    final int index = binarySearchWithTolerance(originalKeys, key, tolerance);
    if (index >= 0) {
      if (!ignoreLabel && !originalLabels[index].equals(label)) {
        throw new IllegalArgumentException("Have a value for " + key + " but the label of the value to add (" + label + ") did not match the original (" + originalLabels[index] + ")");
      }
      final Double[] newKeys = Arrays.copyOf(originalKeys, n);
      final Object[] newLabels = Arrays.copyOf(originalLabels, n);
      final double[] newValues = Arrays.copyOf(originalValues, n);
      newValues[index] += value;
      return new DoubleLabelledMatrix1D(newKeys, newLabels, newValues);
    }
    final Double[] newKeys = Arrays.copyOf(originalKeys, n + 1);
    final Object[] newLabels = Arrays.copyOf(originalLabels, n + 1);
    final double[] newValues = Arrays.copyOf(originalValues, n + 1);
    newKeys[n] = key;
    newLabels[n] = label;
    newValues[n] = value;
    return new DoubleLabelledMatrix1D(newKeys, newLabels, newValues);
  }

  private int binarySearchWithTolerance(final Double[] keys, final double key, final double tolerance) {
    int low = 0;
    int high = keys.length - 1;
    while (low <= high) {
      final int mid = (low + high) >>> 1;
      final double midVal = keys[mid];
      if (CompareUtils.closeEquals(key, midVal, tolerance)) {
        return mid;
      }
      if (midVal < key) {
        low = mid + 1;
      } else {
        high = mid - 1;
      }
    }
    return -(low + 1);
  }

}
