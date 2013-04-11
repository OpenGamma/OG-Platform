/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date;

import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Abstract builder implementation.
 * 
 * @param <T>  the date type
 * @param <V>  the value being viewed over time
 */
public abstract class AbstractDateObjectTimeSeriesBuilder<T, V>
    implements DateObjectTimeSeriesBuilder<T, V> {

  /**
   * The time-series.
   */
  private SortedMap<Integer, V> _series = new ConcurrentSkipListMap<>();  // use this map to block nulls

  /**
   * Creates an instance.
   */
  protected AbstractDateObjectTimeSeriesBuilder() {
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
   * @param series  the times and values to create from, not null
   * @return the time-series, not null
   */
  protected abstract DateObjectTimeSeries<T, V> createTimeSeries(SortedMap<Integer, V> series);

  //-------------------------------------------------------------------------
  @Override
  public DateObjectTimeSeriesBuilder<T, V> put(T time, V value) {
    return put(convertToInt(time), value);
  }

  @Override
  public DateObjectTimeSeriesBuilder<T, V> put(int time, V value) {
    _series.put(time, value);
    return this;
  }

  @Override
  public DateObjectTimeSeriesBuilder<T, V> putAll(T[] times, V[] values) {
    if (times.length != values.length) {
      throw new IllegalArgumentException("Arrays are of different sizes: " + times.length + ", " + values.length);
    }
    for (int i = 0; i < times.length; i++) {
      put(times[i], values[i]);
    }
    return this;
  }

  @Override
  public DateObjectTimeSeriesBuilder<T, V> putAll(int[] times, V[] values) {
    if (times.length != values.length) {
      throw new IllegalArgumentException("Arrays are of different sizes: " + times.length + ", " + values.length);
    }
    for (int i = 0; i < times.length; i++) {
      put(times[i], values[i]);
    }
    return this;
  }

  //-------------------------------------------------------------------------
  @Override
  public DateObjectTimeSeriesBuilder<T, V> putAll(DateObjectTimeSeries<?, V> timeSeries) {
    return putAll(timeSeries, 0, timeSeries.size());
  }

  @Override
  public DateObjectTimeSeriesBuilder<T, V> putAll(DateObjectTimeSeries<?, V> timeSeries, int startPos, int endPos) {
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
    for (int i = startPos; i < endPos; i++) {
      put(timeSeries.getTimeAtIndexFast(i), timeSeries.getValueAtIndex(i));
    }
    return this;
  }

  @Override
  public DateObjectTimeSeriesBuilder<T, V> putAll(Map<T, V> timeSeriesMap) {
    if (timeSeriesMap.size() == 0) {
      return this;
    }
    for (Entry<T, V> entry : timeSeriesMap.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
    return this;
  }

  //-------------------------------------------------------------------------
  @Override
  public DateObjectTimeSeriesBuilder<T, V> clear() {
    _series.clear();
    return this;
  }

  //-------------------------------------------------------------------------
  @Override
  public DateObjectTimeSeries<T, V> build() {
    return createTimeSeries(_series);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "Builder[size=" + _series.size() + "]";
  }

}
