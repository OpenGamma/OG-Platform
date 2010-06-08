/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * An implementation of {@link Interpolator1DModel} which holds all data in two
 * parallel-sorted double arrays.
 */
public class ArrayInterpolator1DModel implements Interpolator1DModel {
  private final double[] _keys;
  private final double[] _values;
  
  public ArrayInterpolator1DModel(double[] keys, double[] values) {
    this(keys, values, false);
  }
  
  public ArrayInterpolator1DModel(double[] keys, double[] values, boolean inputsSorted) {
    Validate.notNull(keys, "Keys must not be null.");
    Validate.notNull(values, "Values must not be null.");
    Validate.isTrue((keys.length == values.length), "keys and values must be same length.");
    Validate.isTrue((keys.length > 0), "Must not have empty arrays.");
    _keys = Arrays.copyOf(keys, keys.length);
    _values = Arrays.copyOf(values, values.length);
    
    if (!inputsSorted) {
      parallelBinarySort();
    }
  }

  /**
   * Sort the content of _keys and _values simultaneously so that
   * both match the correct ordering.
   */
  private void parallelBinarySort() {
    dualArrayQuickSort(_keys, _values, 0, _keys.length - 1);
  }
  
  private static void dualArrayQuickSort(double[] keys, double[] values, int left, int right) {
    if (right > left) {
      int pivot = keys.length / 2;
      int pivotNewIndex = partition(keys, values, left, right, pivot);
      dualArrayQuickSort(keys, values, left, pivotNewIndex - 1);
      dualArrayQuickSort(keys, values, pivotNewIndex + 1, right);
    }
  }

  private static int partition(double[] keys, double[] values, int left, int right, int pivot) {
    double pivotValue = keys[pivot];
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

  private static void swap(double[] keys, double[] values, int first, int second) {
    double t = keys[first];
    keys[first] = keys[second];
    keys[second] = t;
    
    t = values[first];
    values[first] = values[second];
    values[second] = t;
  }

  @Override
  public boolean containsKey(Double key) {
    if (key == null) {
      return false;
    }
    return Arrays.binarySearch(_keys, key) >= 0;
  }

  @Override
  public Double firstKey() {
    return _keys[0];
  }

  @Override
  public Double firstValue() {
    return _values[0];
  }

  @Override
  public Double get(Double key) {
    int index = Arrays.binarySearch(_keys, key);
    if (index < 0) {
      return null;
    }
    return _values[index];
  }

  @Override
  public InterpolationBoundedValues getBoundedValues(Double key) {
    int index = getLowerBoundIndex(key);
    if (index < 0) {
      return new InterpolationBoundedValues(null, null, _keys[0], _values[0]);
    }
    if (index >= (_keys.length - 1)) {
      return new InterpolationBoundedValues(_keys[_keys.length - 1], _values[_values.length - 1], null, null);
    }
    return new InterpolationBoundedValues(_keys[index], _values[index], _keys[index + 1], _values[index + 1]);
  }

  @Override
  public double[] getKeys() {
    return _keys;
  }

  @Override
  public int getLowerBoundIndex(Double value) {
    int index = Arrays.binarySearch(_keys, value);
    if (index >= 0) {
      // Fast break out if it's an exact match.
      return index;
    }
    if (index < 0) {
      index = -(index + 1);
      index--;
    }
    return index;
  }

  @Override
  public Double getLowerBoundKey(Double value) {
    int index = Arrays.binarySearch(_keys, value);
    if (index >= 0) {
      // Fast break out if it's an exact match.
      return _keys[index];
    }
    if (index < 0) {
      index = -(index + 1);
      index--;
    }
    if (index < 0) {
      return null;
    }
    return _keys[index];
  }

  @Override
  public double[] getValues() {
    return _values;
  }

  @Override
  public Double higherKey(Double key) {
    int index = getHigherIndex(key);
    if (index >= _keys.length) {
      return null;
    }
    return _keys[index];
  }

  @Override
  public Double higherValue(Double key) {
    int index = getHigherIndex(key);
    if (index >= _keys.length) {
      return null;
    }
    return _values[index];
  }
  
  protected int getHigherIndex(Double key) {
    int index = Arrays.binarySearch(_keys, key);
    if (index >= 0) {
      // Fast break out if it's an exact match.
      return index + 1;
    }
    if (index < 0) {
      index = -(index + 1);
      index--;
    }
    return index + 1;
  }

  @Override
  public Double lastKey() {
    return _keys[_keys.length - 1];
  }

  @Override
  public Double lastValue() {
    return _values[_values.length - 1];
  }

  @Override
  public int size() {
    return _keys.length;
  }

}
