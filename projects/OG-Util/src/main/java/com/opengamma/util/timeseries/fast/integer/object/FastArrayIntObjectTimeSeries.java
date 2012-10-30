/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.integer.object;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongIterator;
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
import com.opengamma.util.timeseries.fast.longint.object.FastLongObjectTimeSeries;
import com.opengamma.util.tuple.IntObjectPair;

/**
 * Time series.
 * @param <T>  the type
 */
public class FastArrayIntObjectTimeSeries<T> extends AbstractFastIntObjectTimeSeries<T> {
  /** Empty time series. */
  public static final FastIntObjectTimeSeries<Object> EMPTY_SERIES = new FastArrayIntObjectTimeSeries<Object>(
      DateTimeNumericEncoding.TIME_EPOCH_MILLIS);

  /** The times. */
  private final int[] _times;
  /** The values. */
  private final T[] _values;

  @SuppressWarnings("unchecked")
  public FastArrayIntObjectTimeSeries(final DateTimeNumericEncoding encoding) {
    super(encoding);
    _times = new int[0];
    _values = (T[]) new Object[0];
  }

  @SuppressWarnings("unchecked")
  public FastArrayIntObjectTimeSeries(final DateTimeNumericEncoding encoding, final Integer[] times, final T[] values) {
    super(encoding);
    _times = new int[times.length];
    _values = (T[]) new Object[values.length];
    init(ArrayUtils.toPrimitive(times), values);
  }

  @SuppressWarnings("unchecked")
  public FastArrayIntObjectTimeSeries(final DateTimeNumericEncoding encoding, final int[] times, final T[] values) {
    super(encoding);
    _times = new int[times.length];
    _values = (T[]) new Object[values.length];
    init(times, values);
  }

  private void init(final int[] times, final T[] values) {
    if (times.length != values.length) {
      throw new IllegalArgumentException("Arrays are of different sizes: " + times.length + ", " + values.length);
    }
    System.arraycopy(times, 0, _times, 0, times.length);
    System.arraycopy(values, 0, _values, 0, values.length);
    // check dates are ordered
    int maxTime = Integer.MIN_VALUE;
    for (final int time : _times) {
      if (time < maxTime) {
        throw new IllegalArgumentException("dates must be ordered");
      }
      maxTime = time;
    }
  }

  @SuppressWarnings("unchecked")
  public FastArrayIntObjectTimeSeries(final DateTimeNumericEncoding encoding, final List<Integer> times,
      final List<T> values) {
    super(encoding);
    if (times.size() != values.size()) {
      throw new IllegalArgumentException("lists are of different sizes");
    }
    _times = new int[times.size()];
    _values = (T[]) new Object[values.size()];
    final Iterator<T> iter = values.iterator();
    int i = 0;
    int maxTime = Integer.MIN_VALUE; // for checking the dates are sorted.
    for (final int time : times) {
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

  public FastArrayIntObjectTimeSeries(final FastIntObjectTimeSeries<T> dts) {
    super(dts.getEncoding());
    _times = dts.timesArrayFast();
    _values = dts.valuesArrayFast();
  }

  public FastArrayIntObjectTimeSeries(final DateTimeNumericEncoding encoding, final FastIntObjectTimeSeries<T> dts) {
    super(encoding);
    DateTimeNumericEncoding sourceEncoding = dts.getEncoding();
    _times = dts.timesArrayFast();
    for (int i = 0; i < _times.length; i++) {
      _times[i] = sourceEncoding.convertToInt(_times[i], encoding);
    }
    _values = dts.valuesArrayFast();
  }

  public FastArrayIntObjectTimeSeries(final FastLongObjectTimeSeries<T> dts) {
    this(dts.getEncoding(), dts);
  }

  public FastArrayIntObjectTimeSeries(final DateTimeNumericEncoding encoding, final FastLongObjectTimeSeries<T> dts) {
    super(encoding);
    DateTimeNumericEncoding otherEncoding = dts.getEncoding();
    long[] otherTimes = dts.timesArrayFast();
    _times = new int[otherTimes.length];
    for (int i = 0; i < otherTimes.length; i++) {
      _times[i] = otherEncoding.convertToInt(otherTimes[i], encoding);
    }
    _values = dts.valuesArrayFast();
  }

  @SuppressWarnings("unchecked")
  public FastArrayIntObjectTimeSeries(final DateTimeNumericEncoding encoding, final SortedMap<Integer, T> initialMap) {
    super(encoding);
    final int size = initialMap.size();
    _times = new int[size];
    _values = (T[]) new Object[size];
    final Iterator<Entry<Integer, T>> iterator = initialMap.entrySet().iterator();
    int i = 0;
    while (iterator.hasNext()) {
      final Entry<Integer, T> entry = iterator.next();
      _times[i] = entry.getKey().intValue();
      _values[i] = entry.getValue();
      i++;
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  @Override
  public FastIntObjectTimeSeries<T> subSeriesFast(final int startTime, final int endTime) {
    if (isEmpty()) {
      return new FastArrayIntObjectTimeSeries(getEncoding(), new int[0], (T[]) new Object[0]);
    }
    // throw new NoSuchElementException("Series is empty")
    int startPos = Arrays.binarySearch(_times, startTime);
    int endPos = Arrays.binarySearch(_times, endTime);
    // if either is -1, make it zero
    startPos = startPos >= 0 ? startPos : -(startPos + 1);
    endPos = endPos >= 0 ? endPos : -(endPos + 1);
    final int length = endPos - startPos;
    final int[] resultTimes = new int[length];
    final T[] resultValues = (T[]) new Object[length];
    System.arraycopy(_times, startPos, resultTimes, 0, length);
    System.arraycopy(_values, startPos, resultValues, 0, length);
    return new FastArrayIntObjectTimeSeries(getEncoding(), resultTimes, resultValues);
  }

  public T getDataPointFast(final int time) {
    final int index = Arrays.binarySearch(_times, time);
    if (index >= 0) {
      return _values[index];
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public int getEarliestTimeFast() {
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
  public int getLatestTimeFast() {
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

  //-------------------------------------------------------------------------
  /* package */class PrimitiveArrayObjectTimeSeriesIterator implements ObjectIterator<Int2ObjectMap.Entry<T>> {
    private int _current;

    @Override
    public boolean hasNext() {
      return _current < _times.length;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Int2ObjectMap.Entry<T> next() {
      if (hasNext()) {
        final Int2ObjectMap.Entry<T> keyValuePair = (Int2ObjectMap.Entry<T>) new IntObjectPair<Object>(
            _times[_current], _values[_current]);
        _current++;
        return keyValuePair;
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
  public int size() {
    return _times.length;
  }

  @Override
  public boolean isEmpty() {
    return _times.length == 0;
  }

  //-------------------------------------------------------------------------
  /* package */class PrimitiveArrayIntObjectTimeSeriesTimesIterator implements IntIterator {
    private int _current;

    @Override
    public boolean hasNext() {
      return _current < _times.length;
    }

    @Override
    public Integer next() {
      if (hasNext()) {
        final int time = _times[_current];
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
    public int nextInt() {
      if (hasNext()) {
        final int time = _times[_current];
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

  //-------------------------------------------------------------------------
  /* package */class PrimitiveArrayIntObjectTimeSeriesValuesIterator implements ObjectIterator<T> {
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
    return new PrimitiveArrayIntObjectTimeSeriesValuesIterator();
  }

  public T[] valuesArrayFast() {
    return _values.clone();
  }

  public int[] timesArrayFast() {
    return _times.clone();
  }

  @Override
  public int getTimeFast(final int index) {
    return _times[index];
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  public FastIntObjectTimeSeries<T> tailFast(final int numItems) {
    if (numItems <= _times.length) {
      final int[] times = new int[numItems];
      final T[] values = (T[]) new Object[numItems];
      System.arraycopy(_times, _times.length - numItems, times, 0, numItems);
      System.arraycopy(_values, _values.length - numItems, values, 0, numItems);
      return new FastArrayIntObjectTimeSeries(getEncoding(), times, values);
    } else {
      throw new NoSuchElementException("Not enough elements");
    }
  }

  @SuppressWarnings("unchecked")
  public FastIntObjectTimeSeries<T> headFast(final int numItems) {
    if (numItems <= _times.length) {
      final int[] times = new int[numItems];
      final T[] values = (T[]) new Object[numItems];
      System.arraycopy(_times, 0, times, 0, numItems);
      System.arraycopy(_values, 0, values, 0, numItems);
      return new FastArrayIntObjectTimeSeries<T>(getEncoding(), times, values);
    } else {
      throw new NoSuchElementException("Not enough elements");
    }
  }

  /**
   * Note that this is so complicated to try and provide optimal performance. A
   * much slower version would be quite short.
   * @param obj  the objec to check
   * @return true if equal
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
      if (obj instanceof FastIntObjectTimeSeries) {
        @SuppressWarnings("rawtypes")
        final FastIntObjectTimeSeries other = (FastIntObjectTimeSeries) obj;
        if (!Arrays.equals(other.valuesArrayFast(), _values)) {
          return false;
        }
        if (other.getEncoding().equals(getEncoding())) {
          return Arrays.equals(other.timesArrayFast(), _times);
        } else {
          final IntIterator otherTimesIterator = other.timesIteratorFast();
          int i = 0;
          final DateTimeNumericEncoding otherEncoding = other.getEncoding();
          final DateTimeNumericEncoding myEncoding = getEncoding();
          // invarient is that both are the same length as we got past the
          // values equality
          while (otherTimesIterator.hasNext()) {
            if (otherEncoding.convertToInt(otherTimesIterator.nextInt(), myEncoding) != _times[i]) {
              return false;
            }
            i++;
          }
        }
      } else if (obj instanceof FastLongObjectTimeSeries) {
        @SuppressWarnings("rawtypes")
        final FastLongObjectTimeSeries other = (FastLongObjectTimeSeries) obj;
        if (!Arrays.equals(other.valuesArrayFast(), _values)) {
          return false;
        }
        final LongIterator otherTimesIterator = other.timesIteratorFast();
        int i = 0;
        final DateTimeNumericEncoding otherEncoding = other.getEncoding();
        final DateTimeNumericEncoding myEncoding = getEncoding();
        // invarient is that both are the same length as we got past the
        // values equality
        while (otherTimesIterator.hasNext()) {
          if (otherEncoding.convertToInt(otherTimesIterator.nextLong(), myEncoding) != _times[i]) {
            return false;
          }
          i++;
        }
      } else {
        return false;
      }
    } else {
      @SuppressWarnings("rawtypes")
      final FastArrayIntObjectTimeSeries other = (FastArrayIntObjectTimeSeries) obj;
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
        final int[] myTimes = _times;
        final int[] otherTimes = other._times;
        final DateTimeNumericEncoding encoding = other.getEncoding();
        final DateTimeNumericEncoding myEncoding = getEncoding();
        for (int i = 0; i < myTimes.length; i++) {
          if (myTimes[i] != encoding.convertToInt(otherTimes[i], myEncoding)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  @Override
  public IntList timesFast() {
    return new IntArrayList(_times);
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
  public T getValueFast(final int time) {
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

  @Override
  public ObjectIterator<Int2ObjectMap.Entry<T>> iteratorFast() {
    return new PrimitiveArrayObjectTimeSeriesIterator();
  }

  @Override
  public ObjectTimeSeries<Integer, T> subSeries(final Integer startTime, final Integer endTime) {
    return subSeriesFast(startTime, endTime);
  }

  @Override
  public IntIterator timesIteratorFast() {
    return new PrimitiveArrayIntObjectTimeSeriesTimesIterator();
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
  public FastIntObjectTimeSeries<T> newInstanceFast(final int[] times, final T[] values) {
    return new FastArrayIntObjectTimeSeries<T>(getEncoding(), times, values);
  }

}
