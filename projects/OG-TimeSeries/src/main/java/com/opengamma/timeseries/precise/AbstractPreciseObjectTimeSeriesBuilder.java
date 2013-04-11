/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise;

import java.util.Map;
import java.util.SortedMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Abstract builder implementation.
 * 
 * @param <T>  the instant type
 * @param <V>  the value being viewed over time
 */
public abstract class AbstractPreciseObjectTimeSeriesBuilder<T, V>
    implements PreciseObjectTimeSeriesBuilder<T, V> {

  /**
   * The time-series.
   */
  private SortedMap<Long, V> _series = new ConcurrentSkipListMap<>();  // use this map to block nulls

  /**
   * Creates an instance.
   */
  protected AbstractPreciseObjectTimeSeriesBuilder() {
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
   * @param series  the times and values to create from, not null
   * @return the time-series, not null
   */
  protected abstract PreciseObjectTimeSeries<T, V> createTimeSeries(SortedMap<Long, V> series);

  //-------------------------------------------------------------------------
  @Override
  public PreciseObjectTimeSeriesBuilder<T, V> put(T time, V value) {
    return put(convertToLong(time), value);
  }

  @Override
  public PreciseObjectTimeSeriesBuilder<T, V> put(long time, V value) {
    _series.put(time, value);
    return this;
  }

  @Override
  public PreciseObjectTimeSeriesBuilder<T, V> putAll(T[] times, V[] values) {
    if (times.length != values.length) {
      throw new IllegalArgumentException("Arrays are of different sizes: " + times.length + ", " + values.length);
    }
    for (int i = 0; i < times.length; i++) {
      put(times[i], values[i]);
    }
    return this;
  }

  @Override
  public PreciseObjectTimeSeriesBuilder<T, V> putAll(long[] times, V[] values) {
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
  public PreciseObjectTimeSeriesBuilder<T, V> putAll(PreciseObjectTimeSeries<?, V> timeSeries) {
    return putAll(timeSeries, 0, timeSeries.size());
  }

  @Override
  public PreciseObjectTimeSeriesBuilder<T, V> putAll(PreciseObjectTimeSeries<?, V> timeSeries, int startPos, int endPos) {
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
  public PreciseObjectTimeSeriesBuilder<T, V> putAll(Map<T, V> timeSeriesMap) {
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
  public PreciseObjectTimeSeriesBuilder<T, V> clear() {
    _series.clear();
    return this;
  }

  //-------------------------------------------------------------------------
  @Override
  public PreciseObjectTimeSeries<T, V> build() {
    return createTimeSeries(_series);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "Builder[size=" + _series.size() + "]";
  }

}
