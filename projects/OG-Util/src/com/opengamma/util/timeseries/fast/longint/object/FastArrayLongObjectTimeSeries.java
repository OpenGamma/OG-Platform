/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.longint.object;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.SortedMap;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.object.FastIntObjectTimeSeries;
import com.opengamma.util.tuple.LongObjectPair;

/**
 * 
 * @param <T> The type of the data
 */
public class FastArrayLongObjectTimeSeries<T> extends AbstractFastLongObjectTimeSeries<T> {
  private final long[] _times;
  private final T[] _values;

  @SuppressWarnings("unchecked")
  public FastArrayLongObjectTimeSeries(final DateTimeNumericEncoding encoding) {
    super(encoding);
    _times = new long[0];
    _values = (T[]) new Object[0];
  }

  @SuppressWarnings("unchecked")
  public FastArrayLongObjectTimeSeries(final DateTimeNumericEncoding encoding, final Long[] times, final T[] values) {
    super(encoding);
    _times = new long[times.length];
    _values = (T[]) new Object[values.length];
    init(ArrayUtils.toPrimitive(times), values);
  }

  @SuppressWarnings("unchecked")
  public FastArrayLongObjectTimeSeries(final DateTimeNumericEncoding encoding, final long[] times, final T[] values) {
    super(encoding);
    _times = new long[times.length];
    _values = (T[]) new Object[values.length];
    init(times, values);
  }

  private void init(final long[] times, final T[] values) {
    if (times.length != values.length) {
      throw new IllegalArgumentException("Arrays are of different sizes: " + times.length + ", " + values.length);
    }
    System.arraycopy(times, 0, _times, 0, times.length);
    System.arraycopy(values, 0, _values, 0, values.length);
    // check dates are ordered
    long maxTime = Long.MIN_VALUE;
    for (final long time : _times) {
      if (time < maxTime) {
        throw new IllegalArgumentException("dates must be ordered");
      }
      maxTime = time;
    }
  }

  @SuppressWarnings("unchecked")
  public FastArrayLongObjectTimeSeries(final DateTimeNumericEncoding encoding, final List<Long> times,
      final List<T> values) {
    super(encoding);
    if (times.size() != values.size()) {
      throw new IllegalArgumentException("lists are of different sizes");
    }
    _times = new long[times.size()];
    _values = (T[]) new Object[values.size()];
    final Iterator<T> iter = values.iterator();
    int i = 0;
    long maxTime = Long.MIN_VALUE; // for checking the dates are sorted.
    for (final long time : times) {
      final T value = iter.next();
      if (maxTime < time) {
        _times[i] = time;
        _values[i] = value;
        maxTime = time;
      } else {
        throw new IllegalArgumentException("dates must be ordered");
      }
      i++;
    }
  }

  public FastArrayLongObjectTimeSeries(final FastLongObjectTimeSeries<T> dts) {
    super(dts.getEncoding());
    _times = dts.timesArrayFast();
    _values = dts.valuesArrayFast();
  }

  public FastArrayLongObjectTimeSeries(DateTimeNumericEncoding encoding, final FastLongObjectTimeSeries<T> dts) {
    super(dts.getEncoding());
    DateTimeNumericEncoding otherEncoding = dts.getEncoding();
    _times = dts.timesArrayFast();
    for (int i = 0; i < _times.length; i++) {
      _times[i] = otherEncoding.convertToLong(_times[i], encoding);
    }
    _values = dts.valuesArrayFast();
  }

  public FastArrayLongObjectTimeSeries(final FastIntObjectTimeSeries<T> dts) {
    super(dts.getEncoding());
    DateTimeNumericEncoding encoding = dts.getEncoding();
    int[] timesArrayFast = dts.timesArrayFast();
    _values = dts.valuesArrayFast();
    _times = new long[timesArrayFast.length];
    for (int i = 0; i < timesArrayFast.length; i++) {
      _times[i] = encoding.convertToLong(timesArrayFast[i], encoding);
    }
  }

  public FastArrayLongObjectTimeSeries(DateTimeNumericEncoding encoding, final FastIntObjectTimeSeries<T> dts) {
    super(encoding);
    DateTimeNumericEncoding otherEncoding = dts.getEncoding();
    int[] timesArrayFast = dts.timesArrayFast();
    _values = dts.valuesArrayFast();
    _times = new long[timesArrayFast.length];
    for (int i = 0; i < timesArrayFast.length; i++) {
      _times[i] = otherEncoding.convertToLong(timesArrayFast[i], encoding);
    }
  }

  @SuppressWarnings("unchecked")
  public FastArrayLongObjectTimeSeries(final DateTimeNumericEncoding encoding, final SortedMap<Long, T> initialMap) {
    super(encoding);
    final int size = initialMap.size();
    _times = new long[size];
    _values = (T[]) new Object[size];
    final Iterator<Entry<Long, T>> iterator = initialMap.entrySet().iterator();
    int i = 0;
    while (iterator.hasNext()) {
      final Entry<Long, T> entry = iterator.next();
      _times[i] = entry.getKey().longValue();
      _values[i] = entry.getValue();
      i++;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public FastLongObjectTimeSeries<T> subSeriesFast(final long startTime, final long endTime) {
    if (isEmpty()) {
      return new FastArrayLongObjectTimeSeries<T>(getEncoding(), new long[0], (T[]) new Object[0]);
    }
    // throw new NoSuchElementException("Series is empty")
    int startPos = Arrays.binarySearch(_times, startTime);
    int endPos = Arrays.binarySearch(_times, endTime);
    // if either is -1, make it zero
    startPos = startPos >= 0 ? startPos : -startPos - 1;
    endPos = endPos >= 0 ? endPos : -endPos - 1;
    final int length = endPos - startPos;
    final long[] resultTimes = new long[length];
    final T[] resultValues = (T[]) new Object[length];
    System.arraycopy(_times, startPos, resultTimes, 0, length);
    System.arraycopy(_values, startPos, resultValues, 0, length);
    return new FastArrayLongObjectTimeSeries<T>(getEncoding(), resultTimes, resultValues);
  }

  public T getDataPointFast(final long time) {
    final int index = Arrays.binarySearch(_times, time);
    if (index >= 0) {
      return _values[index];
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public long getEarliestTimeFast() {
    if (_times.length > 0) {
      return _times[0];
    } else {
      throw new NoSuchElementException("Series is empty");
    }
  }

  @Override
  public T getEarliestValueFast() {
    if (_values.length > 0) {
      return _values[0];
    } else {
      throw new NoSuchElementException("Series is empty");
    }
  }

  @Override
  public long getLatestTimeFast() {
    if (_times.length > 0) {
      final int index = _times.length - 1;
      return _times[index];
    } else {
      throw new NoSuchElementException("Series is empty");
    }
  }

  @Override
  public T getLatestValueFast() {
    if (_values.length > 0) {
      return _values[_values.length - 1];
    } else {
      throw new NoSuchElementException("Series is empty");
    }
  }

  /* package */class PrimitiveArrayObjectTimeSeriesIterator implements ObjectIterator<Long2ObjectMap.Entry<T>> {
    private int _current;

    @Override
    public boolean hasNext() {
      return _current < _times.length;
    }

    @Override
    public Long2ObjectMap.Entry<T> next() {
      if (hasNext()) {
        final Long2ObjectMap.Entry<T> keyValuePair = new LongObjectPair<T>(_times[_current], _values[_current]);
        _current++;
        return keyValuePair;
      } else {
        throw new NoSuchElementException();
      }
    }

    @Override
    public int skip(final int n) {
      final int skipped = n > (_times.length - _current) ? _times.length - _current : n;
      _current += n;
      if (_current >= _times.length) {
        _current = _times.length;
      }
      return skipped;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public int size() {
    return _times.length;
  }

  @Override
  public boolean isEmpty() {
    return _times.length == 0;
  }

  /* package */class PrimitiveArrayObjectTimeSeriesTimesIterator implements LongIterator {
    private int _current;

    @Override
    public boolean hasNext() {
      return _current < _times.length;
    }

    @Override
    public Long next() {
      if (hasNext()) {
        final long time = _times[_current];
        _current++;
        return time;
      } else {
        throw new NoSuchElementException();
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    @Override
    public long nextLong() {
      if (hasNext()) {
        final long time = _times[_current];
        _current++;
        return time;
      } else {
        throw new NoSuchElementException();
      }
    }

    @Override
    public int skip(final int n) {
      final int skipped = n > (_times.length - _current) ? _times.length - _current : n;
      _current += n;
      if (_current >= _times.length) {
        _current = _times.length;
      }
      return skipped;
    }
  }

  /* package */class PrimitiveArrayObjectTimeSeriesValuesIterator implements ObjectIterator<T> {
    private int _current;

    @Override
    public boolean hasNext() {
      return _current < _values.length;
    }

    @Override
    public T next() {
      if (hasNext()) {
        final T value = _values[_current];
        _current++;
        return value;
      } else {
        throw new NoSuchElementException();
      }
    }

    public T nextT() {
      if (hasNext()) {
        final T value = _values[_current];
        _current++;
        return value;
      } else {
        throw new NoSuchElementException();
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    @Override
    public int skip(final int n) {
      final int skipped = n > (_times.length - _current) ? _times.length - _current : n;
      _current += n;
      if (_current >= _times.length) {
        _current = _times.length;
      }
      return skipped;
    }
  }

  @Override
  public ObjectIterator<T> valuesIteratorFast() {
    return new PrimitiveArrayObjectTimeSeriesValuesIterator();
  }

  public T[] valuesArrayFast() {
    return _values.clone();
  }

  public long[] timesArrayFast() {
    return _times.clone();
  }

  @Override
  public long getTimeFast(final int index) {
    return _times[index];
  }

  @SuppressWarnings("unchecked")
  public FastLongObjectTimeSeries<T> tail(final int numItems) {
    if (numItems <= _times.length) {
      final long[] times = new long[numItems];
      final T[] values = (T[]) new Object[numItems];
      System.arraycopy(_times, _times.length - numItems, times, 0, numItems);
      System.arraycopy(_values, _values.length - numItems, values, 0, numItems);
      return new FastArrayLongObjectTimeSeries<T>(getEncoding(), times, values);
    } else {
      throw new NoSuchElementException("Not enough elements");
    }
  }

  @Override
  public FastLongObjectTimeSeries<T> tailFast(final int numItems) {
    return tail(numItems);
  }

  @SuppressWarnings("unchecked")
  public FastLongObjectTimeSeries<T> head(final int numItems) {
    if (numItems <= _times.length) {
      final long[] times = new long[numItems];
      final T[] values = (T[]) new Object[numItems];
      System.arraycopy(_times, 0, times, 0, numItems);
      System.arraycopy(_values, 0, values, 0, numItems);
      return new FastArrayLongObjectTimeSeries<T>(getEncoding(), times, values);
    } else {
      throw new NoSuchElementException("Not enough elements");
    }
  }

  @Override
  public FastLongObjectTimeSeries<T> headFast(final int numItems) {
    return head(numItems);
  }

  /*
   * Note that this is so complicated to try and provide optimal performance. A
   * much slower version would be quite short.
   */
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
        if (!Arrays.equals(other.valuesArrayFast(), _values)) {
          return false;
        }
        if (other.getEncoding().equals(getEncoding())) {
          return Arrays.equals(other.timesArrayFast(), _times);
        } else {
          final LongIterator otherTimesIterator = other.timesIteratorFast();
          int i = 0;
          final DateTimeNumericEncoding otherEncoding = other.getEncoding();
          final DateTimeNumericEncoding myEncoding = getEncoding();
          // invarient is that both are the same length as we got past the
          // values equality
          while (otherTimesIterator.hasNext()) {
            if (otherEncoding.convertToLong(otherTimesIterator.nextLong(), myEncoding) != _times[i]) {
              return false;
            }
            i++;
          }
        }
      } else if (obj instanceof FastIntObjectTimeSeries<?>) {
        final FastIntObjectTimeSeries<T> other = (FastIntObjectTimeSeries<T>) obj;
        if (!Arrays.equals(other.valuesArrayFast(), _values)) {
          return false;
        }
        final IntIterator otherTimesIterator = other.timesIteratorFast();
        int i = 0;
        final DateTimeNumericEncoding otherEncoding = other.getEncoding();
        final DateTimeNumericEncoding myEncoding = getEncoding();
        // invarient is that both are the same length as we got past the
        // values equality
        while (otherTimesIterator.hasNext()) {
          if (otherEncoding.convertToLong(otherTimesIterator.nextInt(), myEncoding) != _times[i]) {
            return false;
          }
          i++;
        }
      } else {
        return false;
      }
    } else {
      final FastArrayLongObjectTimeSeries<?> other = (FastArrayLongObjectTimeSeries<?>) obj;
      // invariant: none of these can be null.
      if (size() != other.size()) { // should always be O(1)
        return false;
      }
      if (!Arrays.equals(_values, other._values)) {
        return false;
      }
      if (other.getEncoding() == getEncoding()) {
        if (!Arrays.equals(_times, other._times)) {
          return false;
        }
      } else {
        // encoding of other is different, must check...
        // invariant: other.size() == _times.size();
        final long[] myTimes = _times;
        final long[] otherTimes = other._times;
        final DateTimeNumericEncoding encoding = other.getEncoding();
        final DateTimeNumericEncoding myEncoding = getEncoding();
        for (int i = 0; i < myTimes.length; i++) {
          if (myTimes[i] != encoding.convertToLong(otherTimes[i], myEncoding)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  @Override
  public LongList timesFast() {
    return new LongArrayList(_times);
  }

  @Override
  public ObjectList<T> valuesFast() {
    return new ObjectArrayList<T>(_values);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(_values);
  }

  @Override
  public T getValueFast(final long time) {
    final int binarySearch = Arrays.binarySearch(_times, time);
    if (binarySearch >= 0 && _times[binarySearch] == time) {
      return _values[binarySearch];
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public T getValueAtFast(final int index) {
    return _values[index];
  }

  @SuppressWarnings("unchecked")
  @Override
  public Iterator<Entry<Long, T>> iterator() {
    return (Iterator<Entry<Long, T>>) (Iterator<? extends Entry<Long, T>>) new PrimitiveArrayObjectTimeSeriesIterator();
  }

  @Override
  public ObjectIterator<Long2ObjectMap.Entry<T>> iteratorFast() {
    return new PrimitiveArrayObjectTimeSeriesIterator();
  }

  @Override
  public ObjectTimeSeries<Long, T> subSeries(final Long startTime, final Long endTime) {
    return subSeriesFast(startTime, endTime);
  }

  @Override
  public LongIterator timesIteratorFast() {
    return new PrimitiveArrayObjectTimeSeriesTimesIterator();
  }

  @Override
  public FastLongObjectTimeSeries<T> newInstanceFast(final long[] times, final T[] values) {
    return new FastArrayLongObjectTimeSeries<T>(getEncoding(), times, values);
  }

}
