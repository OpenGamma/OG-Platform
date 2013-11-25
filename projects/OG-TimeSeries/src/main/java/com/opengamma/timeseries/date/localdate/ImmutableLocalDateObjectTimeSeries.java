/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date.localdate;

import java.io.Serializable;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.ObjectTimeSeries;
import com.opengamma.timeseries.ObjectTimeSeriesOperators;
import com.opengamma.timeseries.ObjectTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.ObjectTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.date.AbstractDateObjectTimeSeries;
import com.opengamma.timeseries.date.DateObjectTimeSeries;

/**
 * Standard immutable implementation of {@code LocalDateObjectTimeSeries}.
 * 
 * @param <V>  the value being viewed over time
 */
public final class ImmutableLocalDateObjectTimeSeries<V>
    extends AbstractDateObjectTimeSeries<LocalDate, V>
    implements LocalDateObjectTimeSeries<V>, Serializable {

  /** Empty instance. */
  private static final ImmutableLocalDateObjectTimeSeries<?> EMPTY_SERIES = new ImmutableLocalDateObjectTimeSeries<Object>(new int[0], new Object[0]);

  /** Serialization version. */
  private static final long serialVersionUID = -43654613865187568L;

  /**
   * The times in the series.
   */
  private final int[] _times;
  /**
   * The values in the series.
   */
  private final V[] _values;

  //-------------------------------------------------------------------------
  /**
   * Creates an empty builder, used to create time-series.
   * <p>
   * The builder has methods to create and modify a time-series.
   * 
   * @param <V>  the value being viewed over time
   * @return the time-series builder, not null
   */
  public static <V> LocalDateObjectTimeSeriesBuilder<V> builder() {
    return new ImmutableLocalDateObjectTimeSeriesBuilder<V>();
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains a time-series from a single date and value.
   * 
   * @param <V>  the value being viewed over time
   * @return the time-series, not null
   */
  @SuppressWarnings("unchecked")
  public static <V> ImmutableLocalDateObjectTimeSeries<V> ofEmpty() {
    return (ImmutableLocalDateObjectTimeSeries<V>) EMPTY_SERIES;
  }

  /**
   * Obtains a time-series from a single date and value.
   * 
   * @param <V>  the value being viewed over time
   * @param date  the singleton date, not null
   * @param value  the singleton value
   * @return the time-series, not null
   */
  public static <V> ImmutableLocalDateObjectTimeSeries<V> of(LocalDate date, V value) {
    Objects.requireNonNull(date, "date");
    int[] timesArray = new int[] {LocalDateToIntConverter.convertToInt(date)};
    @SuppressWarnings("unchecked")
    V[] valuesArray = (V[]) new Object[] {value};
    return new ImmutableLocalDateObjectTimeSeries<V>(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from matching arrays of dates and values.
   * 
   * @param <V>  the value being viewed over time
   * @param dates  the date array, not null
   * @param values  the value array, not null
   * @return the time-series, not null
   */
  public static <V> ImmutableLocalDateObjectTimeSeries<V> of(LocalDate[] dates, V[] values) {
    int[] timesArray = convertToIntArray(dates);
    V[] valuesArray = values.clone();
    validate(timesArray, valuesArray);
    return new ImmutableLocalDateObjectTimeSeries<V>(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from matching arrays of dates and values.
   * 
   * @param <V>  the value being viewed over time
   * @param dates  the date array, not null
   * @param values  the value array, not null
   * @return the time-series, not null
   */
  public static <V> ImmutableLocalDateObjectTimeSeries<V> of(int[] dates, V[] values) {
    validate(dates, values);
    int[] timesArray = dates.clone();
    V[] valuesArray = values.clone();
    return new ImmutableLocalDateObjectTimeSeries<V>(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from matching arrays of dates and values.
   * 
   * @param <V>  the value being viewed over time
   * @param dates  the date list, not null
   * @param values  the value list, not null
   * @return the time-series, not null
   */
  public static <V> ImmutableLocalDateObjectTimeSeries<V> of(Collection<LocalDate> dates, Collection<V> values) {
    int[] timesArray = convertToIntArray(dates);
    @SuppressWarnings("unchecked")
    V[] valuesArray = (V[]) values.toArray();
    validate(timesArray, valuesArray);
    return new ImmutableLocalDateObjectTimeSeries<V>(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from another time-series.
   * 
   * @param <V>  the value being viewed over time
   * @param timeSeries  the time-series, not null
   * @return the time-series, not null
   */
  @SuppressWarnings("unchecked")
  public static <V> ImmutableLocalDateObjectTimeSeries<V> of(DateObjectTimeSeries<?, V> timeSeries) {
    if (timeSeries instanceof ImmutableLocalDateObjectTimeSeries) {
      return (ImmutableLocalDateObjectTimeSeries<V>) timeSeries;
    }
    DateObjectTimeSeries<?, V> other = (DateObjectTimeSeries<?, V>) timeSeries;
    int[] timesArray = other.timesArrayFast();
    V[] valuesArray = other.valuesArray();
    return new ImmutableLocalDateObjectTimeSeries<V>(timesArray, valuesArray);
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains a time-series from another time-series.
   * 
   * @param <V>  the value being viewed over time
   * @param timeSeries  the time-series, not null
   * @return the time-series, not null
   */
  public static <V> ImmutableLocalDateObjectTimeSeries<V> from(ObjectTimeSeries<LocalDate, V> timeSeries) {
    if (timeSeries instanceof DateObjectTimeSeries) {
      return of((DateObjectTimeSeries<LocalDate, V>) timeSeries);
    }
    int[] timesArray = convertToIntArray(timeSeries.timesArray());
    V[] valuesArray = timeSeries.valuesArray();
    return new ImmutableLocalDateObjectTimeSeries<V>(timesArray, valuesArray);
  }

  //-------------------------------------------------------------------------
  /**
   * Validates the data before creation.
   * 
   * @param <V>  the value being viewed over time
   * @param times  the times, not null
   * @param values  the values, not null
   */
  private static <V> void validate(int[] times, V[] values) {
    if (times == null || values == null) {
      throw new NullPointerException("Array must not be null");
    }
    // check lengths
    if (times.length != values.length) {
      throw new IllegalArgumentException("Arrays are of different sizes: " + times.length + ", " + values.length);
    }
    // check dates are ordered
    int maxTime = Integer.MIN_VALUE;
    for (int time : times) {
      if (time < maxTime) {
        throw new IllegalArgumentException("dates must be ordered");
      }
      maxTime = time;
    }
  }

  /**
   * Creates an instance.
   * 
   * @param times  the times, not null
   * @param values  the values, not null
   */
  ImmutableLocalDateObjectTimeSeries(int[] times, V[] values) {
    _times = times;
    _values = values;
  }

  //-------------------------------------------------------------------------
  static int[] convertToIntArray(Collection<LocalDate> times) {
    int[] timesArray = new int[times.size()];
    int i = 0;
    for (LocalDate time : times) {
      timesArray[i++] = LocalDateToIntConverter.convertToInt(time);
    }
    return timesArray;
  }

  static int[] convertToIntArray(LocalDate[] dates) {
    int[] timesArray = new int[dates.length];
    for (int i = 0; i < timesArray.length; i++) {
      timesArray[i] = LocalDateToIntConverter.convertToInt(dates[i]);
    }
    return timesArray;
  }

  static <V> Entry<LocalDate, V> makeMapEntry(LocalDate key, V value) {
    return new SimpleImmutableEntry<LocalDate, V>(key, value);
  }

  //-------------------------------------------------------------------------
  @Override
  protected int convertToInt(LocalDate date) {
    return LocalDateToIntConverter.convertToInt(date);
  }

  @Override
  protected LocalDate convertFromInt(int date) {
    return LocalDateToIntConverter.convertToLocalDate(date);
  }

  @Override
  protected LocalDate[] createArray(int size) {
    return new LocalDate[size];
  }

  //-------------------------------------------------------------------------
  @Override
  public int size() {
    return _times.length;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean containsTime(int date) {
    int binarySearch = Arrays.binarySearch(_times, date);
    return (binarySearch >= 0);
  }

  @Override
  public V getValue(int date) {
    int binarySearch = Arrays.binarySearch(_times, date);
    if (binarySearch >= 0) {
      return _values[binarySearch];
    } else {
      return null;
    }
  }

  @Override
  public int getTimeAtIndexFast(int index) {
    return _times[index];
  }

  @Override
  public V getValueAtIndex(int index) {
    return _values[index];
  }

  //-------------------------------------------------------------------------
  @Override
  public int getEarliestTimeFast() {
    try {
      return _times[0];
    } catch (IndexOutOfBoundsException ex) {
      throw new NoSuchElementException("Series is empty");
    }
  }

  @Override
  public V getEarliestValue() {
    try {
      return _values[0];
    } catch (IndexOutOfBoundsException ex) {
      throw new NoSuchElementException("Series is empty");
    }
  }

  @Override
  public int getLatestTimeFast() {
    try {
      return _times[_times.length - 1];
    } catch (IndexOutOfBoundsException ex) {
      throw new NoSuchElementException("Series is empty");
    }
  }

  @Override
  public V getLatestValue() {
    try {
      return _values[_values.length - 1];
    } catch (IndexOutOfBoundsException ex) {
      throw new NoSuchElementException("Series is empty");
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public int[] timesArrayFast() {
    return _times.clone();
  }

  @Override
  public V[] valuesArray() {
    return _values.clone();
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateObjectEntryIterator<V> iterator() {
    return new LocalDateObjectEntryIterator<V>() {
      private int _index = -1;

      @Override
      public boolean hasNext() {
        return (_index + 1) < size();
      }

      @Override
      public Entry<LocalDate, V> next() {
        if (hasNext() == false) {
          throw new NoSuchElementException("No more elements in the iteration");
        }
        _index++;
        int date = ImmutableLocalDateObjectTimeSeries.this.getTimeAtIndexFast(_index);
        V value = ImmutableLocalDateObjectTimeSeries.this.getValueAtIndex(_index);
        return makeMapEntry(ImmutableLocalDateObjectTimeSeries.this.convertFromInt(date), value);
      }

      @Override
      public int nextTimeFast() {
        if (hasNext() == false) {
          throw new NoSuchElementException("No more elements in the iteration");
        }
        _index++;
        return ImmutableLocalDateObjectTimeSeries.this.getTimeAtIndexFast(_index);
      }

      @Override
      public LocalDate nextTime() {
        return ImmutableLocalDateObjectTimeSeries.this.convertFromInt(nextTimeFast());
      }

      @Override
      public int currentTimeFast() {
        if (_index < 0) {
          throw new IllegalStateException("Iterator has not yet been started");
        }
        return ImmutableLocalDateObjectTimeSeries.this.getTimeAtIndexFast(_index);
      }

      @Override
      public LocalDate currentTime() {
        return ImmutableLocalDateObjectTimeSeries.this.convertFromInt(currentTimeFast());
      }

      @Override
      public V currentValue() {
        if (_index < 0) {
          throw new IllegalStateException("Iterator has not yet been started");
        }
        return ImmutableLocalDateObjectTimeSeries.this.getValueAtIndex(_index);
      }

      @Override
      public int currentIndex() {
        return _index;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Immutable iterator");
      }
    };
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateObjectTimeSeries<V> subSeries(LocalDate startTime, LocalDate endTime) {
    return subSeriesFast(convertToInt(startTime), true, convertToInt(endTime), false);
  }

  @Override
  public LocalDateObjectTimeSeries<V> subSeries(LocalDate startTime, boolean includeStart, LocalDate endTime, boolean includeEnd) {
    return subSeriesFast(convertToInt(startTime), includeStart, convertToInt(endTime), includeEnd);
  }

  @Override
  public LocalDateObjectTimeSeries<V> subSeriesFast(int startTime, int endTime) {
    return subSeriesFast(startTime, true, endTime, false);
  }

  @Override
  public LocalDateObjectTimeSeries<V> subSeriesFast(int startTime, boolean includeStart, int endTime, boolean includeEnd) {
    if (endTime < startTime) {
      throw new IllegalArgumentException("Invalid subSeries: endTime < startTime");
    }
    // special case for start equals end
    if (startTime == endTime) {
      if (includeStart && includeEnd) {
        int pos = Arrays.binarySearch(_times, startTime);
        if (pos >= 0) {
          return new ImmutableLocalDateObjectTimeSeries<V>(new int[] {startTime}, Arrays.copyOfRange(_values, pos, pos + 1));
        }
      }
      return ofEmpty();
    }
    // special case when this is empty
    if (isEmpty()) {
      return ofEmpty();
    }
    // normalize to include start and exclude end
    if (includeStart == false) {
      startTime++;
    }
    if (includeEnd) {
      if (endTime != Integer.MAX_VALUE) {
        endTime++;
      }
    }
    // calculate
    int startPos = Arrays.binarySearch(_times, startTime);
    startPos = startPos >= 0 ? startPos : -(startPos + 1);
    int endPos = Arrays.binarySearch(_times, endTime);
    endPos = endPos >= 0 ? endPos : -(endPos + 1);
    if (includeEnd && endTime == Integer.MAX_VALUE) {
      endPos = _times.length;
    }
    int[] timesArray = Arrays.copyOfRange(_times, startPos, endPos);
    V[] valuesArray = Arrays.copyOfRange(_values, startPos, endPos);
    return new ImmutableLocalDateObjectTimeSeries<V>(timesArray, valuesArray);
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateObjectTimeSeries<V> head(int numItems) {
    if (numItems == size()) {
      return this;
    }
    int[] timesArray = Arrays.copyOfRange(_times, 0, numItems);
    V[] valuesArray = Arrays.copyOfRange(_values, 0, numItems);
    return new ImmutableLocalDateObjectTimeSeries<V>(timesArray, valuesArray);
  }

  @Override
  public LocalDateObjectTimeSeries<V> tail(int numItems) {
    int size = size();
    if (numItems == size) {
      return this;
    }
    int[] timesArray = Arrays.copyOfRange(_times, size - numItems, size);
    V[] valuesArray = Arrays.copyOfRange(_values, size - numItems, size);
    return new ImmutableLocalDateObjectTimeSeries<V>(timesArray, valuesArray);
  }

  @Override
  @SuppressWarnings("unchecked")
  public LocalDateObjectTimeSeries<V> lag(int days) {
    int[] times = timesArrayFast();
    V[] values = valuesArray();
    if (days == 0) {
      return new ImmutableLocalDateObjectTimeSeries<V>(times, values);
    } else if (days < 0) {
      if (-days < times.length) {
        int[] resultTimes = new int[times.length + days]; // remember days is -ve
        System.arraycopy(times, 0, resultTimes, 0, times.length + days);
        V[] resultValues = (V[]) new Object[times.length + days];
        System.arraycopy(values, -days, resultValues, 0, times.length + days);
        return new ImmutableLocalDateObjectTimeSeries<V>(resultTimes, resultValues);
      } else {
        return ImmutableLocalDateObjectTimeSeries.ofEmpty();
      }
    } else { // if (days > 0) {
      if (days < times.length) {
        int[] resultTimes = new int[times.length - days]; // remember days is +ve
        System.arraycopy(times, days, resultTimes, 0, times.length - days);
        V[] resultValues = (V[]) new Object[times.length - days];
        System.arraycopy(values, 0, resultValues, 0, times.length - days);
        return new ImmutableLocalDateObjectTimeSeries<V>(resultTimes, resultValues);
      } else {
        return ImmutableLocalDateObjectTimeSeries.ofEmpty();
      }
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ImmutableLocalDateObjectTimeSeries<V> newInstance(LocalDate[] dates, V[] values) {
    return of(dates, values);
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateObjectTimeSeries<V> operate(UnaryOperator<V> operator) {
    V[] valuesArray = valuesArray();
    for (int i = 0; i < valuesArray.length; i++) {
      valuesArray[i] = operator.operate(valuesArray[i]);
    }
    return new ImmutableLocalDateObjectTimeSeries<V>(_times, valuesArray);  // immutable, so can share times
  }

  @Override
  public LocalDateObjectTimeSeries<V> operate(V other, BinaryOperator<V> operator) {
    V[] valuesArray = valuesArray();
    for (int i = 0; i < valuesArray.length; i++) {
      valuesArray[i] = operator.operate(valuesArray[i], other);
    }
    return new ImmutableLocalDateObjectTimeSeries<V>(_times, valuesArray);  // immutable, so can share times
  }

  @Override
  @SuppressWarnings("unchecked")
  public LocalDateObjectTimeSeries<V> operate(DateObjectTimeSeries<?, V> other, BinaryOperator<V> operator) {
    int[] aTimes = timesArrayFast();
    V[] aValues = valuesArray();
    int aCount = 0;
    int[] bTimes = other.timesArrayFast();
    V[] bValues = other.valuesArray();
    int bCount = 0;
    int[] resTimes = new int[aTimes.length + bTimes.length];
    V[] resValues = (V[]) new Object[resTimes.length];
    int resCount = 0;
    while (aCount < aTimes.length && bCount < bTimes.length) {
      if (aTimes[aCount] == bTimes[bCount]) {
        resTimes[resCount] = aTimes[aCount];
        resValues[resCount] = operator.operate(aValues[aCount], bValues[bCount]);
        resCount++;
        aCount++;
        bCount++;
      } else if (aTimes[aCount] < bTimes[bCount]) {
        aCount++;
      } else { // if (aTimes[aCount] > bTimes[bCount]) {
        bCount++;
      }
    }
    int[] trimmedTimes = new int[resCount];
    V[] trimmedValues = (V[]) new Object[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return new ImmutableLocalDateObjectTimeSeries<V>(trimmedTimes, trimmedValues);
  }

  @SuppressWarnings("unchecked")
  @Override
  public LocalDateObjectTimeSeries<V> unionOperate(DateObjectTimeSeries<?, V> other, BinaryOperator<V> operator) {
    int[] aTimes = timesArrayFast();
    V[] aValues = valuesArray();
    int aCount = 0;
    int[] bTimes = other.timesArrayFast();
    V[] bValues = other.valuesArray();
    int bCount = 0;
    int[] resTimes = new int[aTimes.length + bTimes.length];
    V[] resValues = (V[]) new Object[resTimes.length];
    int resCount = 0;
    while (aCount < aTimes.length || bCount < bTimes.length) {
      if (aCount >= aTimes.length) {
        int bRemaining = bTimes.length - bCount;
        System.arraycopy(bTimes, bCount, resTimes, resCount, bRemaining);
        System.arraycopy(bValues, bCount, resValues, resCount, bRemaining);
        resCount += bRemaining;
        break;
      } else if (bCount >= bTimes.length) {
        int aRemaining = aTimes.length - aCount;
        System.arraycopy(aTimes, aCount, resTimes, resCount, aRemaining);
        System.arraycopy(aValues, aCount, resValues, resCount, aRemaining);
        resCount += aRemaining;
        break;
      } else if (aTimes[aCount] == bTimes[bCount]) {
        resTimes[resCount] = aTimes[aCount];
        resValues[resCount] = operator.operate(aValues[aCount], bValues[bCount]);
        resCount++;
        aCount++;
        bCount++;
      } else if (aTimes[aCount] < bTimes[bCount]) {
        resTimes[resCount] = aTimes[aCount];
        resValues[resCount] = aValues[aCount];
        resCount++;
        aCount++;
      } else { // if (aTimes[aCount] > bTimes[bCount]) {
        resTimes[resCount] = bTimes[bCount];
        resValues[resCount] = bValues[bCount];
        resCount++;
        bCount++;
      }
    }
    int[] trimmedTimes = new int[resCount];
    V[] trimmedValues = (V[]) new Object[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return new ImmutableLocalDateObjectTimeSeries<V>(trimmedTimes, trimmedValues);
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateObjectTimeSeries<V> intersectionFirstValue(DateObjectTimeSeries<?, V> other) {
    return operate(other, ObjectTimeSeriesOperators.<V>firstOperator());
  }

  @Override
  public LocalDateObjectTimeSeries<V> intersectionSecondValue(DateObjectTimeSeries<?, V> other) {
    return operate(other, ObjectTimeSeriesOperators.<V>secondOperator());
  }

  @Override
  public LocalDateObjectTimeSeries<V> noIntersectionOperation(DateObjectTimeSeries<?, V> other) {
    return unionOperate(other, ObjectTimeSeriesOperators.<V>noIntersectionOperator());
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateObjectTimeSeriesBuilder<V> toBuilder() {
    return ImmutableLocalDateObjectTimeSeries.<V>builder().putAll(this);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof ImmutableLocalDateObjectTimeSeries) {
      ImmutableLocalDateObjectTimeSeries<?> other = (ImmutableLocalDateObjectTimeSeries<?>) obj;
      return Arrays.equals(_times, other._times) &&
              Arrays.equals(_values, other._values);
    }
    if (obj instanceof DateObjectTimeSeries) {
      DateObjectTimeSeries<?, ?> other = (DateObjectTimeSeries<?, ?>) obj;
      return Arrays.equals(timesArrayFast(), other.timesArrayFast()) &&
              Arrays.equals(valuesArray(), other.valuesArray());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(timesArrayFast()) ^ Arrays.hashCode(valuesArray());
  }

}
