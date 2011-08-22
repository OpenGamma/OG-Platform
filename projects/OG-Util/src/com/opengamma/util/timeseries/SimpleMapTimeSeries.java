/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.opengamma.util.ArgumentChecker;


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
  public SimpleMapTimeSeries(Class<T> dateTimeType, Class<V> valueType) {
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
  public SimpleMapTimeSeries(T[] dateTimes, V[] values) {
    ArgumentChecker.notNull(dateTimes, "dateTimes");
    ArgumentChecker.notNull(values, "values");
    ArgumentChecker.isTrue(dateTimes.length == values.length, "Arrays must be same length");
    NavigableMap<T, V> newMap = new TreeMap<T, V>();
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
  private SimpleMapTimeSeries(NavigableMap<T, V> map, T[] oldDateTimes, V[] oldValues) {
    _map = map;
    _times = (T[]) Array.newInstance(oldDateTimes.getClass().getComponentType(), map.size());
    _values = (V[]) Array.newInstance(oldValues.getClass().getComponentType(), map.size());
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
  public V getValue(T dateTime) {
    return _map.get(dateTime);
  }

  @Override
  public T getTimeAt(int index) {
    return _times[index];
  }

  @Override
  public V getValueAt(int index) {
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
  public TimeSeries<T, V> subSeries(T startTime, boolean inclusiveStart, T endTime, boolean exclusiveEnd) {
    return new SimpleMapTimeSeries<T, V>(_map.subMap(startTime, inclusiveStart, endTime, !exclusiveEnd), _times, _values);
  }

  @Override
  public TimeSeries<T, V> subSeries(T startTimeInclusive, T endTimeExclusive) {
    return new SimpleMapTimeSeries<T, V>((NavigableMap<T, V>) _map.subMap(startTimeInclusive, endTimeExclusive), _times, _values);
  }

  @Override
  public TimeSeries<T, V> head(int numItems) {
    T element = getTimeAt(numItems);
    return new SimpleMapTimeSeries<T, V>(_map.headMap(element, true), _times, _values);
  }

  @Override
  public TimeSeries<T, V> tail(int numItems) {
    T element = getTimeAt(size() - numItems);
    return new SimpleMapTimeSeries<T, V>(_map.tailMap(element, true), _times, _values);
  }

  @Override
  public TimeSeries<T, V> lag(int lagCount) {
    if (lagCount == 0) {
      return this;
    } else {
      NavigableMap<T, V> newMap = new TreeMap<T, V>();
      Iterator<T> times = timeIterator();
      Iterator<V> values = valuesIterator();
      if (lagCount > 0) {
        for (int i = 0; i < lagCount; i++) {
          times.next();
        }
        while (times.hasNext()) {
          newMap.put(times.next(), values.next());
        }
      } else {
        for (int i = 0; i < lagCount; i++) {
          values.next();
        }
        while (values.hasNext()) {
          newMap.put(times.next(), values.next());
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
  public TimeSeries<T, V> newInstance(T[] dateTimes, V[] values) {
    return new SimpleMapTimeSeries<T, V>(dateTimes, values);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return ToStringHelper.toString(this);
  }

}
