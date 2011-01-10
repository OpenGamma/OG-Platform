/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.longint;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleSortedMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.Map.Entry;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.timeseries.AbstractFastBackedDoubleTimeSeries;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.FastBackedDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;

/**
 * @author jim
 * 
 */
public class FastMapLongDoubleTimeSeries extends AbstractFastMutableLongDoubleTimeSeries {

  Long2DoubleSortedMap _map = new Long2DoubleAVLTreeMap();
  final double DEFAULT_RETURN_VALUE = _map.defaultReturnValue();

  public FastMapLongDoubleTimeSeries(final DateTimeNumericEncoding encoding) {
    super(encoding);
  }

  public FastMapLongDoubleTimeSeries(final DateTimeNumericEncoding encoding, final long[] times, final double[] values) {
    super(encoding);
    if (times.length != values.length) {
      throw new OpenGammaRuntimeException("times and values arrays must be the same length");
    }
    for (int i = 0; i < times.length; i++) {
      _map.put(times[i], values[i]);
    }
  }

  public FastMapLongDoubleTimeSeries(final DateTimeNumericEncoding encoding, final List<Long> times, final List<Double> values) {
    super(encoding);
    if (times.size() != values.size()) {
      throw new OpenGammaRuntimeException("times and values lists must be the same length");
    }
    final Iterator<Long> timeIter = times.iterator();
    final Iterator<Double> valuesIter = values.iterator();
    while (timeIter.hasNext()) {
      _map.put(timeIter.next(), valuesIter.next());
    }
  }

  public FastMapLongDoubleTimeSeries(final FastLongDoubleTimeSeries dts) {
    super(dts.getEncoding());
    if (dts instanceof FastArrayLongDoubleTimeSeries || dts instanceof FastListLongDoubleTimeSeries) {
      // these are cheap to get arrays from.
      final long[] times = dts.timesArrayFast();
      final double[] values = dts.valuesArrayFast();
      for (int i = 0; i < times.length; i++) {
        _map.put(times[i], values[i]);
      }
    } else {
      final Iterator<Map.Entry<Long, Double>> iter = dts.iterator();
      while (iter.hasNext()) {
        final Entry<Long, Double> next = iter.next();
        _map.put(next.getKey(), next.getValue());
      }
    }
  }

  public FastMapLongDoubleTimeSeries(final DateTimeNumericEncoding encoding, final SortedMap<Long, Double> initialMap) {
    super(encoding);
    _map.putAll(initialMap);
  }

  @Override
  public long getEarliestTimeFast() {
    return _map.firstLongKey();
  }

  @Override
  public double getEarliestValueFast() {
    return _map.get(_map.firstLongKey());
  }

  @Override
  public long getLatestTimeFast() {
    return _map.lastLongKey();
  }

  @Override
  public double getLatestValueFast() {
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
  public double getValueAtFast(final int index) {
    if (index >= _map.size() || index < 0) {
      throw new IndexOutOfBoundsException();
    }
    final LongBidirectionalIterator iterator = _map.keySet().iterator();
    iterator.skip(index);
    return _map.get(iterator.nextLong());
  }

  @Override
  public double getValueFast(final long time) {
    double value = _map.get(time);
    if (value == DEFAULT_RETURN_VALUE) {
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
  public FastLongDoubleTimeSeries subSeriesFast(final long startTime, final long endTime) {
    return new FastMapLongDoubleTimeSeries(getEncoding(), _map.subMap(startTime, endTime));
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

  @Override
  public double[] valuesArrayFast() {
    return _map.values().toDoubleArray();
  }

  @Override
  public DoubleIterator valuesIteratorFast() {
    return _map.values().iterator();
  }

  @Override
  public DoubleList valuesFast() {
    return new DoubleArrayList(_map.values());
  }

  @Override
  public Iterator<Entry<Long, Double>> iterator() {
    return _map.entrySet().iterator();
  }

  @SuppressWarnings("unchecked")
  @Override
  public ObjectIterator<Long2DoubleMap.Entry> iteratorFast() {
    return (ObjectIterator<Long2DoubleMap.Entry>) (ObjectIterator<? extends Map.Entry>) _map.entrySet().iterator();
  }

  @Override
  public FastLongDoubleTimeSeries headFast(final int numItems) {
    if (_map.size() < numItems) {
      throw new OpenGammaRuntimeException("cannot get head " + numItems + " items because there aren't that many in the series");
    }
    final LongBidirectionalIterator iterator = _map.keySet().iterator();
    iterator.skip(numItems);
    if (iterator.hasNext() && numItems != 0) {
      final long time = iterator.nextLong();
      return new FastMapLongDoubleTimeSeries(getEncoding(), _map.headMap(time + 1));
      // +1 means we get inclusive rather than exclusive.
    } else {
      return new FastMapLongDoubleTimeSeries(getEncoding());
    }
  }

  @Override
  public FastLongDoubleTimeSeries tailFast(final int numItems) {
    if (_map.size() < numItems) {
      throw new OpenGammaRuntimeException("cannot get head " + numItems + " items because there aren't that many in the series");
    }
    final LongBidirectionalIterator iterator = _map.keySet().iterator();
    iterator.skip(_map.size() - numItems);
    if (iterator.hasNext() && numItems != 0) {
      final long time = iterator.nextLong();
      return new FastMapLongDoubleTimeSeries(getEncoding(), _map.tailMap(time));
    } else {
      return new FastMapLongDoubleTimeSeries(getEncoding());
    }
  }

  @Override
  public DoubleTimeSeries<Long> head(final int numItems) {
    return headFast(numItems);
  }

  @Override
  public DoubleTimeSeries<Long> tail(final int numItems) {
    return tailFast(numItems);
  }

  @Override
  public void clear() {
    _map.clear();
  }

  @Override
  public void primitivePutDataPoint(final long time, final double value) {
    _map.put(time, value);
  }

  @Override
  public void primitiveRemoveDataPoint(final long time) {
    _map.remove(time);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      if (obj instanceof FastLongDoubleTimeSeries) {
        final FastLongDoubleTimeSeries other = (FastLongDoubleTimeSeries) obj;
        if (size() != other.size()) {
          return false;
        }
        final ObjectIterator<Long2DoubleMap.Entry> otherIterator = other.iteratorFast();
        final ObjectIterator<Long2DoubleMap.Entry> myIterator = iteratorFast();
        final DateTimeNumericEncoding otherEncoding = other.getEncoding();
        final DateTimeNumericEncoding myEncoding = getEncoding();
        // invariant is that both are the same length as we got past the
        // values equality
        while (otherIterator.hasNext()) {
          final Long2DoubleMap.Entry otherNext = otherIterator.next();
          final Long2DoubleMap.Entry myNext = myIterator.next();
          if (!CompareUtils.closeEquals(otherNext.getDoubleValue(), myNext.getDoubleValue())) {
            return false;
          }
          if (otherEncoding.convertToLong(otherNext.getLongKey(), myEncoding) != myNext.getLongKey()) {
            return false;
          }
        }
      } else if (obj instanceof FastIntDoubleTimeSeries) {
        final FastIntDoubleTimeSeries other = (FastIntDoubleTimeSeries) obj;
        if (size() != other.size()) {
          return false;
        }
        final ObjectIterator<Int2DoubleMap.Entry> otherIterator = other.iteratorFast();
        final ObjectIterator<Long2DoubleMap.Entry> myIterator = iteratorFast();
        final DateTimeNumericEncoding otherEncoding = other.getEncoding();
        final DateTimeNumericEncoding myEncoding = getEncoding();
        // invariant is that both are the same length as we got past the
        // values equality
        while (otherIterator.hasNext()) {
          final Int2DoubleMap.Entry otherNext = otherIterator.next();
          final Long2DoubleMap.Entry myNext = myIterator.next();
          if (!CompareUtils.closeEquals(otherNext.getDoubleValue(), myNext.getDoubleValue())) {
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
      final FastMapLongDoubleTimeSeries other = (FastMapLongDoubleTimeSeries) obj;
      // invariant: none of these can be null.
      if (_map.size() != other._map.size()) { // assuming the size is cheap to
        // know.
        return false;
      }
      if (other.getEncoding() != getEncoding()) {
        final ObjectIterator<Long2DoubleMap.Entry> otherIterator = other.iteratorFast();
        final ObjectIterator<Long2DoubleMap.Entry> myIterator = iteratorFast();
        final DateTimeNumericEncoding otherEncoding = other.getEncoding();
        final DateTimeNumericEncoding myEncoding = getEncoding();
        // invariant is that both are the same length as we got past the
        // values equality
        while (otherIterator.hasNext()) {
          final Long2DoubleMap.Entry otherNext = otherIterator.next();
          final Long2DoubleMap.Entry myNext = myIterator.next();
          if (!CompareUtils.closeEquals(otherNext.getDoubleValue(), myNext.getDoubleValue())) {
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
  public FastLongDoubleTimeSeries newInstanceFast(final long[] times, final double[] values) {
    return new FastMapLongDoubleTimeSeries(getEncoding(), times, values);
  }
}
