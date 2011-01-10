/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.integer;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.Int2DoubleAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleSortedMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
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
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * @author jim
 * 
 */
public class FastMapIntDoubleTimeSeries extends AbstractFastMutableIntDoubleTimeSeries {

  Int2DoubleSortedMap _map = new Int2DoubleAVLTreeMap();
  final double DEFAULT_RETURN_VALUE = _map.defaultReturnValue();

  public FastMapIntDoubleTimeSeries(final DateTimeNumericEncoding encoding) {
    super(encoding);
  }

  public FastMapIntDoubleTimeSeries(final DateTimeNumericEncoding encoding, final int[] times, final double[] values) {
    super(encoding);
    if (times.length != values.length) {
      throw new OpenGammaRuntimeException("times and values arrays must be the same length");
    }
    for (int i = 0; i < times.length; i++) {
      _map.put(times[i], values[i]);
    }
  }

  public FastMapIntDoubleTimeSeries(final DateTimeNumericEncoding encoding, final List<Integer> times, final List<Double> values) {
    super(encoding);
    if (times.size() != values.size()) {
      throw new OpenGammaRuntimeException("times and values lists must be the same length");
    }
    final Iterator<Integer> timeIter = times.iterator();
    final Iterator<Double> valuesIter = values.iterator();
    while (timeIter.hasNext()) {
      _map.put(timeIter.next(), valuesIter.next());
    }
  }

  public FastMapIntDoubleTimeSeries(final FastIntDoubleTimeSeries dts) {
    super(dts.getEncoding());
    if (dts instanceof FastArrayIntDoubleTimeSeries || dts instanceof FastListIntDoubleTimeSeries) {
      // these are cheap to get arrays from.
      final int[] times = dts.timesArrayFast();
      final double[] values = dts.valuesArrayFast();
      for (int i = 0; i < times.length; i++) {
        _map.put(times[i], values[i]);
      }
    } else {
      final Iterator<Map.Entry<Integer, Double>> iter = dts.iterator();
      while (iter.hasNext()) {
        final Entry<Integer, Double> next = iter.next();
        _map.put(next.getKey(), next.getValue());
      }
    }
  }

  public FastMapIntDoubleTimeSeries(final DateTimeNumericEncoding encoding, final SortedMap<Integer, Double> initialMap) {
    super(encoding);
    _map.putAll(initialMap);
  }

  @Override
  public int getEarliestTimeFast() {
    return _map.firstIntKey();
  }

  @Override
  public double getEarliestValueFast() {
    return _map.get(_map.firstIntKey());
  }

  @Override
  public int getLatestTimeFast() {
    return _map.lastIntKey();
  }

  @Override
  public double getLatestValueFast() {
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
  public double getValueAtFast(final int index) {
    if (index >= _map.size()) {
      throw new IndexOutOfBoundsException();
    }
    final IntBidirectionalIterator iterator = _map.keySet().iterator();
    iterator.skip(index);
    return _map.get(iterator.nextInt());
  }

  @Override
  public double getValueFast(final int time) {
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
  public FastIntDoubleTimeSeries subSeriesFast(final int startTime, final int endTime) {
    return new FastMapIntDoubleTimeSeries(getEncoding(), _map.subMap(startTime, endTime));
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

  @SuppressWarnings("unchecked")
  @Override
  public ObjectIterator<Int2DoubleMap.Entry> iteratorFast() {
    return (ObjectIterator<Int2DoubleMap.Entry>) (ObjectIterator<? extends Entry<Integer, Double>>) _map.entrySet().iterator();
  }

  @Override
  public FastIntDoubleTimeSeries headFast(final int numItems) {
    if (_map.size() < numItems) {
      throw new OpenGammaRuntimeException("cannot get head " + numItems + " items because there aren't that many in the series");
    }
    final IntBidirectionalIterator iterator = _map.keySet().iterator();
    iterator.skip(numItems);
    if (iterator.hasNext() && numItems != 0) {
      final int time = iterator.nextInt();
      return new FastMapIntDoubleTimeSeries(getEncoding(), _map.headMap(time + 1));
      // +1 means we get inclusive rather than exclusive.
    } else {
      return new FastMapIntDoubleTimeSeries(getEncoding()); //empty series...
    }
  }

  @Override
  public FastIntDoubleTimeSeries tailFast(final int numItems) {
    if (_map.size() < numItems) {
      throw new OpenGammaRuntimeException("cannot get head " + numItems + " items because there aren't that many in the series");
    }
    final IntBidirectionalIterator iterator = _map.keySet().iterator();
    iterator.skip(_map.size() - numItems);
    if (iterator.hasNext() && numItems != 0) {
      final int time = iterator.nextInt();
      return new FastMapIntDoubleTimeSeries(getEncoding(), _map.tailMap(time));
    } else {
      return new FastMapIntDoubleTimeSeries(getEncoding());
    }
  }

  @Override
  public DoubleTimeSeries<Integer> head(final int numItems) {
    return headFast(numItems);
  }

  @Override
  public DoubleTimeSeries<Integer> tail(final int numItems) {
    return tailFast(numItems);
  }

  @Override
  public void clear() {
    _map.clear();
  }

  @Override
  public void primitivePutDataPoint(final int time, final double value) {
    _map.put(time, value);
  }

  @Override
  public void primitiveRemoveDataPoint(final int time) {
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
      if (obj instanceof FastIntDoubleTimeSeries) {
        final FastIntDoubleTimeSeries other = (FastIntDoubleTimeSeries) obj;
        if (size() != other.size()) {
          return false;
        }
        final ObjectIterator<Int2DoubleMap.Entry> otherIterator = other.iteratorFast();
        final ObjectIterator<Int2DoubleMap.Entry> myIterator = iteratorFast();
        final DateTimeNumericEncoding otherEncoding = other.getEncoding();
        final DateTimeNumericEncoding myEncoding = getEncoding();
        // invariant is that both are the same length as we got past the
        // values equality
        while (otherIterator.hasNext()) {
          final Int2DoubleMap.Entry otherNext = otherIterator.next();
          final Int2DoubleMap.Entry myNext = myIterator.next();
          if (!CompareUtils.closeEquals(otherNext.getDoubleValue(), myNext.getDoubleValue())) {
            return false;
          }
          if (otherEncoding.convertToInt(otherNext.getIntKey(), myEncoding) != myNext.getIntKey()) {
            return false;
          }
        }
      } else if (obj instanceof FastLongDoubleTimeSeries) {
        final FastLongDoubleTimeSeries other = (FastLongDoubleTimeSeries) obj;
        if (size() != other.size()) {
          return false;
        }
        final ObjectIterator<Long2DoubleMap.Entry> otherIterator = other.iteratorFast();
        final ObjectIterator<Int2DoubleMap.Entry> myIterator = iteratorFast();
        final DateTimeNumericEncoding otherEncoding = other.getEncoding();
        final DateTimeNumericEncoding myEncoding = getEncoding();
        // invariant is that both are the same length as we got past the
        // values equality
        while (otherIterator.hasNext()) {
          final Long2DoubleMap.Entry otherNext = otherIterator.next();
          final Int2DoubleMap.Entry myNext = myIterator.next();
          if (!CompareUtils.closeEquals(otherNext.getDoubleValue(), myNext.getDoubleValue())) {
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
      final FastMapIntDoubleTimeSeries other = (FastMapIntDoubleTimeSeries) obj;
      // invariant: none of these can be null.
      if (_map.size() != other._map.size()) { // assuming the size is cheap to
        // know.
        return false;
      }
      if (other.getEncoding() != getEncoding()) {
        final ObjectIterator<Int2DoubleMap.Entry> otherIterator = other.iteratorFast();
        final ObjectIterator<Int2DoubleMap.Entry> myIterator = iteratorFast();
        final DateTimeNumericEncoding otherEncoding = other.getEncoding();
        final DateTimeNumericEncoding myEncoding = getEncoding();
        // invariant is that both are the same length as we got past the
        // values equality
        while (otherIterator.hasNext()) {
          final Int2DoubleMap.Entry otherNext = otherIterator.next();
          final Int2DoubleMap.Entry myNext = myIterator.next();
          if (!CompareUtils.closeEquals(otherNext.getDoubleValue(), myNext.getDoubleValue())) {
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
  public FastIntDoubleTimeSeries newInstanceFast(final int[] times, final double[] values) {
    return new FastMapIntDoubleTimeSeries(getEncoding(), times, values);
  }
}
