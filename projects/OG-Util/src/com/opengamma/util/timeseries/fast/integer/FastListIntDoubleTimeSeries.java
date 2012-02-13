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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.util.tuple.IntDoublePair;

/**
 * 
 */
public class FastListIntDoubleTimeSeries extends AbstractFastMutableIntDoubleTimeSeries {
  private final IntArrayList _times;
  private final DoubleArrayList _values;

  public FastListIntDoubleTimeSeries(final DateTimeNumericEncoding encoding) {
    super(encoding);
    _times = new IntArrayList();
    _values = new DoubleArrayList();
  }

  public FastListIntDoubleTimeSeries(final DateTimeNumericEncoding encoding, final int[] times, final double[] values) {
    super(encoding);
    _times = new IntArrayList(times);
    _values = new DoubleArrayList(values);
    ensureTimesSorted();
  }

  public FastListIntDoubleTimeSeries(final DateTimeNumericEncoding encoding, final List<Integer> times,
      final List<Double> values) {
    super(encoding);
    _times = new IntArrayList(times);
    _values = new DoubleArrayList(values);
    ensureTimesSorted();
  }

  public FastListIntDoubleTimeSeries(final FastIntDoubleTimeSeries dts) {
    super(dts.getEncoding());
    _times = new IntArrayList(dts.timesArrayFast());
    _values = new DoubleArrayList(dts.valuesArrayFast());
    // don't need to check here
  }

  public FastListIntDoubleTimeSeries(final DateTimeNumericEncoding targetEncoding, final FastIntDoubleTimeSeries dts) {
    super(targetEncoding);
    _times = new IntArrayList();
    _values = new DoubleArrayList(dts.valuesArrayFast());
    final DateTimeNumericEncoding encoding = dts.getEncoding();
    final int[] times = dts.timesArrayFast();
    for (final int time : times) {
      _times.add(encoding.convertToInt(time, targetEncoding));
    }
  }

  public FastListIntDoubleTimeSeries(final FastLongDoubleTimeSeries dts) {
    super(dts.getEncoding());
    _times = new IntArrayList();
    _values = new DoubleArrayList(dts.valuesArrayFast());
    final DateTimeNumericEncoding encoding = dts.getEncoding();
    final long[] times = dts.timesArrayFast();
    for (final long time : times) {
      _times.add(encoding.convertToInt(time, encoding));
    }
  }

  public FastListIntDoubleTimeSeries(final DateTimeNumericEncoding targetEncoding, final FastLongDoubleTimeSeries dts) {
    super(targetEncoding);
    _times = new IntArrayList();
    _values = new DoubleArrayList(dts.valuesArrayFast());
    final DateTimeNumericEncoding encoding = dts.getEncoding();
    final long[] times = dts.timesArrayFast();
    for (final long time : times) {
      _times.add(encoding.convertToInt(time, targetEncoding));
    }
  }

  public FastListIntDoubleTimeSeries(final DateTimeNumericEncoding encoding, final SortedMap<Integer, Double> initialMap) {
    super(encoding);
    _times = new IntArrayList(initialMap.size());
    _values = new DoubleArrayList(initialMap.size());
    for (final Map.Entry<Integer, Double> entry : initialMap.entrySet()) {
      _times.add(entry.getKey());
      _values.add(entry.getValue());
    }
  }

  private void ensureTimesSorted() {
    final IntIterator iter = _times.iterator();
    int current = Integer.MIN_VALUE;
    while (iter.hasNext()) {
      final int next = iter.nextInt();
      if (next < current) {
        throw new OpenGammaRuntimeException("Times not in order");
      }
      current = next;
    }
  }

  @Override
  public int getEarliestTimeFast() {
    if (_times.size() > 0) {
      return _times.getInt(0);
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
  public int getLatestTimeFast() {
    if (_times.size() > 0) {
      return _times.getInt(_times.size() - 1);
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
  public int getTimeFast(final int index) {
    return _times.getInt(index);
  }

  @Override
  public double getValueAtFast(final int index) {
    return _values.getDouble(index);
  }

  @Override
  public double getValueFast(final int time) {
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
  public FastIntDoubleTimeSeries subSeriesFast(final int startTime, final int endTime) {
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
    return new FastListIntDoubleTimeSeries(getEncoding(), _times.subList(startIndex, endIndex), _values.subList(
        startIndex, endIndex));
  }

  @Override
  public int[] timesArrayFast() {
    return _times.toIntArray();
  }

  @Override
  public IntIterator timesIteratorFast() {
    return _times.iterator();
  }

  @Override
  public IntList timesFast() {
    return new IntArrayList(_times);
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

  private class PrimitiveListIntDoubleTimeSeriesIterator implements ObjectIterator<Int2DoubleMap.Entry> {
    private IntIterator _intIter;
    private DoubleIterator _doubleIter;

    public PrimitiveListIntDoubleTimeSeriesIterator() {
      _intIter = _times.iterator();
      _doubleIter = _values.iterator();
    }

    @Override
    public boolean hasNext() {
      return _intIter.hasNext();
    }

    @Override
    public Int2DoubleMap.Entry next() {
      final int time = _intIter.nextInt();
      final double value = _doubleIter.nextDouble();
      return new IntDoublePair(time, value);
    }

    @Override
    public void remove() {
      _intIter.remove();
      _doubleIter.remove();
    }

    @Override
    public int skip(final int n) {
      _intIter.skip(n);
      return _doubleIter.skip(n);
    }

  }

  public ObjectIterator<Int2DoubleMap.Entry> iteratorFast() {
    return new PrimitiveListIntDoubleTimeSeriesIterator();
  }

  @Override
  public FastIntDoubleTimeSeries headFast(final int numItems) {
    return new FastListIntDoubleTimeSeries(getEncoding(), _times.subList(0, numItems), _values.subList(0, numItems));
  }

  @Override
  public FastIntDoubleTimeSeries tailFast(final int numItems) {
    // note I used _times.size for the second part so it we didn't need two
    // method calls as the optimizer is unlikely to spot it.
    return new FastListIntDoubleTimeSeries(getEncoding(), _times.subList(_times.size() - numItems, _times.size()),
        _values.subList(_times.size() - numItems, _times.size()));
  }

  @Override
  public void clear() {
    _times.clear();
    _values.clear();
  }

  @Override
  public void primitivePutDataPoint(final int time, final double value) {
    final int index = Arrays.binarySearch(_times.toIntArray(), time);
    if (index >= 0) {
      _values.set(index, value);
    } else {
      final int insertion_index = -index - 1;
      if (insertion_index == _times.size()) { // add onto the end.
        _times.add(time);
        _values.add(value);
      } else {
        _times.add(insertion_index, time);
        _values.add(insertion_index, value);
      }
    }
  }

  @Override
  public void primitiveRemoveDataPoint(final int time) {
    final int index = Arrays.binarySearch(_times.toIntArray(), time);
    if (index >= 0) {
      _times.remove(index);
      _values.remove(index);
    }
  }

  // REVIEW: jim 15-Feb-2010 -- should these be here (head and tail)?

  @Override
  public int hashCode() {
    return _values.hashCode();
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
        if (!Arrays.equals(other.valuesArrayFast(), _values.toDoubleArray())) {
          return false;
        }
        if (other.getEncoding().equals(getEncoding())) {
          return Arrays.equals(other.timesArrayFast(), _times.toIntArray());
        } else {
          final IntIterator otherTimesIterator = other.timesIteratorFast();
          final IntIterator myTimesIterator = _times.iterator();
          final DateTimeNumericEncoding otherEncoding = other.getEncoding();
          final DateTimeNumericEncoding myEncoding = getEncoding();
          // invarient is that both are the same length as we got past the
          // values equality
          while (otherTimesIterator.hasNext()) {
            if (otherEncoding.convertToInt(otherTimesIterator.nextInt(), myEncoding) != myTimesIterator.next()) {
              return false;
            }
          }
        }
      } else if (obj instanceof FastLongDoubleTimeSeries) {
        final FastLongDoubleTimeSeries other = (FastLongDoubleTimeSeries) obj;
        if (!Arrays.equals(other.valuesArrayFast(), _values.toDoubleArray())) {
          return false;
        }
        final LongIterator otherTimesIterator = other.timesIteratorFast();
        final IntIterator myTimesIterator = _times.iterator();
        final DateTimeNumericEncoding otherEncoding = other.getEncoding();
        final DateTimeNumericEncoding myEncoding = getEncoding();
        // invarient is that both are the same length as we got past the
        // values equality
        while (otherTimesIterator.hasNext()) {
          if (otherEncoding.convertToInt(otherTimesIterator.nextLong(), myEncoding) != myTimesIterator.next()) {
            return false;
          }
        }
      } else {
        return false;
      }
    } else {
      final FastListIntDoubleTimeSeries other = (FastListIntDoubleTimeSeries) obj;
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
        final int[] myTimes = _times.toIntArray();
        final int[] otherTimes = other._times.toIntArray();
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
  public DoubleTimeSeries<Integer> head(final int numItems) {
    return headFast(numItems);
  }

  @Override
  public DoubleTimeSeries<Integer> tail(final int numItems) {
    return tailFast(numItems);
  }

  @Override
  public FastIntDoubleTimeSeries newInstanceFast(final int[] times, final double[] values) {
    return new FastListIntDoubleTimeSeries(getEncoding(), times, values);
  }

}
