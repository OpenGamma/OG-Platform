/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.longint;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.SortedMap;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.tuple.LongDoublePair;

/**
 * 
 */
public class FastListLongDoubleTimeSeries extends AbstractFastMutableLongDoubleTimeSeries {
  private final LongArrayList _times;
  private final DoubleArrayList _values;

  public FastListLongDoubleTimeSeries(final DateTimeNumericEncoding encoding) {
    super(encoding);
    _times = new LongArrayList();
    _values = new DoubleArrayList();
  }

  public FastListLongDoubleTimeSeries(final DateTimeNumericEncoding encoding, final long[] times, final double[] values) {
    super(encoding);
    _times = new LongArrayList(times);
    _values = new DoubleArrayList(values);
    ensureTimesSorted();
  }

  public FastListLongDoubleTimeSeries(final DateTimeNumericEncoding encoding, final List<Long> times,
      final List<Double> values) {
    super(encoding);
    _times = new LongArrayList(times);
    _values = new DoubleArrayList(values);
    ensureTimesSorted();
  }

  public FastListLongDoubleTimeSeries(final FastLongDoubleTimeSeries dts) {
    super(dts.getEncoding());
    _times = new LongArrayList(dts.timesArrayFast());
    _values = new DoubleArrayList(dts.valuesArrayFast());
    // don't need to check here
  }

  public FastListLongDoubleTimeSeries(DateTimeNumericEncoding encoding, final FastLongDoubleTimeSeries dts) {
    super(encoding);
    long[] timesArrayFast = dts.timesArrayFast(); // NOTE: we can't do it this way if we change to returning the backing array.
    DateTimeNumericEncoding sourceEncoding = dts.getEncoding();
    for (int i = 0; i < timesArrayFast.length; i++) {
      timesArrayFast[i] = sourceEncoding.convertToLong(timesArrayFast[i], encoding);
    }
    _times = new LongArrayList(timesArrayFast);
    _values = new DoubleArrayList(dts.valuesArrayFast());
  }

  public FastListLongDoubleTimeSeries(final FastIntDoubleTimeSeries dts) {
    super(dts.getEncoding());
    int[] timesArrayFast = dts.timesArrayFast();
    _times = new LongArrayList();
    for (int i = 0; i < timesArrayFast.length; i++) {
      _times.add(getEncoding().convertToLong(timesArrayFast[i], getEncoding()));
    }
    _values = new DoubleArrayList(dts.valuesArrayFast());
    // don't need to check here
  }

  public FastListLongDoubleTimeSeries(DateTimeNumericEncoding encoding, final FastIntDoubleTimeSeries dts) {
    super(dts.getEncoding());
    DateTimeNumericEncoding sourceEncoding = dts.getEncoding();
    int[] timesArrayFast = dts.timesArrayFast();
    _times = new LongArrayList();
    for (int i = 0; i < timesArrayFast.length; i++) {
      _times.add(sourceEncoding.convertToLong(timesArrayFast[i], getEncoding()));
    }
    _values = new DoubleArrayList(dts.valuesArrayFast());
    // don't need to check here
  }

  public FastListLongDoubleTimeSeries(final DateTimeNumericEncoding encoding, final SortedMap<Long, Double> initialMap) {
    super(encoding);
    _times = new LongArrayList(initialMap.size());
    _values = new DoubleArrayList(initialMap.size());
    for (final Map.Entry<Long, Double> entry : initialMap.entrySet()) {
      _times.add(entry.getKey());
      _values.add(entry.getValue());
    }
  }

  private void ensureTimesSorted() {
    final LongIterator iter = _times.iterator();
    long current = Long.MIN_VALUE;
    while (iter.hasNext()) {
      final long next = iter.nextLong();
      if (next < current) {
        throw new OpenGammaRuntimeException("Times not in order");
      }
      current = next;
    }
  }

  @Override
  public long getEarliestTimeFast() {
    if (_times.size() > 0) {
      return _times.getLong(0);
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public double getEarliestValueFast() {
    if (_values.size() > 0) {
      return _values.getDouble(0);
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public long getLatestTimeFast() {
    if (_times.size() > 0) {
      return _times.getLong(_times.size() - 1);
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public double getLatestValueFast() {
    if (_values.size() > 0) {
      return _values.getDouble(_times.size() - 1);
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public long getTimeFast(final int index) {
    return _times.getLong(index);
  }

  @Override
  public double getValueAtFast(final int index) {
    return _values.getDouble(index);
  }

  @Override
  public double getValueFast(final long time) {
    final int index = _times.indexOf(time);
    if (index >= 0) {
      return _values.getDouble(index);
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public boolean isEmpty() {
    return _values.isEmpty();
  }

  @Override
  public int size() {
    return _values.size();
  }

  @Override
  public FastLongDoubleTimeSeries subSeriesFast(final long startTime, final long endTime) {
    int startIndex = _times.indexOf(startTime);
    int endIndex = _times.indexOf(endTime);
    if (startIndex == -1) {
      startIndex = -(Collections.binarySearch(_times, startTime) + 1); // insertion point
    }
    if (endIndex == -1) {
      endIndex = -(Collections.binarySearch(_times, endTime) + 1);
    }
    if (startIndex == -1 || endIndex == -1) {
      throw new NoSuchElementException();
    }
    if (startIndex == -1 || endIndex == -1) {
      throw new NoSuchElementException();
    }
    return new FastListLongDoubleTimeSeries(getEncoding(), _times.subList(startIndex, endIndex), _values.subList(
        startIndex, endIndex));
  }

  @Override
  public long[] timesArrayFast() {
    return _times.toLongArray();
  }

  @Override
  public LongIterator timesIteratorFast() {
    return _times.iterator();
  }

  @Override
  public LongList timesFast() {
    return new LongArrayList(_times);
  }

  @Override
  public double[] valuesArrayFast() {
    return _values.toDoubleArray();
  }

  @Override
  public DoubleIterator valuesIteratorFast() {
    return _values.iterator();
  }

  @Override
  public DoubleList valuesFast() {
    return new DoubleArrayList(_values);
  }

  private class PrimitiveListLongDoubleTimeSeriesIterator implements ObjectIterator<Long2DoubleMap.Entry> {
    private LongIterator _longIter;
    private DoubleIterator _doubleIter;

    public PrimitiveListLongDoubleTimeSeriesIterator() {
      _longIter = _times.iterator();
      _doubleIter = _values.iterator();
    }

    @Override
    public boolean hasNext() {
      return _longIter.hasNext();
    }

    @Override
    public Long2DoubleMap.Entry next() {
      final long time = _longIter.nextLong();
      final double value = _doubleIter.nextDouble();
      return new LongDoublePair(time, value);
    }

    @Override
    public void remove() {
      _longIter.remove();
      _doubleIter.remove();
    }

    @Override
    public int skip(final int n) {
      _longIter.skip(n);
      return _doubleIter.skip(n);
    }

  }

  @SuppressWarnings("unchecked")
  @Override
  public Iterator<Entry<Long, Double>> iterator() {
    return (Iterator<Entry<Long, Double>>) (Iterator<? extends Entry<Long, Double>>) new PrimitiveListLongDoubleTimeSeriesIterator();
  }

  @Override
  public ObjectIterator<Long2DoubleMap.Entry> iteratorFast() {
    return new PrimitiveListLongDoubleTimeSeriesIterator();
  }

  // REVIEW: jim 15-Feb-2010 -- should these be here (head and tail)?

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
    _times.clear();
    _values.clear();
  }

  @Override
  public void primitivePutDataPoint(final long time, final double value) {
    final int index = Arrays.binarySearch(_times.elements(), 0, _times.size(), time);  // raw elements() may be larger than size
    if (index >= 0) {
      _values.set(index, value);
    } else {
      _times.add(-(index + 1), time);
      _values.add(-(index + 1), value);
    }
  }

  @Override
  public void primitiveRemoveDataPoint(final long time) {
    final int index = Arrays.binarySearch(_times.elements(), 0, _times.size(), time);  // raw elements() may be larger than size
    if (index >= 0) {
      _times.remove(index);
      _values.remove(index);
    }
  }

  @Override
  public FastLongDoubleTimeSeries headFast(final int numItems) {
    return new FastListLongDoubleTimeSeries(getEncoding(), _times.subList(0, numItems), _values.subList(0, numItems));
  }

  @Override
  public FastLongDoubleTimeSeries tailFast(final int numItems) {
    // note I used _times.size for the second part so it we didn't need two
    // method calls as the optimizer is unlikely to spot it.
    return new FastListLongDoubleTimeSeries(getEncoding(), _times.subList(_times.size() - numItems, _times.size()),
        _values.subList(_times.size() - numItems, _times.size()));
  }

  /**
   * {@inheritDoc}
   * Note that this is so complicated to try and provide optimal performance. A
   * much slower version would be quite short.
   */
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
        if (!Arrays.equals(other.valuesArrayFast(), _values.elements())) {
          return false;
        }
        if (other.getEncoding().equals(getEncoding())) {
          return Arrays.equals(other.timesArrayFast(), timesArrayFast());
        } else {
          final LongIterator otherTimesIterator = other.timesIteratorFast();
          final LongIterator myTimesIterator = _times.iterator();
          final DateTimeNumericEncoding otherEncoding = other.getEncoding();
          final DateTimeNumericEncoding myEncoding = getEncoding();
          // invarient is that both are the same length as we got past the
          // values equality
          while (otherTimesIterator.hasNext()) {
            if (otherEncoding.convertToLong(otherTimesIterator.nextLong(), myEncoding) != myTimesIterator.next()) {
              return false;
            }
          }
        }
      } else if (obj instanceof FastIntDoubleTimeSeries) {
        final FastIntDoubleTimeSeries other = (FastIntDoubleTimeSeries) obj;
        if (!Arrays.equals(other.valuesArrayFast(), _values.elements())) {
          return false;
        }
        final IntIterator otherTimesIterator = other.timesIteratorFast();
        final LongIterator myTimesIterator = _times.iterator();
        final DateTimeNumericEncoding otherEncoding = other.getEncoding();
        final DateTimeNumericEncoding myEncoding = getEncoding();
        // invarient is that both are the same length as we got past the
        // values equality
        while (otherTimesIterator.hasNext()) {
          if (otherEncoding.convertToLong(otherTimesIterator.nextInt(), myEncoding) != myTimesIterator.next()) {
            return false;
          }
        }
      } else {
        return false;
      }
    } else {
      final FastListLongDoubleTimeSeries other = (FastListLongDoubleTimeSeries) obj;
      // invariant: none of these can be null.
      if (size() != other.size()) {
        return false;
      }
      if (!_values.equals(other._values)) {
        return false;
      }
      if (other.getEncoding() == getEncoding()) {
        if (!_times.equals(other._times)) {
          return false;
        }
      } else {
        // encoding of other is different, must check...
        // invariant: other.size() == _times.size();
        final long[] myTimes = _times.elements();  // raw elements() may be larger than size
        final long[] otherTimes = other._times.elements();  // raw elements() may be larger than size
        final DateTimeNumericEncoding encoding = other.getEncoding();
        final DateTimeNumericEncoding myEncoding = getEncoding();
        for (int i = 0; i < size(); i++) {
          if (myTimes[i] != encoding.convertToLong(otherTimes[i], myEncoding)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    return _values.hashCode();
  }

  @Override
  public FastLongDoubleTimeSeries newInstanceFast(final long[] times, final double[] values) {
    return new FastListLongDoubleTimeSeries(getEncoding(), times, values);
  }

}
