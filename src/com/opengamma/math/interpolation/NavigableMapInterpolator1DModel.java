/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;

import org.apache.commons.lang.Validate;

/**
 * An implementation of {@link Interpolator1DModel} backed by a
 * {@link NavigableMap}.
 * 
 */
public class NavigableMapInterpolator1DModel implements Interpolator1DModel {
  private final NavigableMap<Double, Double> _backingMap;

  public NavigableMapInterpolator1DModel(final NavigableMap<Double, Double> backingMap) {
    Validate.notNull(backingMap, "Backing map");
    _backingMap = backingMap;
  }

  @Override
  public Double getLowerBoundKey(final Double value) {
    return _backingMap.floorKey(value);
  }

  @Override
  public int getLowerBoundIndex(final Double value) {
    final Double lower = getLowerBoundKey(value);
    int i = 0;
    final Iterator<Double> iter = _backingMap.keySet().iterator();
    Double key = iter.next();
    while (!key.equals(lower)) {
      key = iter.next();
      i++;
    }
    return i;
  }

  @Override
  public Double get(final Double key) {
    return _backingMap.get(key);
  }

  @Override
  public Double lastKey() {
    return _backingMap.lastKey();
  }

  @Override
  public Double lastValue() {
    return _backingMap.lastEntry().getValue();
  }

  @Override
  public Double higherKey(final Double key) {
    return _backingMap.higherKey(key);
  }

  @Override
  public Double higherValue(final Double key) {
    final Map.Entry<Double, Double> entry = _backingMap.higherEntry(key);
    if (entry == null) {
      return null;
    }
    return entry.getValue();
  }

  @Override
  public int size() {
    return _backingMap.size();
  }

  @Override
  public double[] getKeys() {
    final double[] result = new double[size()];
    int i = 0;
    for (final Double d : _backingMap.keySet()) {
      result[i++] = d;
    }
    assert i == size();
    return result;
  }

  @Override
  public double[] getValues() {
    final double[] result = new double[size()];
    int i = 0;
    for (final Double d : _backingMap.values()) {
      result[i++] = d;
    }
    assert i == size();
    return result;
  }

  @Override
  public Double firstKey() {
    return _backingMap.firstKey();
  }

  @Override
  public Double firstValue() {
    return _backingMap.firstEntry().getValue();
  }

  @Override
  public boolean containsKey(final Double key) {
    return _backingMap.containsKey(key);
  }

  @Override
  public InterpolationBoundedValues getBoundedValues(final Double key) {
    final Double lowerBoundKey = getLowerBoundKey(key);
    if (lowerBoundKey == null) {
      throw new IllegalArgumentException(key + " was less than lowest value of data " + firstKey());
    }
    if (higherKey(key) == null) {
      throw new IllegalArgumentException(key + " was greater than highest value of data " + lastKey());
    }
    return new InterpolationBoundedValues(lowerBoundKey, get(lowerBoundKey), higherKey(key), higherValue(key));
  }

}
