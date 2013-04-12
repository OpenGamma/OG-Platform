/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Abstract builder implementation.
 * 
 * @param <T>  the instant type
 */
public abstract class AbstractPreciseDoubleTimeSeriesBuilder<T>
    implements PreciseDoubleTimeSeriesBuilder<T> {

  /**
   * The current size.
   */
  private int _size;
  /**
   * The times.
   */
  private long[] _times = new long[128];
  /**
   * The values.
   */
  private double[] _values = new double[128];

  /**
   * Creates an instance.
   */
  protected AbstractPreciseDoubleTimeSeriesBuilder() {
  }

  //-------------------------------------------------------------------------
  /**
   * Converts the specified instant to the {@code long} form.
   * 
   * @param instant  the instant to convert, not null
   * @return the {@code long} instant
   */
  protected abstract long convertToLong(T instant);

  /**
   * Creates the time-series.
   * 
   * @param times  the times to create from, not null
   * @param values  the values to create from, not null
   * @return the time-series, not null
   */
  protected abstract PreciseDoubleTimeSeries<T> createTimeSeries(long[] times, double[] values);

  private void ensureCapacity(int newSize) {
    if (newSize > _times.length) {
      newSize = Math.max(newSize + 8, (_size * 3) / 2);
      _times = Arrays.copyOf(_times, newSize);
      _values = Arrays.copyOf(_values, _size * 2);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public int size() {
    return _size;
  }

  //-------------------------------------------------------------------------
  @Override
  public PreciseDoubleTimeSeriesBuilder<T> put(T time, double value) {
    return put(convertToLong(time), value);
  }

  @Override
  public PreciseDoubleTimeSeriesBuilder<T> put(long time, double value) {
    int search = Arrays.binarySearch(_times, 0, _size, time);
    if (search >= 0) {
      _values[search] = value;
    } else {
      ensureCapacity(_size + 1);
      int pos = -(search + 1);
      System.arraycopy(_times, pos, _times, pos + 1, _size - pos);
      System.arraycopy(_values, pos, _values, pos + 1, _size - pos);
      _times[pos] = time;
      _values[pos] = value;
      _size++;
    }
    return this;
  }

  @Override
  public PreciseDoubleTimeSeriesBuilder<T> putAll(T[] times, double[] values) {
    if (times.length != values.length) {
      throw new IllegalArgumentException("Arrays are of different sizes: " + times.length + ", " + values.length);
    }
    ensureCapacity(_size + times.length);
    for (int i = 0; i < times.length; i++) {
      put(times[i], values[i]);
    }
    return this;
  }

  @Override
  public PreciseDoubleTimeSeriesBuilder<T> putAll(long[] times, double[] values) {
    if (times.length != values.length) {
      throw new IllegalArgumentException("Arrays are of different sizes: " + times.length + ", " + values.length);
    }
    ensureCapacity(_size + times.length);
    for (int i = 0; i < times.length; i++) {
      put(times[i], values[i]);
    }
    return this;
  }

  //-------------------------------------------------------------------------
  @Override
  public PreciseDoubleTimeSeriesBuilder<T> putAll(PreciseDoubleTimeSeries<?> timeSeries) {
    return putAll(timeSeries, 0, timeSeries.size());
  }

  @Override
  public PreciseDoubleTimeSeriesBuilder<T> putAll(PreciseDoubleTimeSeries<?> timeSeries, int startPos, int endPos) {
    if (startPos < 0 || startPos > timeSeries.size()) {
      throw new IndexOutOfBoundsException("Invalid start index: " + startPos);
    }
    if (endPos < 0 || endPos > timeSeries.size()) {
      throw new IndexOutOfBoundsException("Invalid end index: " + endPos);
    }
    if (startPos > endPos) {
      throw new IndexOutOfBoundsException("End index not be less than start index");
    }
    if (startPos == endPos) {
      return this;
    }
    int sizeToAdd = endPos - startPos;
    ensureCapacity(_size + sizeToAdd);
    if (_size == 0) {
      System.arraycopy(timeSeries.timesArrayFast(), startPos, _times, 0, sizeToAdd);
      System.arraycopy(timeSeries.valuesArrayFast(), startPos, _values, 0, sizeToAdd);
      _size = sizeToAdd;
    } else {
      for (int i = startPos; i < endPos; i++) {
        put(timeSeries.getTimeAtIndexFast(i), timeSeries.getValueAtIndexFast(i));
      }
    }
    return this;
  }

  @Override
  public PreciseDoubleTimeSeriesBuilder<T> putAll(Map<T, Double> timeSeriesMap) {
    if (timeSeriesMap.size() == 0) {
      return this;
    }
    ensureCapacity(_size + timeSeriesMap.size());
    for (Entry<T, Double> entry : timeSeriesMap.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
    return this;
  }

  //-------------------------------------------------------------------------
  @Override
  public PreciseDoubleTimeSeriesBuilder<T> clear() {
    _size = 0;
    return this;
  }

  //-------------------------------------------------------------------------
  @Override
  public PreciseDoubleTimeSeries<T> build() {
    return createTimeSeries(Arrays.copyOf(_times, _size), Arrays.copyOf(_values, _size));
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "Builder[size=" + _size + "]";
  }

}
