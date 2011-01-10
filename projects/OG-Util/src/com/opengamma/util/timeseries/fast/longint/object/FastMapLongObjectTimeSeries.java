/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.longint.object;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.Map.Entry;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.timeseries.AbstractFastBackedObjectTimeSeries;
import com.opengamma.util.timeseries.FastBackedObjectTimeSeries;
import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.object.FastIntObjectTimeSeries;

/**
 * @author jim
 * 
 */
public class FastMapLongObjectTimeSeries<T> extends AbstractFastMutableLongObjectTimeSeries<T> {

  private Long2ObjectSortedMap<T> _map = new Long2ObjectAVLTreeMap<T>();
  private final T _defaultReturnValue = _map.defaultReturnValue();

  public FastMapLongObjectTimeSeries(final DateTimeNumericEncoding encoding) {
    super(encoding);
  }

  public FastMapLongObjectTimeSeries(final DateTimeNumericEncoding encoding, final long[] times, final T[] values) {
    super(encoding);
    if (times.length != values.length) {
      throw new OpenGammaRuntimeException("times and values arrays must be the same length");
    }
    for (int i = 0; i < times.length; i++) {
      _map.put(times[i], values[i]);
    }
  }

  public FastMapLongObjectTimeSeries(final DateTimeNumericEncoding encoding, final List<Long> times, final List<T> values) {
    super(encoding);
    if (times.size() != values.size()) {
      throw new OpenGammaRuntimeException("times and values lists must be the same length");
    }
    final Iterator<Long> timeIter = times.iterator();
    final Iterator<T> valuesIter = values.iterator();
    while (timeIter.hasNext()) {
      _map.put(timeIter.next(), valuesIter.next());
    }
  }

  public FastMapLongObjectTimeSeries(final FastLongObjectTimeSeries<T> dts) {
    super(dts.getEncoding());
    if (dts instanceof FastArrayLongObjectTimeSeries<?> || dts instanceof FastListLongObjectTimeSeries<?>) {
      // these are cheap to get arrays from.
      final long[] times = dts.timesArrayFast();
      final T[] values = dts.valuesArrayFast();
      for (int i = 0; i < times.length; i++) {
        _map.put(times[i], values[i]);
      }
    } else {
      final Iterator<Map.Entry<Long, T>> iter = dts.iterator();
      while (iter.hasNext()) {
        final Entry<Long, T> next = iter.next();
        _map.put(next.getKey(), next.getValue());
      }
    }
  }

  public FastMapLongObjectTimeSeries(final DateTimeNumericEncoding encoding, final SortedMap<Long, T> initialMap) {
    super(encoding);
    _map.putAll(initialMap);
  }

  @Override
  public long getEarliestTimeFast() {
    return _map.firstLongKey();
  }

  @Override
  public T getEarliestValueFast() {
    return _map.get(_map.firstLongKey());
  }

  @Override
  public long getLatestTimeFast() {
    return _map.lastLongKey();
  }

  @Override
  public T getLatestValueFast() {
    return _map.get(_map.lastLongKey());
  }

  @Override
  public long getTimeFast(final int index) {
    if (index >= _map.size()) {
      throw new NoSuchElementException();
    }
    final LongBidirectionalIterator iterator = _map.keySet().iterator();
    iterator.skip(index);
    return iterator.nextLong();
  }

  @Override
  public T getValueAtFast(final int index) {
    if (index >= _map.size() || index < 0) {
      throw new IndexOutOfBoundsException();
    }
    final LongBidirectionalIterator iterator = _map.keySet().iterator();
    iterator.skip(index);
    return _map.get(iterator.nextLong());
  }

  @Override
  public T getValueFast(final long time) {
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
  public FastLongObjectTimeSeries<T> subSeriesFast(final long startTime, final long endTime) {
    return new FastMapLongObjectTimeSeries<T>(getEncoding(), _map.subMap(startTime, endTime));
  }

  @Override
  public long[] timesArrayFast() {
    return _map.keySet().toLongArray();
  }

  @Override
  public LongIterator timesIteratorFast() {
    return _map.keySet().iterator();
  }

  @Override
  public LongList timesFast() {
    return new LongArrayList(_map.keySet());
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

  @Override
  public Iterator<Entry<Long, T>> iterator() {
    return _map.entrySet().iterator();
  }

  @SuppressWarnings("unchecked")
  @Override
  public ObjectIterator<Long2ObjectMap.Entry<T>> iteratorFast() {
    return (ObjectIterator<Long2ObjectMap.Entry<T>>) (ObjectIterator<? extends Map.Entry>) _map.entrySet().iterator();
  }

  @Override
  public FastLongObjectTimeSeries<T> headFast(final int numItems) {
    if (_map.size() < numItems) {
      throw new OpenGammaRuntimeException("cannot get head " + numItems + " items because there aren't that many in the series");
    }
    final LongBidirectionalIterator iterator = _map.keySet().iterator();
    iterator.skip(numItems);
    if (iterator.hasNext() && numItems != 0) {
      final long time = iterator.nextLong();
      return new FastMapLongObjectTimeSeries<T>(getEncoding(), _map.headMap(time + 1));
      // +1 means we get inclusive rather than exclusive.
    } else {
      return new FastMapLongObjectTimeSeries<T>(getEncoding());
    }
  }

  @Override
  public FastLongObjectTimeSeries<T> tailFast(final int numItems) {
    if (_map.size() < numItems) {
      throw new OpenGammaRuntimeException("cannot get head " + numItems + " items because there aren't that many in the series");
    }
    final LongBidirectionalIterator iterator = _map.keySet().iterator();
    iterator.skip(_map.size() - numItems);
    if (iterator.hasNext() && numItems != 0) {
      final long time = iterator.nextLong();
      return new FastMapLongObjectTimeSeries<T>(getEncoding(), _map.tailMap(time));
    } else {
      return new FastMapLongObjectTimeSeries<T>(getEncoding());
    }
  }

  @Override
  public ObjectTimeSeries<Long, T> head(final int numItems) {
    return headFast(numItems);
  }

  @Override
  public ObjectTimeSeries<Long, T> tail(final int numItems) {
    return tailFast(numItems);
  }

  @Override
  public void clear() {
    _map.clear();
  }

  @Override
  public void primitivePutDataPoint(final long time, final T value) {
    _map.put(time, value);
  }

  @Override
  public void primitiveRemoveDataPoint(final long time) {
    _map.remove(time);
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      if (obj instanceof FastLongObjectTimeSeries<?>) {
        final FastLongObjectTimeSeries<T> other = (FastLongObjectTimeSeries<T>) obj;
        if (size() != other.size()) {
          return false;
        }
        final ObjectIterator<Long2ObjectMap.Entry<T>> otherIterator = other.iteratorFast();
        final ObjectIterator<Long2ObjectMap.Entry<T>> myIterator = iteratorFast();
        final DateTimeNumericEncoding otherEncoding = other.getEncoding();
        final DateTimeNumericEncoding myEncoding = getEncoding();
        // invariant is that both are the same length as we got past the
        // values equality
        while (otherIterator.hasNext()) {
          final Long2ObjectMap.Entry<T> otherNext = otherIterator.next();
          final Long2ObjectMap.Entry<T> myNext = myIterator.next();
          if (!ObjectUtils.equals(otherNext.getValue(), myNext.getValue())) {
            return false;
          }
          if (otherEncoding.convertToLong(otherNext.getLongKey(), myEncoding) != myNext.getLongKey()) {
            return false;
          }
        }
      } else if (obj instanceof FastIntObjectTimeSeries<?>) {
        final FastIntObjectTimeSeries<T> other = (FastIntObjectTimeSeries<T>) obj;
        if (size() != other.size()) {
          return false;
        }
        final ObjectIterator<Int2ObjectMap.Entry<T>> otherIterator = other.iteratorFast();
        final ObjectIterator<Long2ObjectMap.Entry<T>> myIterator = iteratorFast();
        final DateTimeNumericEncoding otherEncoding = other.getEncoding();
        final DateTimeNumericEncoding myEncoding = getEncoding();
        // invariant is that both are the same length as we got past the
        // values equality
        while (otherIterator.hasNext()) {
          final Int2ObjectMap.Entry<T> otherNext = otherIterator.next();
          final Long2ObjectMap.Entry<T> myNext = myIterator.next();
          if (!ObjectUtils.equals(otherNext.getValue(), myNext.getValue())) {
            return false;
          }
          if (otherEncoding.convertToLong(otherNext.getIntKey(), myEncoding) != myNext.getLongKey()) {
            return false;
          }
        }
      } else {
        return false;
      }
    } else {
      final FastMapLongObjectTimeSeries<T> other = (FastMapLongObjectTimeSeries<T>) obj;
      // invariant: none of these can be null.
      if (_map.size() != other._map.size()) { // assuming the size is cheap to
        // know.
        return false;
      }
      if (other.getEncoding() != getEncoding()) {
        final ObjectIterator<Long2ObjectMap.Entry<T>> otherIterator = other.iteratorFast();
        final ObjectIterator<Long2ObjectMap.Entry<T>> myIterator = iteratorFast();
        final DateTimeNumericEncoding otherEncoding = other.getEncoding();
        final DateTimeNumericEncoding myEncoding = getEncoding();
        // invariant is that both are the same length as we got past the
        // values equality
        while (otherIterator.hasNext()) {
          final Long2ObjectMap.Entry<T> otherNext = otherIterator.next();
          final Long2ObjectMap.Entry<T> myNext = myIterator.next();
          if (!ObjectUtils.equals(otherNext, myNext)) {
            return false;
          }
          if (otherEncoding.convertToLong(otherNext.getLongKey(), myEncoding) != myNext.getLongKey()) {
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
  public FastLongObjectTimeSeries<T> newInstanceFast(final long[] times, final T[] values) {
    return new FastMapLongObjectTimeSeries<T>(getEncoding(), times, values);
  }
}
