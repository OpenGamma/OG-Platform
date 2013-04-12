/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Abstract builder implementation.
 * 
 * @param <T>  the date type
 */
public abstract class AbstractDateDoubleTimeSeriesBuilder<T>
    implements DateDoubleTimeSeriesBuilder<T> {

  /**
   * The current size.
   */
  private int _size;
  /**
   * The times.
   */
  private int[] _times = new int[128];
  /**
   * The values.
   */
  private double[] _values = new double[128];

  /**
   * Creates an instance.
   */
  public AbstractDateDoubleTimeSeriesBuilder() {
  }

  //-------------------------------------------------------------------------
  /**
   * Converts the specified date to the {@code int} form.
   * 
   * @param date  the date to convert, not null
   * @return the {@code int} date
   */
  protected abstract int convertToInt(T date);

  /**
   * Creates the time-series.
   * 
   * @param times  the times to create from, not null
   * @param values  the values to create from, not null
   * @return the time-series, not null
   */
  protected abstract DateDoubleTimeSeries<T> createTimeSeries(int[] times, double[] values);

  private void ensureCapacity(int newSize) {
    if (newSize > _times.length) {
      newSize = Math.max(newSize + 8, (_size * 3) / 2);
      _times = Arrays.copyOf(_times, newSize);
      _values = Arrays.copyOf(_values, _size * 2);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public DateDoubleTimeSeriesBuilder<T> put(T time, double value) {
    return put(convertToInt(time), value);
  }

  @Override
  public DateDoubleTimeSeriesBuilder<T> put(int time, double value) {
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
  public DateDoubleTimeSeriesBuilder<T> putAll(T[] times, double[] values) {
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
  public DateDoubleTimeSeriesBuilder<T> putAll(int[] times, double[] values) {
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
  public DateDoubleTimeSeriesBuilder<T> putAll(DateDoubleTimeSeries<?> timeSeries) {
    return putAll(timeSeries, 0, timeSeries.size());
  }

  @Override
  public DateDoubleTimeSeriesBuilder<T> putAll(DateDoubleTimeSeries<?> timeSeries, int startPos, int endPos) {
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
  public DateDoubleTimeSeriesBuilder<T> putAll(Map<T, Double> timeSeriesMap) {
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
  public DateDoubleTimeSeriesBuilder<T> clear() {
    _size = 0;
    return this;
  }

  //-------------------------------------------------------------------------
  @Override
  public DateDoubleTimeSeries<T> build() {
    return createTimeSeries(Arrays.copyOf(_times, _size), Arrays.copyOf(_values, _size));
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "Builder[size=" + _size + "]";
  }

}
