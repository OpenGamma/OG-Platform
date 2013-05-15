/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ParallelArrayBinarySort;
import com.opengamma.util.tuple.Pair;

/**
/**
 * Parent class for a family of curves that can have any time of data on the <i>x</i> and <i>y</i> axes, provided that the <i>x</i> data is {@link Comparable}.
 * It is possible to construct a curve using either unsorted (in <i>x</i>) data or sorted (ascending in <i>x</i>). Note that if the constructor
 * is told that unsorted data are sorted then no sorting will take place, which will give unpredictable results.
 * @param <T> The type of the x data
 * @param <U> The type of the y data
 */
public abstract class ObjectsCurve<T extends Comparable<T>, U> extends Curve<T, U> {
  private final int _n;
  private final T[] _xData;
  private final U[] _yData;

  /**
   * 
   * @param xData An array of <i>x</i> data, not null
   * @param yData An array of <i>y</i> data, not null, contains same number of entries as <i>x</i>
   * @param isSorted Is the <i>x</i>-data sorted
   */
  public ObjectsCurve(final T[] xData, final U[] yData, final boolean isSorted) {
    super();
    ArgumentChecker.notNull(xData, "x data");
    ArgumentChecker.notNull(yData, "y data");
    _n = xData.length;
    ArgumentChecker.isTrue(_n == yData.length, "size of x data {} does not match size of y data {}", _n, yData.length);
    _xData = Arrays.copyOf(xData, _n);
    _yData = Arrays.copyOf(yData, _n);
    for (int i = 0; i < _n; i++) {
      ArgumentChecker.notNull(xData[i], "element " + i + " of x data");
      ArgumentChecker.notNull(yData[i], "element " + i + " of y data");
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
   * 
   * @param data A map of <i>x-y</i> data, not null
   * @param isSorted Is the <i>x</i>-data sorted
   */
  public ObjectsCurve(final Map<T, U> data, final boolean isSorted) {
    super();
    ArgumentChecker.noNulls(data.keySet(), "x values");
    ArgumentChecker.noNulls(data.values(), "y values");
    _n = data.size();
    final Map.Entry<T, U> firstEntry = data.entrySet().iterator().next();
    _xData = data.keySet().toArray((T[]) Array.newInstance(firstEntry.getKey().getClass(), 0));
    _yData = data.values().toArray((U[]) Array.newInstance(firstEntry.getValue().getClass(), 0));
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
   * 
   * @param data A set of <i>x-y</i> pairs, not null
   * @param isSorted Is the <i>x</i>-data sorted
   */
  public ObjectsCurve(final Set<Pair<T, U>> data, final boolean isSorted) {
    super();
    ArgumentChecker.notNull(data, "data");
    _n = data.size();
    final List<T> xTemp = new ArrayList<>(_n);
    final List<U> yTemp = new ArrayList<>(_n);
    for (final Pair<T, U> entry : data) {
      ArgumentChecker.notNull(entry, "element of data");
      xTemp.add(entry.getFirst());
      yTemp.add(entry.getSecond());
    }
    final Pair<T, U> firstEntry = data.iterator().next();
    _xData = xTemp.toArray((T[]) Array.newInstance(firstEntry.getFirst().getClass(), 0));
    _yData = yTemp.toArray((U[]) Array.newInstance(firstEntry.getSecond().getClass(), 0));
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
   * 
   * @param xData A list of <i>x</i> data points, assumed to be sorted ascending, not null
   * @param yData A list of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param isSorted Is the <i>x</i>-data sorted
   */
  public ObjectsCurve(final List<T> xData, final List<U> yData, final boolean isSorted) {
    super();
    ArgumentChecker.notNull(xData, "x data");
    ArgumentChecker.notNull(yData, "y data");
    _n = xData.size();
    ArgumentChecker.isTrue(_n == yData.size(), "size of x data {} does not match size of y data {}", _n, yData.size());
    _xData = xData.toArray((T[]) Array.newInstance(xData.get(0).getClass(), 0));
    _yData = yData.toArray((U[]) Array.newInstance(yData.get(0).getClass(), 0));
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
   * 
   * @param xData An array of <i>x</i> data, not null
   * @param yData An array of <i>y</i> data, not null, contains same number of entries as <i>x</i>
   * @param isSorted Is the <i>x</i>-data sorted
   * @param name The name of the curve
   */
  public ObjectsCurve(final T[] xData, final U[] yData, final boolean isSorted, final String name) {
    super(name);
    ArgumentChecker.notNull(xData, "x data");
    _n = xData.length;
    ArgumentChecker.notNull(yData, "y data");
    ArgumentChecker.isTrue(_n == yData.length, "size of x data {} does not match size of y data {}", _n, yData.length);
    _xData = Arrays.copyOf(xData, _n);
    _yData = Arrays.copyOf(yData, _n);
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
   * 
   * @param data A map of <i>x-y</i> data, not null
   * @param isSorted Is the <i>x</i>-data sorted
   * @param name The name of the curve
   */
  public ObjectsCurve(final Map<T, U> data, final boolean isSorted, final String name) {
    super(name);
    ArgumentChecker.noNulls(data.keySet(), "x values");
    ArgumentChecker.noNulls(data.values(), "y values");
    _n = data.size();
    final Map.Entry<T, U> firstEntry = data.entrySet().iterator().next();
    _xData = data.keySet().toArray((T[]) Array.newInstance(firstEntry.getKey().getClass(), 0));
    _yData = data.values().toArray((U[]) Array.newInstance(firstEntry.getValue().getClass(), 0));
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }

  }

  /**
   * 
   * @param data A set of <i>x-y</i> pairs, not null
   * @param isSorted Is the <i>x</i>-data sorted
   * @param name The name of the curve
   */
  public ObjectsCurve(final Set<Pair<T, U>> data, final boolean isSorted, final String name) {
    super(name);
    ArgumentChecker.notNull(data, "data");
    _n = data.size();
    final List<T> xTemp = new ArrayList<>(_n);
    final List<U> yTemp = new ArrayList<>(_n);
    final int i = 0;
    for (final Pair<T, U> entry : data) {
      ArgumentChecker.notNull(entry, "element " + i + " of data");
      xTemp.add(entry.getFirst());
      yTemp.add(entry.getSecond());
    }
    final Pair<T, U> firstEntry = data.iterator().next();
    _xData = xTemp.toArray((T[]) Array.newInstance(firstEntry.getFirst().getClass(), 0));
    _yData = yTemp.toArray((U[]) Array.newInstance(firstEntry.getSecond().getClass(), 0));
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
   * 
   * @param xData A list of <i>x</i> data, not null
   * @param yData A list of <i>y</i> data, not null, contains same number of entries as <i>x</i>
   * @param isSorted Is the <i>x</i>-data sorted
   * @param name The name of the curve
   */
  public ObjectsCurve(final List<T> xData, final List<U> yData, final boolean isSorted, final String name) {
    super(name);
    ArgumentChecker.notNull(xData, "x data");
    ArgumentChecker.notNull(yData, "y data");
    _n = xData.size();
    ArgumentChecker.isTrue(_n == yData.size(), "size of x data {} does not match size of y data {}", _n, yData.size());
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
    final ObjectsCurve<?, ?> other = (ObjectsCurve<?, ?>) obj;
    return ArrayUtils.isEquals(_xData, other._xData) && ArrayUtils.isEquals(_yData, other._yData);
  }

}
