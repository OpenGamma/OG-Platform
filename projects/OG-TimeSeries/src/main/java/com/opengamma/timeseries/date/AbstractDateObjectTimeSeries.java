/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import com.opengamma.timeseries.TimeSeriesUtils;

/**
 * Abstract implementation of {@code DateObjectTimeSeries}.
 * 
 * @param <T>  the date type
 * @param <V>  the value being viewed over time
 */
public abstract class AbstractDateObjectTimeSeries<T, V> implements DateObjectTimeSeries<T, V> {

  /**
   * Creates an instance.
   */
  protected AbstractDateObjectTimeSeries() {
  }

  //-------------------------------------------------------------------------
  /**
   * Converts the specified date to the {@code int} form.
   * 
   * @param date  the date to convert, not null
   * @return the {@code int} date
   */
  protected abstract int convertToInt(T date);

  /**
   * Converts the specified date from the {@code int} form.
   * 
   * @param date  the {@code int} date to convert
   * @return the date, not null
   */
  protected abstract T convertFromInt(int date);

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
  public boolean containsTime(T date) {
    return containsTime(convertToInt(date));
  }

  @Override
  public V getValue(T date) {
    return getValue(convertToInt(date));
  }

  @Override
  public T getTimeAtIndex(int index) {
    return convertFromInt(getTimeAtIndexFast(index));
  }

  //-------------------------------------------------------------------------
  @Override
  public T getEarliestTime() {
    return convertFromInt(getEarliestTimeFast());
  }

  @Override
  public T getLatestTime() {
    return convertFromInt(getLatestTimeFast());
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
        return AbstractDateObjectTimeSeries.this.size();
      }
      @Override
      public Iterator<T> iterator() {
        return timesIterator();
      }
    };
  }

  @Override
  public T[] timesArray() {
    int[] times = timesArrayFast();
    T[] result = createArray(times.length);
    for (int i = 0; i < times.length; i++) {
      result[i] = convertFromInt(times[i]);
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
        return AbstractDateObjectTimeSeries.this.size();
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
    if (obj instanceof DateDoubleTimeSeries) {
      DateDoubleTimeSeries<?> other = (DateDoubleTimeSeries<?>) obj;
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
