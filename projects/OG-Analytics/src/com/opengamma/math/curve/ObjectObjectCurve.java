/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.ParallelArrayBinarySort;
import com.opengamma.util.tuple.Pair;

/**
 * 
 * @param <T> The type of the x data
 * @param <U> The type of the y data
 */
public abstract class ObjectObjectCurve<T extends Comparable<T>, U> extends Curve<T, U> {
  private final int _n;
  private final T[] _xData;
  private final U[] _yData;

  public ObjectObjectCurve(final T[] xData, final U[] yData, final boolean isSorted) {
    super();
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    _n = xData.length;
    Validate.isTrue(_n == yData.length);
    _xData = Arrays.copyOf(xData, _n);
    _yData = Arrays.copyOf(yData, _n);
    for (int i = 0; i < _n; i++) {
      Validate.notNull(xData[i], "element " + i + " of x data");
      Validate.notNull(yData[i], "element " + i + " of y data");
      _xData[i] = xData[i];
      _yData[i] = yData[i];
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  @SuppressWarnings("unchecked")
  public ObjectObjectCurve(final Map<T, U> data, final boolean isSorted) {
    super();
    Validate.notNull(data, "data");
    Validate.noNullElements(data.keySet(), "x values");
    Validate.noNullElements(data.values(), "y values");
    _n = data.size();
    final Map.Entry<T, U> firstEntry = data.entrySet().iterator().next();
    _xData = data.keySet().toArray((T[]) Array.newInstance(firstEntry.getKey().getClass(), 0));
    _yData = data.values().toArray((U[]) Array.newInstance(firstEntry.getValue().getClass(), 0));
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  @SuppressWarnings("unchecked")
  public ObjectObjectCurve(final Set<Pair<T, U>> data, final boolean isSorted) {
    super();
    Validate.notNull(data, "data");
    _n = data.size();
    final List<T> xTemp = new ArrayList<T>(_n);
    final List<U> yTemp = new ArrayList<U>(_n);
    for (final Pair<T, U> entry : data) {
      Validate.notNull(entry, "element of data");
      xTemp.add(entry.getFirst());
      yTemp.add(entry.getSecond());
    }
    final Pair<T, U> firstEntry = data.iterator().next();
    _xData = xTemp.toArray((T[]) Array.newInstance(firstEntry.getKey().getClass(), 0));
    _yData = yTemp.toArray((U[]) Array.newInstance(firstEntry.getValue().getClass(), 0));
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  @SuppressWarnings("unchecked")
  public ObjectObjectCurve(final List<T> xData, final List<U> yData, final boolean isSorted) {
    super();
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    _n = xData.size();
    Validate.isTrue(_n == yData.size());
    _xData = xData.toArray((T[]) Array.newInstance(xData.get(0).getClass(), 0));
    _yData = yData.toArray((U[]) Array.newInstance(yData.get(0).getClass(), 0));
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  public ObjectObjectCurve(final T[] xData, final U[] yData, final boolean isSorted, final String name) {
    super(name);
    Validate.notNull(xData, "x data");
    _n = xData.length;
    Validate.notNull(yData, "y data");
    Validate.isTrue(_n == yData.length);
    _xData = Arrays.copyOf(xData, _n);
    _yData = Arrays.copyOf(yData, _n);
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  @SuppressWarnings("unchecked")
  public ObjectObjectCurve(final Map<T, U> data, final boolean isSorted, final String name) {
    super(name);
    Validate.notNull(data, "data");
    Validate.noNullElements(data.keySet(), "x values");
    Validate.noNullElements(data.values(), "y values");
    _n = data.size();
    final Map.Entry<T, U> firstEntry = data.entrySet().iterator().next();
    _xData = data.keySet().toArray((T[]) Array.newInstance(firstEntry.getKey().getClass(), 0));
    _yData = data.values().toArray((U[]) Array.newInstance(firstEntry.getValue().getClass(), 0));
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }

  }

  @SuppressWarnings("unchecked")
  public ObjectObjectCurve(final Set<Pair<T, U>> data, final boolean isSorted, final String name) {
    super(name);
    Validate.notNull(data, "data");
    _n = data.size();
    final List<T> xTemp = new ArrayList<T>(_n);
    final List<U> yTemp = new ArrayList<U>(_n);
    final int i = 0;
    for (final Pair<T, U> entry : data) {
      Validate.notNull(entry, "element " + i + " of data");
      xTemp.add(entry.getFirst());
      yTemp.add(entry.getSecond());
    }
    final Pair<T, U> firstEntry = data.iterator().next();
    _xData = xTemp.toArray((T[]) Array.newInstance(firstEntry.getKey().getClass(), 0));
    _yData = yTemp.toArray((U[]) Array.newInstance(firstEntry.getValue().getClass(), 0));
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  @SuppressWarnings("unchecked")
  public ObjectObjectCurve(final List<T> xData, final List<U> yData, final boolean isSorted, final String name) {
    super(name);
    Validate.notNull(xData, "x data");
    Validate.notNull(yData, "y data");
    _n = xData.size();
    Validate.isTrue(_n == yData.size());
    _xData = xData.toArray((T[]) Array.newInstance(xData.get(0).getClass(), 0));
    _yData = yData.toArray((U[]) Array.newInstance(yData.get(0).getClass(), 0));
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_xData);
    result = prime * result + Arrays.hashCode(_yData);
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
    return ArrayUtils.isEquals(_xData, other._xData) && ArrayUtils.isEquals(_yData, other._yData);
  }

}
