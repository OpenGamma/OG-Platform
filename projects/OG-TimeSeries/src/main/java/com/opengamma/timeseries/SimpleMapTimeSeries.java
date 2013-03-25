/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;


/**
 * A simple time-series implementation based on a map.
 * <p>
 * This class is mostly useful to understand the basic time-series API.
 * 
 * @param <T> the date-time type, such as {@code Instant} or {@code LocalDate}
 * @param <V> the value being viewed over time, such as {@code Double}
 */
public class SimpleMapTimeSeries<T, V> implements TimeSeries<T, V> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The map of data points.
   */
  private final NavigableMap<T, V> _map;
  /**
   * The indexed data points.
   */
  private final T[] _times;
  /**
   * The indexed data points.
   */
  private final V[] _values;

  /**
   * Creates an empty instance.
   * 
   * @param dateTimeType  the date-time type, not null
   * @param valueType  the value type, not null
   */
  @SuppressWarnings("unchecked")
  public SimpleMapTimeSeries(final Class<T> dateTimeType, final Class<V> valueType) {
    ArgumentChecker.notNull(dateTimeType, "dateTimeType");
    ArgumentChecker.notNull(valueType, "valueType");
    _map = new TreeMap<T, V>();
    _times = (T[]) Array.newInstance(dateTimeType, 0);
    _values = (V[]) Array.newInstance(valueType, 0);
  }

  /**
   * Creates an instance.
   * 
   * @param dateTimes  the date-times, not null
   * @param values  the values, not null
   */
  public SimpleMapTimeSeries(final T[] dateTimes, final V[] values) {
    ArgumentChecker.notNull(dateTimes, "dateTimes");
    ArgumentChecker.notNull(values, "values");
    ArgumentChecker.isTrue(dateTimes.length == values.length, "Arrays must be same length");
    final NavigableMap<T, V> newMap = new TreeMap<T, V>();
    for (int i = 0; i < dateTimes.length; i++) {
      newMap.put(dateTimes[i], values[i]);
    }
    _map = newMap;
    _times = dateTimes.clone();
    _values = values.clone();
  }

  /**
   * Creates an instance (for private use only).
   * 
   * @param map  the map to assign, not null
   * @param oldDateTimes  the old date-time array, not null
   * @param oldValues  the old value array, not null
   */
  @SuppressWarnings("unchecked")
  private SimpleMapTimeSeries(final NavigableMap<T, V> map, final T[] oldDateTimes, final V[] oldValues) {
    ArgumentChecker.notNull(map, "map");
    ArgumentChecker.notNull(oldDateTimes, "oldDateTimes");
    ArgumentChecker.notNull(oldValues, "oldValues");
    _map = map;
    _times = (T[]) Array.newInstance(oldDateTimes.getClass().getComponentType(), map.size());
    _values = (V[]) Array.newInstance(oldValues.getClass().getComponentType(), map.size());
    int i = 0;
    for (final T oldDateTime : oldDateTimes) {
      if (map.containsKey(oldDateTime)) {
        _times[i] = oldDateTime;
        _values[i++] = map.get(oldDateTime);
      }
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public int size() {
    return _map.size();
  }

  @Override
  public boolean isEmpty() {
    return _map.isEmpty();
  }

  @Override
  public V getValue(final T dateTime) {
    return _map.get(dateTime);
  }

  @Override
  public T getTimeAt(final int index) {
    return _times[index];
  }

  @Override
  public V getValueAt(final int index) {
    return _values[index];
  }

  @Override
  public T getLatestTime() {
    return _map.lastKey();
  }

  @Override
  public V getLatestValue() {
    return _map.get(_map.lastKey());
  }

  @Override
  public T getEarliestTime() {
    return _map.firstKey();
  }

  @Override
  public V getEarliestValue() {
    return _map.get(_map.firstKey());
  }

  @Override
  public Iterator<Entry<T, V>> iterator() {
    return _map.entrySet().iterator();
  }

  @Override
  public Iterator<T> timeIterator() {
    return _map.keySet().iterator();
  }

  @Override
  public Iterator<V> valuesIterator() {
    return _map.values().iterator();
  }

  @Override
  public TimeSeries<T, V> subSeries(final T startTime, final boolean includeStart, final T endTime, final boolean includeEnd) {
    return new SimpleMapTimeSeries<T, V>(_map.subMap(startTime, includeStart, endTime, includeEnd), _times, _values);
  }

  @Override
  public TimeSeries<T, V> subSeries(final T startTimeInclusive, final T endTimeExclusive) {
    return new SimpleMapTimeSeries<T, V>((NavigableMap<T, V>) _map.subMap(startTimeInclusive, endTimeExclusive), _times, _values);
  }

  @Override
  public TimeSeries<T, V> head(final int numItems) {
    final T element = getTimeAt(numItems);
    return new SimpleMapTimeSeries<T, V>(_map.headMap(element, true), _times, _values);
  }

  @Override
  public TimeSeries<T, V> tail(final int numItems) {
    final T element = getTimeAt(size() - numItems);
    return new SimpleMapTimeSeries<T, V>(_map.tailMap(element, true), _times, _values);
  }

  @Override
  public TimeSeries<T, V> lag(final int lagCount) {
    if (lagCount == 0) {
      return this;
    } else {
      final NavigableMap<T, V> newMap = new TreeMap<T, V>();
      final Iterator<T> times = timeIterator();
      final Iterator<V> values = valuesIterator();
      if (lagCount > 0) {
        if (lagCount < _times.length) {
          for (int i = 0; i < lagCount; i++) {
            times.next();
          }
          while (times.hasNext()) {
            newMap.put(times.next(), values.next());
          }
        }
      } else {
        if (-lagCount < _times.length) {
          for (int i = lagCount; i < 0; i++) {
            values.next();
          }
          while (values.hasNext()) {
            newMap.put(times.next(), values.next());
          }
        }
      }
      return new SimpleMapTimeSeries<T, V>(newMap, _times, _values);
    }
  }

  @Override
  public List<T> times() {
    return new ArrayList<T>(Arrays.asList(_times));
  }

  @Override
  public T[] timesArray() {
    return _times.clone();
  }

  @Override
  public List<V> values() {
    return new ArrayList<V>(Arrays.asList(_values));
  }

  @Override
  public V[] valuesArray() {
    return _values.clone();
  }

  @Override
  public TimeSeries<T, V> newInstance(final T[] dateTimes, final V[] values) {
    return new SimpleMapTimeSeries<T, V>(dateTimes, values);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return TimeSeriesUtils.toString(this);
  }

}
