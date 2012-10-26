/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.integer.object;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.SortedMap;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.object.FastLongObjectTimeSeries;

/**
 * 
 * @param <T> The type of the data
 */
public class FastMapIntObjectTimeSeries<T> extends AbstractFastMutableIntObjectTimeSeries<T> {

  private Int2ObjectSortedMap<T> _map = new Int2ObjectAVLTreeMap<T>();
  private final T _defaultReturnValue = _map.defaultReturnValue();

  public FastMapIntObjectTimeSeries(final DateTimeNumericEncoding encoding) {
    super(encoding);
  }

  public FastMapIntObjectTimeSeries(final DateTimeNumericEncoding encoding, final int[] times, final T[] values) {
    super(encoding);
    if (times.length != values.length) {
      throw new OpenGammaRuntimeException("times and values arrays must be the same length");
    }
    for (int i = 0; i < times.length; i++) {
      _map.put(times[i], values[i]);
    }
  }

  public FastMapIntObjectTimeSeries(final DateTimeNumericEncoding encoding, final List<Integer> times,
      final List<T> values) {
    super(encoding);
    if (times.size() != values.size()) {
      throw new OpenGammaRuntimeException("times and values lists must be the same length");
    }
    final Iterator<Integer> timeIter = times.iterator();
    final Iterator<T> valuesIter = values.iterator();
    while (timeIter.hasNext()) {
      _map.put(timeIter.next(), valuesIter.next());
    }
  }

  public FastMapIntObjectTimeSeries(final FastIntObjectTimeSeries<T> dts) {
    super(dts.getEncoding());
    if (dts instanceof FastArrayIntObjectTimeSeries<?> || dts instanceof FastListIntObjectTimeSeries<?>) {
      // these are cheap to get arrays from.
      final int[] times = dts.timesArrayFast();
      final T[] values = dts.valuesArrayFast();
      for (int i = 0; i < times.length; i++) {
        _map.put(times[i], values[i]);
      }
    } else {
      final Iterator<Map.Entry<Integer, T>> iter = dts.iterator();
      while (iter.hasNext()) {
        final Entry<Integer, T> next = iter.next();
        _map.put(next.getKey(), next.getValue());
      }
    }
  }

  public FastMapIntObjectTimeSeries(final DateTimeNumericEncoding encoding, final SortedMap<Integer, T> initialMap) {
    super(encoding);
    _map.putAll(initialMap);
  }

  @Override
  public int getEarliestTimeFast() {
    return _map.firstIntKey();
  }

  @Override
  public T getEarliestValueFast() {
    return _map.get(_map.firstIntKey());
  }

  @Override
  public int getLatestTimeFast() {
    return _map.lastIntKey();
  }

  @Override
  public T getLatestValueFast() {
    return _map.get(_map.lastIntKey());
  }

  @Override
  public int getTimeFast(final int index) {
    if (index >= _map.size()) {
      throw new IndexOutOfBoundsException();
    }
    final IntBidirectionalIterator iterator = _map.keySet().iterator();
    iterator.skip(index);
    return iterator.nextInt();
  }

  @Override
  public T getValueAtFast(final int index) {
    if (index >= _map.size()) {
      throw new IndexOutOfBoundsException();
    }
    final IntBidirectionalIterator iterator = _map.keySet().iterator();
    iterator.skip(index);
    return _map.get(iterator.nextInt());
  }

  @Override
  public T getValueFast(final int time) {
    T value = _map.get(time);
    if (value == _defaultReturnValue) {
      throw new NoSuchElementException();
    }
    return value;
  }

  @Override
  public boolean isEmpty() {
    return _map.isEmpty();
  }

  @Override
  public int size() {
    return _map.size();
  }

  @Override
  public FastIntObjectTimeSeries<T> subSeriesFast(final int startTime, final int endTime) {
    return new FastMapIntObjectTimeSeries<T>(getEncoding(), _map.subMap(startTime, endTime));
  }

  @Override
  public int[] timesArrayFast() {
    return _map.keySet().toIntArray();
  }

  @Override
  public IntIterator timesIteratorFast() {
    return _map.keySet().iterator();
  }

  @Override
  public IntList timesFast() {
    return new IntArrayList(_map.keySet());
  }

  @SuppressWarnings("unchecked")
  @Override
  public T[] valuesArrayFast() {
    return (T[]) _map.values().toArray();
  }

  @Override
  public ObjectIterator<T> valuesIteratorFast() {
    return _map.values().iterator();
  }

  @Override
  public ObjectList<T> valuesFast() {
    return new ObjectArrayList<T>(_map.values());
  }

  @SuppressWarnings("unchecked")
  @Override
  public ObjectIterator<Int2ObjectMap.Entry<T>> iteratorFast() {
    return (ObjectIterator<Int2ObjectMap.Entry<T>>) (ObjectIterator<? extends Entry<Integer, T>>) _map.entrySet()
        .iterator();
  }

  @Override
  public FastIntObjectTimeSeries<T> headFast(final int numItems) {
    if (_map.size() < numItems) {
      throw new OpenGammaRuntimeException("cannot get head " + numItems +
          " items because there aren't that many in the series");
    }
    final IntBidirectionalIterator iterator = _map.keySet().iterator();
    iterator.skip(numItems);
    if (iterator.hasNext() && numItems != 0) {
      final int time = iterator.nextInt();
      return new FastMapIntObjectTimeSeries<T>(getEncoding(), _map.headMap(time + 1));
      // +1 means we get inclusive rather than exclusive.
    } else {
      return new FastMapIntObjectTimeSeries<T>(getEncoding()); //empty series...
    }
  }

  @Override
  public FastIntObjectTimeSeries<T> tailFast(final int numItems) {
    if (_map.size() < numItems) {
      throw new OpenGammaRuntimeException("cannot get head " + numItems +
          " items because there aren't that many in the series");
    }
    final IntBidirectionalIterator iterator = _map.keySet().iterator();
    iterator.skip(_map.size() - numItems);
    if (iterator.hasNext() && numItems != 0) {
      final int time = iterator.nextInt();
      return new FastMapIntObjectTimeSeries<T>(getEncoding(), _map.tailMap(time));
    } else {
      return new FastMapIntObjectTimeSeries<T>(getEncoding());
    }
  }

  @Override
  public ObjectTimeSeries<Integer, T> head(final int numItems) {
    return headFast(numItems);
  }

  @Override
  public ObjectTimeSeries<Integer, T> tail(final int numItems) {
    return tailFast(numItems);
  }

  @Override
  public void clear() {
    _map.clear();
  }

  @Override
  public void primitivePutDataPoint(final int time, final T value) {
    _map.put(time, value);
  }

  @Override
  public void primitiveRemoveDataPoint(final int time) {
    _map.remove(time);
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      if (obj instanceof FastIntObjectTimeSeries<?>) {
        final FastIntObjectTimeSeries<T> other = (FastIntObjectTimeSeries<T>) obj;
        if (size() != other.size()) {
          return false;
        }
        final ObjectIterator<Int2ObjectMap.Entry<T>> otherIterator = other.iteratorFast();
        final ObjectIterator<Int2ObjectMap.Entry<T>> myIterator = iteratorFast();
        final DateTimeNumericEncoding otherEncoding = other.getEncoding();
        final DateTimeNumericEncoding myEncoding = getEncoding();
        // invariant is that both are the same length as we got past the
        // values equality
        while (otherIterator.hasNext()) {
          final Int2ObjectMap.Entry otherNext = otherIterator.next();
          final Int2ObjectMap.Entry myNext = myIterator.next();
          if (!ObjectUtils.equals(otherNext.getValue(), myNext.getValue())) {
            return false;
          }
          if (otherEncoding.convertToInt(otherNext.getIntKey(), myEncoding) != myNext.getIntKey()) {
            return false;
          }
        }
      } else if (obj instanceof FastLongObjectTimeSeries<?>) {
        final FastLongObjectTimeSeries<T> other = (FastLongObjectTimeSeries<T>) obj;
        if (size() != other.size()) {
          return false;
        }
        final ObjectIterator<Long2ObjectMap.Entry<T>> otherIterator = other.iteratorFast();
        final ObjectIterator<Int2ObjectMap.Entry<T>> myIterator = iteratorFast();
        final DateTimeNumericEncoding otherEncoding = other.getEncoding();
        final DateTimeNumericEncoding myEncoding = getEncoding();
        // invariant is that both are the same length as we got past the
        // values equality
        while (otherIterator.hasNext()) {
          final Long2ObjectMap.Entry<T> otherNext = otherIterator.next();
          final Int2ObjectMap.Entry<T> myNext = myIterator.next();
          if (!ObjectUtils.equals(otherNext.getValue(), myNext.getValue())) {
            return false;
          }
          if (otherEncoding.convertToInt(otherNext.getLongKey(), myEncoding) != myNext.getIntKey()) {
            return false;
          }
        }
      } else {
        return false;
      }
    } else {
      final FastMapIntObjectTimeSeries other = (FastMapIntObjectTimeSeries) obj;
      // invariant: none of these can be null.
      if (_map.size() != other._map.size()) { // assuming the size is cheap to
        // know.
        return false;
      }
      if (other.getEncoding() != getEncoding()) {
        final ObjectIterator<Int2ObjectMap.Entry<T>> otherIterator = other.iteratorFast();
        final ObjectIterator<Int2ObjectMap.Entry<T>> myIterator = iteratorFast();
        final DateTimeNumericEncoding otherEncoding = other.getEncoding();
        final DateTimeNumericEncoding myEncoding = getEncoding();
        // invariant is that both are the same length as we got past the
        // values equality
        while (otherIterator.hasNext()) {
          final Int2ObjectMap.Entry otherNext = otherIterator.next();
          final Int2ObjectMap.Entry myNext = myIterator.next();
          if (!ObjectUtils.equals(otherNext.getValue(), myNext.getValue())) {
            return false;
          }
          if (otherEncoding.convertToInt(otherNext.getIntKey(), myEncoding) != myNext.getIntKey()) {
            return false;
          }
        }
      } else {
        if (!_map.equals(other._map)) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    return _map.hashCode();
  }

  @Override
  public FastIntObjectTimeSeries<T> newInstanceFast(final int[] times, final T[] values) {
    return new FastMapIntObjectTimeSeries<T>(getEncoding(), times, values);
  }
}
