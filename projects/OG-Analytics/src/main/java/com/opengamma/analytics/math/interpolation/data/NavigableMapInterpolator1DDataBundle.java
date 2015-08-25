/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation.data;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;

import com.opengamma.util.ArgumentChecker;

/**
 * An implementation of {@link Interpolator1DDataBundle} backed by a
 * {@link NavigableMap}.
 * 
 */
public class NavigableMapInterpolator1DDataBundle implements Interpolator1DDataBundle, Serializable {
  private final NavigableMap<Double, Double> _backingMap;

  public NavigableMapInterpolator1DDataBundle(final NavigableMap<Double, Double> backingMap) {
    ArgumentChecker.notNull(backingMap, "Backing map");
    ArgumentChecker.isTrue(backingMap.size() > 0, "Must have at least two data points.");
    _backingMap = backingMap;
  }

  @Override
  public Double getLowerBoundKey(final Double value) {
    if (value < firstKey()) {
      throw new IllegalArgumentException("Could not get lower bound key for " + value + ": lowest x-value is "
          + firstKey());
    }
    if (value > lastKey()) {
      throw new IllegalArgumentException("Could not get lower bound key for " + value + ": highest x-value is "
          + lastKey());
    }
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
    if (key < firstKey()) {
      throw new IllegalArgumentException("Could not get lower bound key for " + key + ": lowest x-value is "
          + firstKey());
    }
    if (key > lastKey()) {
      throw new IllegalArgumentException("Could not get lower bound key for " + key + ": highest x-value is "
          + lastKey());
    }
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
    final Double higherKey = higherKey(key);
    if (higherKey.equals(lastKey())) {
      return new InterpolationBoundedValues(getLowerBoundIndex(key), lastKey(), lastValue(), null, null);
    }
    return new InterpolationBoundedValues(getLowerBoundIndex(key), lowerBoundKey, get(lowerBoundKey), higherKey,
        higherValue(key));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_backingMap == null) ? 0 : _backingMap.hashCode());
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
    final NavigableMapInterpolator1DDataBundle other = (NavigableMapInterpolator1DDataBundle) obj;
    if (_backingMap == null) {
      if (other._backingMap != null) {
        return false;
      }
    } else if (!_backingMap.equals(other._backingMap)) {
      return false;
    }
    return true;
  }

  @Override
  public void setYValueAtIndex(final int index, final double y) {
    ArgumentChecker.notNegative(index, "index");
    if (index >= size()) {
      throw new IllegalArgumentException("Index was greater than number of data points");
    }
    int count = 0;
    for (final Map.Entry<Double, Double> entry : _backingMap.entrySet()) {
      if (count == index) {
        _backingMap.put(entry.getKey(), y);
      }
      count++;
    }
  }
}
