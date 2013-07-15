/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.opengamma.timeseries.TimeSeriesUtils;

/**
 * Abstract implementation of {@code InstantObjectTimeSeries}.
 * 
 * @param <T>  the instant type
 * @param <V>  the value being viewed over time
 */
public abstract class AbstractPreciseObjectTimeSeries<T, V> implements PreciseObjectTimeSeries<T, V> {

  /**
   * Creates an instance.
   */
  public AbstractPreciseObjectTimeSeries() {
  }

  //-------------------------------------------------------------------------
  /**
   * Converts the specified instant to the {@code long} form.
   * 
   * @param instant  the instant to convert, not null
   * @return the {@code long} instant
   */
  protected abstract long convertToLong(T instant);

  /**
   * Converts the specified instant from the {@code long} form.
   * 
   * @param instant  the {@code long} instant to convert
   * @return the instant, not null
   */
  protected abstract T convertFromLong(long instant);

  /**
   * Creates an array of the correct T type.
   * 
   * @param size  the size of the array to create
   * @return the array, not null
   */
  protected abstract T[] createArray(int size);

  //-------------------------------------------------------------------------
  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean containsTime(T instant) {
    return containsTime(convertToLong(instant));
  }

  @Override
  public V getValue(T instant) {
    return getValue(convertToLong(instant));
  }

  @Override
  public T getTimeAtIndex(int index) {
    return convertFromLong(getTimeAtIndexFast(index));
  }

  //-------------------------------------------------------------------------
  @Override
  public T getEarliestTime() {
    return convertFromLong(getEarliestTimeFast());
  }

  @Override
  public T getLatestTime() {
    return convertFromLong(getLatestTimeFast());
  }

  //-------------------------------------------------------------------------
  @Override
  public Iterator<T> timesIterator() {
    return new Iterator<T>() {
      private int _index = -1;
      @Override
      public boolean hasNext() {
        return (_index + 1) < size();
      }
      @Override
      public T next() {
        if (hasNext() == false) {
          throw new NoSuchElementException("No more elements");
        }
        _index++;
        return getTimeAtIndex(_index);
      }
      @Override
      public void remove() {
        throw new UnsupportedOperationException("Immutable");
      }
    };
  }

  @Override
  public List<T> times() {
    return new AbstractList<T>() {
      @Override
      public T get(int index) {
        return getTimeAtIndex(index);
      }
      @Override
      public int size() {
        return AbstractPreciseObjectTimeSeries.this.size();
      }
      @Override
      public Iterator<T> iterator() {
        return timesIterator();
      }
    };
  }

  @Override
  public T[] timesArray() {
    long[] times = timesArrayFast();
    T[] result = createArray(times.length);
    for (int i = 0; i < times.length; i++) {
      result[i] = convertFromLong(times[i]);
    }
    return result;
  }

  @Override
  public Iterator<V> valuesIterator() {
    return new Iterator<V>() {
      private int _index = -1;
      @Override
      public boolean hasNext() {
        return (_index + 1) < size();
      }
      @Override
      public V next() {
        if (hasNext() == false) {
          throw new NoSuchElementException("No more elements");
        }
        _index++;
        return getValueAtIndex(_index);
      }
      @Override
      public void remove() {
        throw new UnsupportedOperationException("Immutable");
      }
    };
  }

  @Override
  public List<V> values() {
    return new AbstractList<V>() {
      @Override
      public V get(int index) {
        return getValueAtIndex(index);
      }
      @Override
      public int size() {
        return AbstractPreciseObjectTimeSeries.this.size();
      }
      @Override
      public Iterator<V> iterator() {
        return valuesIterator();
      }
    };
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof PreciseDoubleTimeSeries) {
      PreciseDoubleTimeSeries<?> other = (PreciseDoubleTimeSeries<?>) obj;
      return Arrays.equals(timesArrayFast(), other.timesArrayFast()) &&
              Arrays.equals(valuesArray(), other.valuesArray());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(timesArrayFast()) ^ Arrays.hashCode(valuesArray());
  }

  @Override
  public String toString() {
    return TimeSeriesUtils.toString(this);
  }

}
