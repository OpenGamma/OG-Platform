/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.zdt;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.precise.PreciseDoubleTimeSeries;

/**
 * Builder implementation for {@code ImmutableZonedDateTimeDoubleTimeSeries}.
 */
final class ImmutableZonedDateTimeDoubleTimeSeriesBuilder
    implements ZonedDateTimeDoubleTimeSeriesBuilder {

  /**
   * The time-zone.
   */
  private final ZoneId _zone;
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
   * 
   * @param zone  the time-zone, not null
   */
  ImmutableZonedDateTimeDoubleTimeSeriesBuilder(ZoneId zone) {
    _zone = Objects.requireNonNull(zone, "zone");
  }

  //-------------------------------------------------------------------------
  private void ensureCapacity(int newSize) {
    if (newSize > _times.length) {
      newSize = Math.max(newSize + 8, (_size * 3) / 2);
      _times = Arrays.copyOf(_times, newSize);
      _values = Arrays.copyOf(_values, _size * 2);
    }
  }

  private static long convertToLong(ZonedDateTime instant) {
    return ZonedDateTimeToLongConverter.convertToLong(instant);
  }

  private static ZonedDateTime convertFromLong(long instant, ZoneId zone) {
    return ZonedDateTimeToLongConverter.convertToZonedDateTime(instant, zone);
  }

  //-------------------------------------------------------------------------
  @Override
  public int size() {
    return _size;
  }

  @Override
  public ZonedDateTimeDoubleEntryIterator iterator() {
    return new ZonedDateTimeDoubleEntryIterator() {
      private int _index = -1;

      @Override
      public boolean hasNext() {
        return (_index + 1) < size();
      }

      @Override
      public Entry<ZonedDateTime, Double> next() {
        if (hasNext() == false) {
          throw new NoSuchElementException("No more elements in the iteration");
        }
        _index++;
        long instant = _times[_index];
        double value = _values[_index];
        return makeMapEntry(convertFromLong(instant, _zone), value);
      }

      private Entry<ZonedDateTime, Double> makeMapEntry(ZonedDateTime key, Double value) {
        return new SimpleImmutableEntry<>(key, value);
      }

      @Override
      public long nextTimeFast() {
        if (hasNext() == false) {
          throw new NoSuchElementException("No more elements in the iteration");
        }
        _index++;
        return _times[_index];
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
        return _times[_index];
      }

      @Override
      public ZonedDateTime currentTime() {
        return convertFromLong(currentTimeFast(), _zone);
      }

      @Override
      public Double currentValue() {
        if (_index < 0) {
          throw new IllegalStateException("Iterator has not yet been started");
        }
        return _values[_index];
      }

      @Override
      public double currentValueFast() {
        if (_index < 0) {
          throw new IllegalStateException("Iterator has not yet been started");
        }
        return _values[_index];
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
        if (_index < _size) {
          System.arraycopy(_times, _index + 1, _times, _index, _size - _index);
          System.arraycopy(_values, _index + 1, _values, _index, _size - _index);
        }
        _size--;
        _index--;
      }
    };
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeDoubleTimeSeriesBuilder put(ZonedDateTime time, double value) {
    return put(convertToLong(time), value);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeriesBuilder put(long time, double value) {
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
  public ZonedDateTimeDoubleTimeSeriesBuilder putAll(ZonedDateTime[] times, double[] values) {
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
  public ZonedDateTimeDoubleTimeSeriesBuilder putAll(long[] times, double[] values) {
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
  public ZonedDateTimeDoubleTimeSeriesBuilder putAll(PreciseDoubleTimeSeries<?> timeSeries) {
    return putAll(timeSeries, 0, timeSeries.size());
  }

  @Override
  public ZonedDateTimeDoubleTimeSeriesBuilder putAll(PreciseDoubleTimeSeries<?> timeSeries, int startPos, int endPos) {
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
  public ZonedDateTimeDoubleTimeSeriesBuilder putAll(Map<ZonedDateTime, Double> timeSeriesMap) {
    if (timeSeriesMap.size() == 0) {
      return this;
    }
    ensureCapacity(_size + timeSeriesMap.size());
    for (Entry<ZonedDateTime, Double> entry : timeSeriesMap.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
    return this;
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeDoubleTimeSeriesBuilder clear() {
    _size = 0;
    return this;
  }

  //-------------------------------------------------------------------------
  @Override
  public ImmutableZonedDateTimeDoubleTimeSeries build() {
    if (_size == 0) {
      return ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(_zone);
    }
    return new ImmutableZonedDateTimeDoubleTimeSeries(Arrays.copyOf(_times, _size), Arrays.copyOf(_values, _size), _zone);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "Builder[size=" + _size + "]";
  }

}
