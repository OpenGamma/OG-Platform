/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.instant;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.threeten.bp.Instant;

import com.opengamma.timeseries.precise.PreciseObjectTimeSeries;

/**
 * Builder implementation for {@code ImmutableInstantObjectTimeSeries}.
 */
final class ImmutableInstantObjectTimeSeriesBuilder<V>
    implements InstantObjectTimeSeriesBuilder<V> {

  /**
   * The time-series.
   */
  private SortedMap<Long, V> _series = new ConcurrentSkipListMap<>();  // use this map to block nulls

  /**
   * Creates an instance.
   */
  protected ImmutableInstantObjectTimeSeriesBuilder() {
  }

  //-------------------------------------------------------------------------
  private static long convertToLong(Instant instant) {
    return InstantToLongConverter.convertToLong(instant);
  }

  private static Instant convertFromLong(long instant) {
    return InstantToLongConverter.convertToInstant(instant);
  }

  //-------------------------------------------------------------------------
  @Override
  public int size() {
    return _series.size();
  }

  @Override
  public InstantObjectEntryIterator<V> iterator() {
    return new InstantObjectEntryIterator<V>() {
      private Iterator<Entry<Long, V>> _iterator = _series.entrySet().iterator();
      private int _index = -1;
      private Entry<Long, V> _current;

      @Override
      public boolean hasNext() {
        return _iterator.hasNext();
      }

      @Override
      public Entry<Instant, V> next() {
        return new SimpleImmutableEntry<>(nextTime(), currentValue());
      }

      @Override
      public long nextTimeFast() {
        if (hasNext() == false) {
          throw new NoSuchElementException("No more elements in the iteration");
        }
        _index++;
        _current = _iterator.next();
        return _current.getKey();
      }

      @Override
      public Instant nextTime() {
        return convertFromLong(nextTimeFast());
      }

      @Override
      public long currentTimeFast() {
        if (_index < 0) {
          throw new IllegalStateException("Iterator has not yet been started");
        }
        if (_current == null) {
          throw new IllegalStateException("Element has been removed");
        }
        return _current.getKey();
      }

      @Override
      public Instant currentTime() {
        return convertFromLong(currentTimeFast());
      }

      @Override
      public V currentValue() {
        if (_index < 0) {
          throw new IllegalStateException("Iterator has not yet been started");
        }
        if (_current == null) {
          throw new IllegalStateException("Element has been removed");
        }
        return _current.getValue();
      }

      @Override
      public int currentIndex() {
        return _index;
      }

      @Override
      public void remove() {
        if (_index < 0) {
          throw new IllegalStateException("Iterator has not yet been started");
        }
        if (_current == null) {
          throw new IllegalStateException("Element has been removed");
        }
        _iterator.remove();
        _current = null;
        _index--;
      }
    };
  }

  //-------------------------------------------------------------------------
  @Override
  public InstantObjectTimeSeriesBuilder<V> put(Instant time, V value) {
    return put(convertToLong(time), value);
  }

  @Override
  public InstantObjectTimeSeriesBuilder<V> put(long time, V value) {
    _series.put(time, value);
    return this;
  }

  @Override
  public InstantObjectTimeSeriesBuilder<V> putAll(Instant[] times, V[] values) {
    if (times.length != values.length) {
      throw new IllegalArgumentException("Arrays are of different sizes: " + times.length + ", " + values.length);
    }
    for (int i = 0; i < times.length; i++) {
      put(times[i], values[i]);
    }
    return this;
  }

  @Override
  public InstantObjectTimeSeriesBuilder<V> putAll(long[] times, V[] values) {
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
  public InstantObjectTimeSeriesBuilder<V> putAll(PreciseObjectTimeSeries<?, V> timeSeries) {
    return putAll(timeSeries, 0, timeSeries.size());
  }

  @Override
  public InstantObjectTimeSeriesBuilder<V> putAll(PreciseObjectTimeSeries<?, V> timeSeries, int startPos, int endPos) {
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
  public InstantObjectTimeSeriesBuilder<V> putAll(Map<Instant, V> timeSeriesMap) {
    if (timeSeriesMap.size() == 0) {
      return this;
    }
    for (Entry<Instant, V> entry : timeSeriesMap.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
    return this;
  }

  //-------------------------------------------------------------------------
  @Override
  public InstantObjectTimeSeriesBuilder<V> clear() {
    _series.clear();
    return this;
  }

  //-------------------------------------------------------------------------
  @Override
  public ImmutableInstantObjectTimeSeries<V> build() {
    long[] times = new long[_series.size()];
    @SuppressWarnings("unchecked")
    V[] values = (V[]) new Object[_series.size()];
    int i = 0;
    for (Entry<Long, V> entry : _series.entrySet()) {
      times[i] = entry.getKey();
      values[i++] = entry.getValue();
    }
    return new ImmutableInstantObjectTimeSeries<V>(times, values);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "Builder[size=" + _series.size() + "]";
  }

}
