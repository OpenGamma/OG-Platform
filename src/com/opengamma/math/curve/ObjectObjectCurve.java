/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.ParallelArrayBinarySort;
import com.opengamma.util.tuple.Pair;

/**
 * 
 * @param <T> The type of the x data
 * @param <U> The type of the y data
 */
public abstract class ObjectObjectCurve<T extends Comparable<T>, U> implements Curve<T, U> {
  private static final AtomicLong ATOMIC = new AtomicLong();
  private int _n;
  private final T[] _xData;
  private final U[] _yData;
  private final String _name;

  public ObjectObjectCurve(final T[] xData, final U[] yData) {
    this(xData, yData, false);
  }

  public ObjectObjectCurve(final Map<T, U> data) {
    this(data, false);
  }

  public ObjectObjectCurve(final Pair<T, U>[] data) {
    this(data, false);
  }

  public ObjectObjectCurve(final Set<Pair<T, U>> data) {
    this(data, false);
  }

  public ObjectObjectCurve(final T[] xData, final U[] yData, final String name) {
    this(xData, yData, false, name);
  }

  public ObjectObjectCurve(final Map<T, U> data, final String name) {
    this(data, false, name);
  }

  public ObjectObjectCurve(final Pair<T, U>[] data, final String name) {
    this(data, false, name);
  }

  public ObjectObjectCurve(final Set<Pair<T, U>> data, final String name) {
    this(data, false, name);
  }

  public ObjectObjectCurve(final T[] xData, final U[] yData, final boolean isSorted) {
    this(xData, yData, isSorted, Long.toString(ATOMIC.getAndIncrement()));
  }

  public ObjectObjectCurve(final Map<T, U> data, final boolean isSorted) {
    this(data, isSorted, Long.toString(ATOMIC.getAndIncrement()));
  }

  public ObjectObjectCurve(final Pair<T, U>[] data, final boolean isSorted) {
    this(data, isSorted, Long.toString(ATOMIC.getAndIncrement()));
  }

  public ObjectObjectCurve(final Set<Pair<T, U>> data, final boolean isSorted) {
    this(data, isSorted, Long.toString(ATOMIC.getAndIncrement()));
  }

  public ObjectObjectCurve(final T[] xData, final U[] yData, final boolean isSorted, final String name) {
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    _n = xData.length;
    Validate.isTrue(_n == yData.length);
    _xData = Arrays.copyOf(xData, _n);
    _yData = Arrays.copyOf(yData, _n);
    _name = name;
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  @SuppressWarnings("unchecked")
  public ObjectObjectCurve(final Map<T, U> data, final boolean isSorted, final String name) {
    Validate.notNull(data, "data");
    Validate.noNullElements(data.keySet(), "x values");
    Validate.noNullElements(data.values(), "y values");
    _xData = (T[]) data.keySet().toArray();
    _yData = (U[]) data.values().toArray();
    _n = data.size();
    _name = name;
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  @SuppressWarnings("unchecked")
  public ObjectObjectCurve(final Pair<T, U>[] data, final boolean isSorted, final String name) {
    Validate.notNull(data, "data");
    _n = data.length;
    final List<T> xTemp = new ArrayList<T>();
    final List<U> yTemp = new ArrayList<U>();
    _name = name;
    for (int i = 0; i < _n; i++) {
      Validate.notNull(data[i], "element " + i + " of data");
      xTemp.add(data[i].getFirst());
      yTemp.add(data[i].getSecond());
    }
    _xData = (T[]) xTemp.toArray();
    _yData = (U[]) yTemp.toArray();
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  @SuppressWarnings("unchecked")
  public ObjectObjectCurve(final Set<Pair<T, U>> data, final boolean isSorted, final String name) {
    Validate.notNull(data, "data");
    _n = data.size();
    final List<T> xTemp = new ArrayList<T>();
    final List<U> yTemp = new ArrayList<U>();
    _name = name;
    final int i = 0;
    for (final Pair<T, U> entry : data) {
      Validate.notNull(entry, "element " + i + " of data");
      xTemp.add(entry.getFirst());
      yTemp.add(entry.getSecond());
    }
    _xData = (T[]) xTemp.toArray();
    _yData = (U[]) yTemp.toArray();
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  @Override
  public T[] getXData() {
    return _xData;
  }

  @Override
  public U[] getYData() {
    return _yData;
  }

  @Override
  public int size() {
    return _n;
  }

  public String getName() {
    return _name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_xData);
    result = prime * result + Arrays.hashCode(_yData);
    result = prime * result + _name.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ObjectObjectCurve<?, ?> other = (ObjectObjectCurve<?, ?>) obj;
    return Arrays.equals(_xData, other._xData) && Arrays.equals(_yData, other._yData) && ObjectUtils.equals(_name, other._name);
  }
}
