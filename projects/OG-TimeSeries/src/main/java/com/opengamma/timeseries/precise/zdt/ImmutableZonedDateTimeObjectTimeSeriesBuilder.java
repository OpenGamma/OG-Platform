/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.zdt;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.precise.PreciseObjectTimeSeries;

/**
 * Builder implementation for {@code ImmutableZonedDateTimeObjectTimeSeries}.
 */
final class ImmutableZonedDateTimeObjectTimeSeriesBuilder<V>
    implements ZonedDateTimeObjectTimeSeriesBuilder<V> {

  /**
   * The time-zone.
   */
  private final ZoneId _zone;
  /**
   * The time-series.
   */
  private SortedMap<Long, V> _series = new ConcurrentSkipListMap<>();  // use this map to block nulls

  /**
   * Creates an instance.
   * 
   * @param zone  the time-zone, not null
   */
  protected ImmutableZonedDateTimeObjectTimeSeriesBuilder(ZoneId zone) {
    _zone = Objects.requireNonNull(zone, "zone");
  }

  //-------------------------------------------------------------------------
  private static long convertToLong(ZonedDateTime instant) {
    return ZonedDateTimeToLongConverter.convertToLong(instant);
  }

  private static ZonedDateTime convertFromLong(long instant, ZoneId zone) {
    return ZonedDateTimeToLongConverter.convertToZonedDateTime(instant, zone);
  }

  //-------------------------------------------------------------------------
  @Override
  public int size() {
    return _series.size();
  }

  @Override
  public ZonedDateTimeObjectEntryIterator<V> iterator() {
    return new ZonedDateTimeObjectEntryIterator<V>() {
      private Iterator<Entry<Long, V>> _iterator = _series.entrySet().iterator();
      private int _index = -1;
      private Entry<Long, V> _current;

      @Override
      public boolean hasNext() {
        return _iterator.hasNext();
      }

      @Override
      public Entry<ZonedDateTime, V> next() {
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
      public ZonedDateTime nextTime() {
        return convertFromLong(nextTimeFast(), _zone);
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
      public ZonedDateTime currentTime() {
        return convertFromLong(currentTimeFast(), _zone);
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
  public ZonedDateTimeObjectTimeSeriesBuilder<V> put(ZonedDateTime time, V value) {
    return put(convertToLong(time), value);
  }

  @Override
  public ZonedDateTimeObjectTimeSeriesBuilder<V> put(long time, V value) {
    _series.put(time, value);
    return this;
  }

  @Override
  public ZonedDateTimeObjectTimeSeriesBuilder<V> putAll(ZonedDateTime[] times, V[] values) {
    if (times.length != values.length) {
      throw new IllegalArgumentException("Arrays are of different sizes: " + times.length + ", " + values.length);
    }
    for (int i = 0; i < times.length; i++) {
      put(times[i], values[i]);
    }
    return this;
  }

  @Override
  public ZonedDateTimeObjectTimeSeriesBuilder<V> putAll(long[] times, V[] values) {
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
  public ZonedDateTimeObjectTimeSeriesBuilder<V> putAll(PreciseObjectTimeSeries<?, V> timeSeries) {
    return putAll(timeSeries, 0, timeSeries.size());
  }

  @Override
  public ZonedDateTimeObjectTimeSeriesBuilder<V> putAll(PreciseObjectTimeSeries<?, V> timeSeries, int startPos, int endPos) {
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
  public ZonedDateTimeObjectTimeSeriesBuilder<V> putAll(Map<ZonedDateTime, V> timeSeriesMap) {
    if (timeSeriesMap.size() == 0) {
      return this;
    }
    for (Entry<ZonedDateTime, V> entry : timeSeriesMap.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
    return this;
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeObjectTimeSeriesBuilder<V> clear() {
    _series.clear();
    return this;
  }

  //-------------------------------------------------------------------------
  @Override
  public ImmutableZonedDateTimeObjectTimeSeries<V> build() {
    long[] times = new long[_series.size()];
    @SuppressWarnings("unchecked")
    V[] values = (V[]) new Object[_series.size()];
    int i = 0;
    for (Entry<Long, V> entry : _series.entrySet()) {
      times[i] = entry.getKey();
      values[i++] = entry.getValue();
    }
    return new ImmutableZonedDateTimeObjectTimeSeries<V>(times, values, _zone);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "Builder[size=" + _series.size() + "]";
  }

}
