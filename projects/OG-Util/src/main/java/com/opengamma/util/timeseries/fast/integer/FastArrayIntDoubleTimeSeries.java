/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.integer;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.SortedMap;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.util.tuple.IntDoublePair;

/**
 * 
 */
public class FastArrayIntDoubleTimeSeries extends AbstractFastIntDoubleTimeSeries {
  /** An empty double time series with the time expressed as int and the millisecond-from-epoch date encoding */
  public static final FastIntDoubleTimeSeries EMPTY_SERIES = new FastArrayIntDoubleTimeSeries(
      DateTimeNumericEncoding.TIME_EPOCH_MILLIS);

  private final int[] _times;
  private final double[] _values;

  public FastArrayIntDoubleTimeSeries(final DateTimeNumericEncoding encoding) {
    super(encoding);
    _times = new int[0];
    _values = new double[0];
  }

  public FastArrayIntDoubleTimeSeries(final DateTimeNumericEncoding encoding, final Integer[] times,
      final Double[] values) {
    super(encoding);
    _times = new int[times.length];
    _values = new double[values.length];
    init(ArrayUtils.toPrimitive(times), ArrayUtils.toPrimitive(values));
  }

  public FastArrayIntDoubleTimeSeries(final DateTimeNumericEncoding encoding, final int[] times, final double[] values) {
    super(encoding);
    _times = new int[times.length];
    _values = new double[values.length];
    init(times, values);
  }

  private void init(final int[] times, final double[] values) {
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

  public FastArrayIntDoubleTimeSeries(final DateTimeNumericEncoding encoding, final List<Integer> times,
      final List<Double> values) {
    super(encoding);
    ArgumentChecker.isTrue(times.size() == values.size(), "lists are of different sizes; have {} and {}", times.size(), values.size());
    _times = new int[times.size()];
    _values = new double[values.size()];
    final Iterator<Double> iter = values.iterator();
    int i = 0;
    int maxTime = Integer.MIN_VALUE; // for checking the dates are sorted.
    for (final int time : times) {
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

  public FastArrayIntDoubleTimeSeries(final FastIntDoubleTimeSeries dts) {
    super(dts.getEncoding());
    _times = dts.timesArrayFast();
    _values = dts.valuesArrayFast();
  }

  public FastArrayIntDoubleTimeSeries(final DateTimeNumericEncoding encoding, final FastIntDoubleTimeSeries dts) {
    super(encoding);
    DateTimeNumericEncoding sourceEncoding = dts.getEncoding();
    _times = dts.timesArrayFast();
    for (int i = 0; i < _times.length; i++) {
      _times[i] = sourceEncoding.convertToInt(_times[i], encoding);
    }
    _values = dts.valuesArrayFast();
  }

  public FastArrayIntDoubleTimeSeries(final FastLongDoubleTimeSeries dts) {
    this(dts.getEncoding(), dts);
  }

  public FastArrayIntDoubleTimeSeries(final DateTimeNumericEncoding encoding, final FastLongDoubleTimeSeries dts) {
    super(encoding);
    DateTimeNumericEncoding otherEncoding = dts.getEncoding();
    long[] otherTimes = dts.timesArrayFast();
    _times = new int[otherTimes.length];
    for (int i = 0; i < otherTimes.length; i++) {
      _times[i] = otherEncoding.convertToInt(otherTimes[i], encoding);
    }
    _values = dts.valuesArrayFast();
  }

  public FastArrayIntDoubleTimeSeries(final DateTimeNumericEncoding encoding,
      final SortedMap<Integer, Double> initialMap) {
    super(encoding);
    final int size = initialMap.size();
    _times = new int[size];
    _values = new double[size];
    final Iterator<Entry<Integer, Double>> iterator = initialMap.entrySet().iterator();
    int i = 0;
    while (iterator.hasNext()) {
      final Entry<Integer, Double> entry = iterator.next();
      _times[i] = entry.getKey().intValue();
      _values[i] = entry.getValue().doubleValue();
      i++;
    }
  }

  @Override
  public FastIntDoubleTimeSeries subSeriesFast(final int startTime, final int endTime) {
    if (isEmpty()) {
      return EMPTY_SERIES;
    }
    // throw new NoSuchElementException("Series is empty")
    int startPos = Arrays.binarySearch(_times, startTime);
    int endPos = (endTime == Integer.MIN_VALUE) ? _times.length : Arrays.binarySearch(_times, endTime);
    // if either is -1, make it zero
    startPos = startPos >= 0 ? startPos : -(startPos + 1);
    endPos = endPos >= 0 ? endPos : -(endPos + 1);
    final int length = endPos - startPos; // trying it out anyway.
    if (endPos >= _times.length) {
      endPos--;
    }

    final int[] resultTimes = new int[length];
    final double[] resultValues = new double[length];
    System.arraycopy(_times, startPos, resultTimes, 0, length);
    System.arraycopy(_values, startPos, resultValues, 0, length);
    return new FastArrayIntDoubleTimeSeries(getEncoding(), resultTimes, resultValues);
  }

  public double getDataPointFast(final int time) {
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
  public double getEarliestValueFast() {
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
  public double getLatestValueFast() {
    if (_values.length > 0) {
      return _values[_values.length - 1];
    } else {
      throw new NoSuchElementException("Series is empty");
    }
  }

  /* package */class PrimitiveArrayDoubleTimeSeriesIterator implements ObjectIterator<Int2DoubleMap.Entry> {
    private int _current;

    @Override
    public boolean hasNext() {
      return _current < _times.length;
    }

    @Override
    public Int2DoubleMap.Entry next() {
      if (hasNext()) {
        final Int2DoubleMap.Entry keyValuePair = new IntDoublePair(_times[_current], _values[_current]);
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

  /* package */class PrimitiveArrayIntDoubleTimeSeriesTimesIterator implements IntIterator {
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

  /* package */class PrimitiveArrayIntDoubleTimeSeriesValuesIterator implements DoubleIterator {
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

    @Override
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
    return new PrimitiveArrayIntDoubleTimeSeriesValuesIterator();
  }

  @Override
  public double[] valuesArrayFast() {
    return _values.clone();
  }

  @Override
  public int[] timesArrayFast() {
    return _times.clone();
  }

  @Override
  public int getTimeFast(final int index) {
    return _times[index];
  }

  @Override
  public FastIntDoubleTimeSeries tailFast(final int numItems) {
    if (numItems <= _times.length) {
      final int[] times = new int[numItems];
      final double[] values = new double[numItems];
      System.arraycopy(_times, _times.length - numItems, times, 0, numItems);
      System.arraycopy(_values, _values.length - numItems, values, 0, numItems);
      return new FastArrayIntDoubleTimeSeries(getEncoding(), times, values);
    } else {
      throw new NoSuchElementException("Not enough elements");
    }
  }

  @Override
  public FastIntDoubleTimeSeries headFast(final int numItems) {
    if (numItems <= _times.length) {
      final int[] times = new int[numItems];
      final double[] values = new double[numItems];
      System.arraycopy(_times, 0, times, 0, numItems);
      System.arraycopy(_values, 0, values, 0, numItems);
      return new FastArrayIntDoubleTimeSeries(getEncoding(), times, values);
    } else {
      throw new NoSuchElementException("Not enough elements");
    }
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
      if (obj instanceof FastIntDoubleTimeSeries) {
        final FastIntDoubleTimeSeries other = (FastIntDoubleTimeSeries) obj;
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
      } else if (obj instanceof FastLongDoubleTimeSeries) {
        final FastLongDoubleTimeSeries other = (FastLongDoubleTimeSeries) obj;
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
      final FastArrayIntDoubleTimeSeries other = (FastArrayIntDoubleTimeSeries) obj;
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
  public DoubleList valuesFast() {
    return new DoubleArrayList(_values);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(_values);
  }

  @Override
  public double getValueFast(final int time) {
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

  @Override
  public ObjectIterator<Int2DoubleMap.Entry> iteratorFast() {
    return new PrimitiveArrayDoubleTimeSeriesIterator();
  }

  @Override
  public DoubleTimeSeries<Integer> subSeries(final Integer startTime, final Integer endTime) {
    return subSeriesFast(startTime, endTime);
  }

  @Override
  public IntIterator timesIteratorFast() {
    return new PrimitiveArrayIntDoubleTimeSeriesTimesIterator();
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
  public FastIntDoubleTimeSeries newInstanceFast(final int[] times, final double[] values) {
    return new FastArrayIntDoubleTimeSeries(getEncoding(), times, values);
  }

}
