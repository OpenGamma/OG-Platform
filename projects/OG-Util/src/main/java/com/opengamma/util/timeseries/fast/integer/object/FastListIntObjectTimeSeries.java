/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fast.integer.object;

import static com.opengamma.util.tuple.TuplesUtil.pairToEntry;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.object.FastLongObjectTimeSeries;
import com.opengamma.lambdava.tuple.IntObjectPair;

/**
 * 
 * @param <T> The type of the data
 */
public class FastListIntObjectTimeSeries<T> extends AbstractFastMutableIntObjectTimeSeries<T> {
  private final IntArrayList _times;
  private final ObjectArrayList<T> _values;

  public FastListIntObjectTimeSeries(final DateTimeNumericEncoding encoding) {
    super(encoding);
    _times = new IntArrayList();
    _values = new ObjectArrayList<T>();
  }

  public FastListIntObjectTimeSeries(final DateTimeNumericEncoding encoding, final int[] times, final T[] values) {
    super(encoding);
    _times = new IntArrayList(times);
    _values = new ObjectArrayList<T>(values);
    ensureTimesSorted();
  }

  public FastListIntObjectTimeSeries(final DateTimeNumericEncoding encoding, final List<Integer> times,
      final List<T> values) {
    super(encoding);
    _times = new IntArrayList(times);
    _values = new ObjectArrayList<T>(values);
    ensureTimesSorted();
  }

  public FastListIntObjectTimeSeries(final FastIntObjectTimeSeries<T> dts) {
    super(dts.getEncoding());
    _times = new IntArrayList(dts.timesArrayFast());
    _values = new ObjectArrayList<T>((T[]) dts.valuesArrayFast());
    // don't need to check here
  }

  public FastListIntObjectTimeSeries(final DateTimeNumericEncoding targetEncoding, final FastIntObjectTimeSeries<T> dts) {
    super(targetEncoding);
    _times = new IntArrayList();
    _values = new ObjectArrayList<T>(dts.valuesArrayFast());
    final DateTimeNumericEncoding encoding = dts.getEncoding();
    final int[] times = dts.timesArrayFast();
    for (final int time : times) {
      _times.add(encoding.convertToInt(time, targetEncoding));
    }
  }

  public FastListIntObjectTimeSeries(final FastLongObjectTimeSeries<T> dts) {
    super(dts.getEncoding());
    _times = new IntArrayList();
    _values = new ObjectArrayList<T>(dts.valuesArrayFast());
    final DateTimeNumericEncoding encoding = dts.getEncoding();
    final long[] times = dts.timesArrayFast();
    for (final long time : times) {
      _times.add(encoding.convertToInt(time, encoding));
    }
  }

  public FastListIntObjectTimeSeries(final DateTimeNumericEncoding targetEncoding, final FastLongObjectTimeSeries<T> dts) {
    super(targetEncoding);
    _times = new IntArrayList();
    _values = new ObjectArrayList<T>(dts.valuesArrayFast());
    final DateTimeNumericEncoding encoding = dts.getEncoding();
    final long[] times = dts.timesArrayFast();
    for (final long time : times) {
      _times.add(encoding.convertToInt(time, targetEncoding));
    }
  }

  public FastListIntObjectTimeSeries(final DateTimeNumericEncoding encoding, final SortedMap<Integer, T> initialMap) {
    super(encoding);
    _times = new IntArrayList(initialMap.size());
    _values = new ObjectArrayList<T>(initialMap.size());
    for (final Map.Entry<Integer, T> entry : initialMap.entrySet()) {
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
  public T getEarliestValueFast() {
    if (_values.size() > 0) {
      return _values.get(0);
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
  public T getLatestValueFast() {
    if (_values.size() > 0) {
      return _values.get(_times.size() - 1);
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public int getTimeFast(final int index) {
    return _times.getInt(index);
  }

  @Override
  public T getValueAtFast(final int index) {
    return _values.get(index);
  }

  @Override
  public T getValueFast(final int time) {
    final int index = _times.indexOf(time);
    if (index >= 0) {
      return _values.get(index);
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
  public FastIntObjectTimeSeries<T> subSeriesFast(final int startTime, final int endTime) {
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
    return new FastListIntObjectTimeSeries<T>(getEncoding(), _times.subList(startIndex, endIndex), _values.subList(
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

  @SuppressWarnings("unchecked")
  @Override
  public T[] valuesArrayFast() {
    return (T[]) _values.toArray();
  }

  @Override
  public ObjectIterator<T> valuesIteratorFast() {
    return _values.iterator();
  }

  @Override
  public ObjectList<T> valuesFast() {
    return new ObjectArrayList<T>(_values);
  }

  private class PrimitiveListIntObjectTimeSeriesIterator implements ObjectIterator<Int2ObjectMap.Entry<T>> {
    private IntIterator _intIter;
    private ObjectIterator<T> _typeTIter;

    public PrimitiveListIntObjectTimeSeriesIterator() {
      _intIter = _times.iterator();
      _typeTIter = _values.iterator();
    }

    @Override
    public boolean hasNext() {
      return _intIter.hasNext();
    }

    @Override
    public Int2ObjectMap.Entry<T> next() {
      final int time = _intIter.nextInt();
      final T value = _typeTIter.next();
      return pairToEntry(new IntObjectPair<T>(time, value));
    }

    @Override
    public void remove() {
      _intIter.remove();
      _typeTIter.remove();
    }

    @Override
    public int skip(final int n) {
      _intIter.skip(n);
      return _typeTIter.skip(n);
    }

  }

  public ObjectIterator<Int2ObjectMap.Entry<T>> iteratorFast() {
    return new PrimitiveListIntObjectTimeSeriesIterator();
  }

  @Override
  public FastIntObjectTimeSeries<T> headFast(final int numItems) {
    return new FastListIntObjectTimeSeries<T>(getEncoding(), _times.subList(0, numItems), _values.subList(0, numItems));
  }

  @Override
  public FastIntObjectTimeSeries<T> tailFast(final int numItems) {
    // note I used _times.size for the second part so it we didn't need two
    // method calls as the optimizer is unlikely to spot it.
    return new FastListIntObjectTimeSeries<T>(getEncoding(), _times.subList(_times.size() - numItems, _times.size()),
        _values.subList(_times.size() - numItems, _times.size()));
  }

  @Override
  public void clear() {
    _times.clear();
    _values.clear();
  }

  @Override
  public void primitivePutDataPoint(final int time, final T value) {
    final int index = Arrays.binarySearch(_times.elements(), time);
    if (index >= 0) {
      _values.set(index, value);
    } else {
      if ((-(index + 1)) >= _times.size()) { // add onto the end.
        _times.add(time);
        _values.add(value);
      } else {
        _times.add(-(index + 1), time);
        _values.add(-(index + 1), value);
      }
    }
  }

  @Override
  public void primitiveRemoveDataPoint(final int time) {
    final int index = Arrays.binarySearch(_times.elements(), time);
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
   * Note that this is so complicated to try and provide optimal performance. A
   * much slower version would be quite short.
   * @param obj  the object to check
   * @return true if equal
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
      if (obj instanceof FastIntObjectTimeSeries<?>) {
        final FastIntObjectTimeSeries<T> other = (FastIntObjectTimeSeries<T>) obj;
        if (!Arrays.equals(other.valuesArrayFast(), _values.elements())) {
          return false;
        }
        if (other.getEncoding().equals(getEncoding())) {
          return Arrays.equals(other.timesArrayFast(), _times.elements());
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
      } else if (obj instanceof FastLongObjectTimeSeries<?>) {
        final FastLongObjectTimeSeries<T> other = (FastLongObjectTimeSeries<T>) obj;
        if (!Arrays.equals(other.valuesArrayFast(), _values.elements())) {
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
      @SuppressWarnings("rawtypes")
      final FastListIntObjectTimeSeries other = (FastListIntObjectTimeSeries) obj;
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
        final int[] myTimes = _times.elements();
        final int[] otherTimes = other._times.elements();
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
  public ObjectTimeSeries<Integer, T> head(final int numItems) {
    return headFast(numItems);
  }

  @Override
  public ObjectTimeSeries<Integer, T> tail(final int numItems) {
    return tailFast(numItems);
  }

  @Override
  public FastIntObjectTimeSeries<T> newInstanceFast(final int[] times, final T[] values) {
    return new FastListIntObjectTimeSeries<T>(getEncoding(), times, values);
  }

}
