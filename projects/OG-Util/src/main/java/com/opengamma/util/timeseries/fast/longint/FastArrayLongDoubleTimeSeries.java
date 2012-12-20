/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.longint;

import static com.opengamma.util.tuple.TuplesUtil.pairToEntry;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.SortedMap;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.lambdava.tuple.LongDoublePair;

/**
 * 
 */
public class FastArrayLongDoubleTimeSeries extends AbstractFastLongDoubleTimeSeries {
  /** An empty double time series backed using milliseconds as the date encoding */
  public static final FastLongDoubleTimeSeries EMPTY_SERIES = new FastArrayLongDoubleTimeSeries(
      DateTimeNumericEncoding.TIME_EPOCH_MILLIS);

  private final long[] _times;
  private final double[] _values;

  private FastArrayLongDoubleTimeSeries(final DateTimeNumericEncoding encoding) {
    super(encoding);
    _times = new long[0];
    _values = new double[0];
  }

  public FastArrayLongDoubleTimeSeries(final DateTimeNumericEncoding encoding, final Long[] times, final Double[] values) {
    super(encoding);
    _times = new long[times.length];
    _values = new double[values.length];
    init(ArrayUtils.toPrimitive(times), ArrayUtils.toPrimitive(values));
  }

  public FastArrayLongDoubleTimeSeries(final DateTimeNumericEncoding encoding, final long[] times, final double[] values) {
    super(encoding);
    _times = new long[times.length];
    _values = new double[values.length];
    init(times, values);
  }

  private void init(final long[] times, final double[] values) {
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

  public FastArrayLongDoubleTimeSeries(final DateTimeNumericEncoding encoding, final List<Long> times,
      final List<Double> values) {
    super(encoding);
    if (times.size() != values.size()) {
      throw new IllegalArgumentException("lists are of different sizes");
    }
    _times = new long[times.size()];
    _values = new double[values.size()];
    final Iterator<Double> iter = values.iterator();
    int i = 0;
    long maxTime = Long.MIN_VALUE; // for checking the dates are sorted.
    for (final long time : times) {
      final double value = iter.next();
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

  public FastArrayLongDoubleTimeSeries(final FastLongDoubleTimeSeries dts) {
    super(dts.getEncoding());
    _times = dts.timesArrayFast();
    _values = dts.valuesArrayFast();
  }

  public FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding encoding, final FastLongDoubleTimeSeries dts) {
    super(dts.getEncoding());
    DateTimeNumericEncoding otherEncoding = dts.getEncoding();
    _times = dts.timesArrayFast();
    for (int i = 0; i < _times.length; i++) {
      _times[i] = otherEncoding.convertToLong(_times[i], encoding);
    }
    _values = dts.valuesArrayFast();
  }

  public FastArrayLongDoubleTimeSeries(final FastIntDoubleTimeSeries dts) {
    super(dts.getEncoding());
    DateTimeNumericEncoding encoding = dts.getEncoding();
    int[] timesArrayFast = dts.timesArrayFast();
    _values = dts.valuesArrayFast();
    _times = new long[timesArrayFast.length];
    for (int i = 0; i < timesArrayFast.length; i++) {
      _times[i] = encoding.convertToLong(timesArrayFast[i], encoding);
    }
  }

  public FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding encoding, final FastIntDoubleTimeSeries dts) {
    super(encoding);
    DateTimeNumericEncoding otherEncoding = dts.getEncoding();
    int[] timesArrayFast = dts.timesArrayFast();
    _values = dts.valuesArrayFast();
    _times = new long[timesArrayFast.length];
    for (int i = 0; i < timesArrayFast.length; i++) {
      _times[i] = otherEncoding.convertToLong(timesArrayFast[i], encoding);
    }
  }

  public FastArrayLongDoubleTimeSeries(final DateTimeNumericEncoding encoding, final SortedMap<Long, Double> initialMap) {
    super(encoding);
    final int size = initialMap.size();
    _times = new long[size];
    _values = new double[size];
    final Iterator<Entry<Long, Double>> iterator = initialMap.entrySet().iterator();
    int i = 0;
    while (iterator.hasNext()) {
      final Entry<Long, Double> entry = iterator.next();
      _times[i] = entry.getKey().longValue();
      _values[i] = entry.getValue().doubleValue();
      i++;
    }
  }

  @Override
  public FastLongDoubleTimeSeries subSeriesFast(final long startTime, final long endTime) {
    if (isEmpty()) {
      return EMPTY_SERIES;
    }
    // throw new NoSuchElementException("Series is empty")
    int startPos = Arrays.binarySearch(_times, startTime);
    int endPos = (endTime == Long.MIN_VALUE) ? _times.length : Arrays.binarySearch(_times, endTime);
    // if either is -1, make it zero
    startPos = startPos >= 0 ? startPos : -startPos - 1;
    endPos = endPos >= 0 ? endPos : -endPos - 1;
    final int length = endPos - startPos;
    if (endPos >= _times.length) {
      endPos--;
    }
    final long[] resultTimes = new long[length];
    final double[] resultValues = new double[length];
    System.arraycopy(_times, startPos, resultTimes, 0, length);
    System.arraycopy(_values, startPos, resultValues, 0, length);
    return new FastArrayLongDoubleTimeSeries(getEncoding(), resultTimes, resultValues);
  }

  public double getDataPointFast(final long time) {
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
  public double getEarliestValueFast() {
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
  public double getLatestValueFast() {
    if (_values.length > 0) {
      return _values[_values.length - 1];
    } else {
      throw new NoSuchElementException("Series is empty");
    }
  }

  /* package */class PrimitiveArrayDoubleTimeSeriesIterator implements ObjectIterator<Long2DoubleMap.Entry> {
    private int _current;

    @Override
    public boolean hasNext() {
      return _current < _times.length;
    }

    @Override
    public Long2DoubleMap.Entry next() {
      if (hasNext()) {
        final Long2DoubleMap.Entry keyValuePair = pairToEntry(new LongDoublePair(_times[_current], _values[_current]));
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

  /* package */class PrimitiveArrayDoubleTimeSeriesTimesIterator implements LongIterator {
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

  /* package */class PrimitiveArrayDoubleTimeSeriesValuesIterator implements DoubleIterator {
    private int _current;

    @Override
    public boolean hasNext() {
      return _current < _values.length;
    }

    @Override
    public Double next() {
      if (hasNext()) {
        final Double value = _values[_current];
        _current++;
        return value;
      } else {
        throw new NoSuchElementException();
      }
    }

    public double nextDouble() {
      if (hasNext()) {
        final double value = _values[_current];
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
  public DoubleIterator valuesIteratorFast() {
    return new PrimitiveArrayDoubleTimeSeriesValuesIterator();
  }

  @Override
  public double[] valuesArrayFast() {
    return _values.clone();
  }

  @Override
  public long[] timesArrayFast() {
    return _times.clone();
  }

  @Override
  public long getTimeFast(final int index) {
    return _times[index];
  }

  @Override
  public FastLongDoubleTimeSeries tail(final int numItems) {
    if (numItems <= _times.length) {
      final long[] times = new long[numItems];
      final double[] values = new double[numItems];
      System.arraycopy(_times, _times.length - numItems, times, 0, numItems);
      System.arraycopy(_values, _values.length - numItems, values, 0, numItems);
      return new FastArrayLongDoubleTimeSeries(getEncoding(), times, values);
    } else {
      throw new NoSuchElementException("Not enough elements");
    }
  }

  @Override
  public FastLongDoubleTimeSeries tailFast(final int numItems) {
    return tail(numItems);
  }

  @Override
  public FastLongDoubleTimeSeries head(final int numItems) {
    if (numItems <= _times.length) {
      final long[] times = new long[numItems];
      final double[] values = new double[numItems];
      System.arraycopy(_times, 0, times, 0, numItems);
      System.arraycopy(_values, 0, values, 0, numItems);
      return new FastArrayLongDoubleTimeSeries(getEncoding(), times, values);
    } else {
      throw new NoSuchElementException("Not enough elements");
    }
  }

  @Override
  public FastLongDoubleTimeSeries headFast(final int numItems) {
    return head(numItems);
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
      } else if (obj instanceof FastIntDoubleTimeSeries) {
        final FastIntDoubleTimeSeries other = (FastIntDoubleTimeSeries) obj;
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
      final FastArrayLongDoubleTimeSeries other = (FastArrayLongDoubleTimeSeries) obj;
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
  public DoubleList valuesFast() {
    return new DoubleArrayList(_values);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(_values);
  }

  @Override
  public double getValueFast(final long time) {
    final int binarySearch = Arrays.binarySearch(_times, time);
    if (binarySearch >= 0 && _times[binarySearch] == time) {
      return _values[binarySearch];
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public double getValueAtFast(final int index) {
    return _values[index];
  }

  @SuppressWarnings("unchecked")
  @Override
  public Iterator<Entry<Long, Double>> iterator() {
    return (Iterator<Entry<Long, Double>>) (Iterator<? extends Entry<Long, Double>>) new PrimitiveArrayDoubleTimeSeriesIterator();
  }

  @Override
  public ObjectIterator<Long2DoubleMap.Entry> iteratorFast() {
    return new PrimitiveArrayDoubleTimeSeriesIterator();
  }

  @Override
  public DoubleTimeSeries<Long> subSeries(final Long startTime, final Long endTime) {
    return subSeriesFast(startTime, endTime);
  }

  @Override
  public LongIterator timesIteratorFast() {
    return new PrimitiveArrayDoubleTimeSeriesTimesIterator();
  }

  @Override
  public FastLongDoubleTimeSeries newInstanceFast(final long[] times, final double[] values) {
    return new FastArrayLongDoubleTimeSeries(getEncoding(), times, values);
  }

}
