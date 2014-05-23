/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.zdt;

import java.io.Serializable;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.ObjectTimeSeries;
import com.opengamma.timeseries.ObjectTimeSeriesOperators;
import com.opengamma.timeseries.ObjectTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.ObjectTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.precise.AbstractPreciseObjectTimeSeries;
import com.opengamma.timeseries.precise.PreciseObjectTimeSeries;

/**
 * Standard immutable implementation of {@code ZonedDateTimeObjectTimeSeries}.
 * 
 * @param <V>  the value being viewed over time
 */
public final class ImmutableZonedDateTimeObjectTimeSeries<V>
    extends AbstractPreciseObjectTimeSeries<ZonedDateTime, V>
    implements ZonedDateTimeObjectTimeSeries<V>, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = -43654613865187568L;

  /**
   * The time-zone.
   */
  private final ZoneId _zone;
  /**
   * The times in the series.
   */
  private final long[] _times;
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
   * @param zone  the time-zone, not null
   * @return the time-series builder, not null
   */
  public static <V> ZonedDateTimeObjectTimeSeriesBuilder<V> builder(ZoneId zone) {
    return new ImmutableZonedDateTimeObjectTimeSeriesBuilder<V>(zone);
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains a time-series from a single date and value.
   * 
   * @param <V>  the value being viewed over time
   * @param zone  the time-zone, not null
   * @return the time-series, not null
   */
  @SuppressWarnings("unchecked")
  public static <V> ImmutableZonedDateTimeObjectTimeSeries<V> ofEmpty(ZoneId zone) {
    Objects.requireNonNull(zone, "zone");
    return new ImmutableZonedDateTimeObjectTimeSeries<>(new long[0], (V[]) new Object[0], zone);
  }

  /**
   * Obtains a time-series from a single instant and value.
   * 
   * @param <V>  the value being viewed over time
   * @param instant  the singleton instant, not null
   * @param value  the singleton value
   * @return the time-series, not null
   */
  public static <V> ImmutableZonedDateTimeObjectTimeSeries<V> of(ZonedDateTime instant, V value) {
    Objects.requireNonNull(instant, "instant");
    long[] timesArray = new long[] {ZonedDateTimeToLongConverter.convertToLong(instant)};
    @SuppressWarnings("unchecked")
    V[] valuesArray = (V[]) new Object[] {value};
    return new ImmutableZonedDateTimeObjectTimeSeries<V>(timesArray, valuesArray, instant.getZone());
  }

  /**
   * Obtains a time-series from matching arrays of instants and values.
   * 
   * @param <V>  the value being viewed over time
   * @param instants  the instant array, not null
   * @param values  the value array, not null
   * @param zone  the time-zone, may be null if the arrays are non-empty
   * @return the time-series, not null
   * @throws IllegalArgumentException if the arrays are of different lengths
   */
  public static <V> ImmutableZonedDateTimeObjectTimeSeries<V> of(ZonedDateTime[] instants, V[] values, ZoneId zone) {
    long[] timesArray = convertToLongArray(instants);
    V[] valuesArray = values.clone();
    validate(timesArray, valuesArray);
    zone = (zone != null ? zone : instants[0].getZone());
    return new ImmutableZonedDateTimeObjectTimeSeries<V>(timesArray, valuesArray, zone);
  }

  /**
   * Obtains a time-series from matching arrays of instants and values.
   * 
   * @param <V>  the value being viewed over time
   * @param instants  the instant array, not null
   * @param values  the value array, not null
   * @param zone  the time-zone, not null
   * @return the time-series, not null
   * @throws IllegalArgumentException if the arrays are of different lengths
   */
  public static <V> ImmutableZonedDateTimeObjectTimeSeries<V> of(long[] instants, V[] values, ZoneId zone) {
    validate(instants, values);
    Objects.requireNonNull(zone, "zone");
    long[] timesArray = instants.clone();
    V[] valuesArray = values.clone();
    return new ImmutableZonedDateTimeObjectTimeSeries<V>(timesArray, valuesArray, zone);
  }

  /**
   * Obtains a time-series from matching arrays of instants and values.
   * 
   * @param <V>  the value being viewed over time
   * @param instants  the instant list, not null
   * @param values  the value list, not null
   * @param zone  the time-zone, may be null if the collections are non-empty
   * @return the time-series, not null
   * @throws IllegalArgumentException if the collections are of different lengths
   */
  public static <V> ImmutableZonedDateTimeObjectTimeSeries<V> of(Collection<ZonedDateTime> instants, Collection<V> values, ZoneId zone) {
    long[] timesArray = convertToLongArray(instants);
    @SuppressWarnings("unchecked")
    V[] valuesArray = (V[]) values.toArray();
    validate(timesArray, valuesArray);
    zone = (zone != null ? zone : instants.iterator().next().getZone());
    return new ImmutableZonedDateTimeObjectTimeSeries<V>(timesArray, valuesArray, zone);
  }

  /**
   * Obtains a time-series from another time-series.
   * 
   * @param <V>  the value being viewed over time
   * @param timeSeries  the time-series, not null
   * @param zone  the time-zone, not null
   * @return the time-series, not null
   */
  @SuppressWarnings("unchecked")
  public static <V> ImmutableZonedDateTimeObjectTimeSeries<V> of(PreciseObjectTimeSeries<?, V> timeSeries, ZoneId zone) {
    Objects.requireNonNull(zone, "zone");
    if (timeSeries instanceof ImmutableZonedDateTimeObjectTimeSeries &&
        ((ImmutableZonedDateTimeObjectTimeSeries<V>) timeSeries).getZone().equals(zone)) {
      return (ImmutableZonedDateTimeObjectTimeSeries<V>) timeSeries;
    }
    PreciseObjectTimeSeries<?, V> other = (PreciseObjectTimeSeries<?, V>) timeSeries;
    long[] timesArray = other.timesArrayFast();
    V[] valuesArray = other.valuesArray();
    return new ImmutableZonedDateTimeObjectTimeSeries<V>(timesArray, valuesArray, zone);
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains a time-series from another time-series.
   * 
   * @param <V>  the value being viewed over time
   * @param timeSeries  the time-series, not null
   * @param zone  the time-zone, not null
   * @return the time-series, not null
   */
  public static <V> ImmutableZonedDateTimeObjectTimeSeries<V> from(ObjectTimeSeries<ZonedDateTime, V> timeSeries, ZoneId zone) {
    Objects.requireNonNull(zone, "zone");
    if (timeSeries instanceof PreciseObjectTimeSeries) {
      return of((PreciseObjectTimeSeries<ZonedDateTime, V>) timeSeries, zone);
    }
    long[] timesArray = convertToLongArray(timeSeries.timesArray());
    V[] valuesArray = timeSeries.valuesArray();
    return new ImmutableZonedDateTimeObjectTimeSeries<V>(timesArray, valuesArray, zone);
  }

  //-------------------------------------------------------------------------
  /**
   * Validates the data before creation.
   * 
   * @param <V>  the value being viewed over time
   * @param instants  the times, not null
   * @param values  the values, not null
   */
  private static <V> void validate(long[] instants, V[] values) {
    if (instants == null || values == null) {
      throw new NullPointerException("Array must not be null");
    }
    // check lengths
    if (instants.length != values.length) {
      throw new IllegalArgumentException("Arrays are of different sizes: " + instants.length + ", " + values.length);
    }
    // check dates are ordered
    long maxTime = Long.MIN_VALUE;
    for (long time : instants) {
      if (time < maxTime) {
        throw new IllegalArgumentException("ZonedDateTimes must be ordered");
      }
      maxTime = time;
    }
  }

  /**
   * Creates an instance.
   * 
   * @param instants  the times, not null
   * @param values  the values, not null
   * @param zone  the time-zone, not null
   */
  ImmutableZonedDateTimeObjectTimeSeries(long[] instants, V[] values, ZoneId zone) {
    _times = instants;
    _values = values;
    _zone = zone;
  }

  //-------------------------------------------------------------------------
  static long[] convertToLongArray(Collection<ZonedDateTime> instants) {
    long[] timesArray = new long[instants.size()];
    int i = 0;
    for (ZonedDateTime instant : instants) {
      timesArray[i++] = ZonedDateTimeToLongConverter.convertToLong(instant);
    }
    return timesArray;
  }

  static long[] convertToLongArray(ZonedDateTime[] instants) {
    long[] timesArray = new long[instants.length];
    for (int i = 0; i < timesArray.length; i++) {
      timesArray[i] = ZonedDateTimeToLongConverter.convertToLong(instants[i]);
    }
    return timesArray;
  }

  static <V> Entry<ZonedDateTime, V> makeMapEntry(ZonedDateTime key, V value) {
    return new SimpleImmutableEntry<ZonedDateTime, V>(key, value);
  }

  //-------------------------------------------------------------------------
  @Override
  protected long convertToLong(ZonedDateTime instant) {
    return ZonedDateTimeToLongConverter.convertToLong(instant);
  }

  @Override
  protected ZonedDateTime convertFromLong(long instant) {
    return ZonedDateTimeToLongConverter.convertToZonedDateTime(instant, getZone());
  }

  @Override
  protected ZonedDateTime[] createArray(int size) {
    return new ZonedDateTime[size];
  }

  //-------------------------------------------------------------------------
  @Override
  public ZoneId getZone() {
    return _zone;
  }

  @Override
  public ZonedDateTimeObjectTimeSeries<V> withZone(ZoneId zone) {
    Objects.requireNonNull(zone, "zone");
    if (zone.equals(_zone)) {
      return this;
    }
    // immutable, so can share arrays
    return new ImmutableZonedDateTimeObjectTimeSeries<V>(_times, _values, zone);
  }

  //-------------------------------------------------------------------------
  @Override
  public int size() {
    return _times.length;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean containsTime(long instant) {
    int binarySearch = Arrays.binarySearch(_times, instant);
    return (binarySearch >= 0);
  }

  @Override
  public V getValue(long instant) {
    int binarySearch = Arrays.binarySearch(_times, instant);
    if (binarySearch >= 0) {
      return _values[binarySearch];
    } else {
      return null;
    }
  }

  @Override
  public long getTimeAtIndexFast(int index) {
    return _times[index];
  }

  @Override
  public V getValueAtIndex(int index) {
    return _values[index];
  }

  //-------------------------------------------------------------------------
  @Override
  public long getEarliestTimeFast() {
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
  public long getLatestTimeFast() {
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
  public long[] timesArrayFast() {
    return _times.clone();
  }

  @Override
  public V[] valuesArray() {
    return _values.clone();
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeObjectEntryIterator<V> iterator() {
    return new ZonedDateTimeObjectEntryIterator<V>() {
      private int _index = -1;

      @Override
      public boolean hasNext() {
        return (_index + 1) < size();
      }

      @Override
      public Entry<ZonedDateTime, V> next() {
        if (hasNext() == false) {
          throw new NoSuchElementException("No more elements in the iteration");
        }
        _index++;
        long date = ImmutableZonedDateTimeObjectTimeSeries.this.getTimeAtIndexFast(_index);
        V value = ImmutableZonedDateTimeObjectTimeSeries.this.getValueAtIndex(_index);
        return makeMapEntry(ImmutableZonedDateTimeObjectTimeSeries.this.convertFromLong(date), value);
      }

      @Override
      public long nextTimeFast() {
        if (hasNext() == false) {
          throw new NoSuchElementException("No more elements in the iteration");
        }
        _index++;
        return ImmutableZonedDateTimeObjectTimeSeries.this.getTimeAtIndexFast(_index);
      }

      @Override
      public ZonedDateTime nextTime() {
        return ImmutableZonedDateTimeObjectTimeSeries.this.convertFromLong(nextTimeFast());
      }

      @Override
      public long currentTimeFast() {
        if (_index < 0) {
          throw new IllegalStateException("Iterator has not yet been started");
        }
        return ImmutableZonedDateTimeObjectTimeSeries.this.getTimeAtIndexFast(_index);
      }

      @Override
      public ZonedDateTime currentTime() {
        return ImmutableZonedDateTimeObjectTimeSeries.this.convertFromLong(currentTimeFast());
      }

      @Override
      public V currentValue() {
        if (_index < 0) {
          throw new IllegalStateException("Iterator has not yet been started");
        }
        return ImmutableZonedDateTimeObjectTimeSeries.this.getValueAtIndex(_index);
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
  public ZonedDateTimeObjectTimeSeries<V> subSeries(ZonedDateTime startZonedDateTime, ZonedDateTime endZonedDateTime) {
    return subSeriesFast(convertToLong(startZonedDateTime), true, convertToLong(endZonedDateTime), false);
  }

  @Override
  public ZonedDateTimeObjectTimeSeries<V> subSeries(ZonedDateTime startZonedDateTime, boolean includeStart, ZonedDateTime endZonedDateTime, boolean includeEnd) {
    return subSeriesFast(convertToLong(startZonedDateTime), includeStart, convertToLong(endZonedDateTime), includeEnd);
  }

  @Override
  public ZonedDateTimeObjectTimeSeries<V> subSeriesFast(long startZonedDateTime, long endZonedDateTime) {
    return subSeriesFast(startZonedDateTime, true, endZonedDateTime, false);
  }

  @Override
  public ZonedDateTimeObjectTimeSeries<V> subSeriesFast(long startZonedDateTime, boolean includeStart, long endZonedDateTime, boolean includeEnd) {
    if (endZonedDateTime < startZonedDateTime) {
      throw new IllegalArgumentException("Invalid subSeries: endTime < startTime");
    }
    // special case for start equals end
    if (startZonedDateTime == endZonedDateTime) {
      if (includeStart && includeEnd) {
        int pos = Arrays.binarySearch(_times, startZonedDateTime);
        if (pos >= 0) {
          return new ImmutableZonedDateTimeObjectTimeSeries<V>(new long[] {startZonedDateTime}, Arrays.copyOfRange(_values, pos, pos + 1), _zone);
        }
      }
      return ofEmpty(_zone);
    }
    // special case when this is empty
    if (isEmpty()) {
      return ofEmpty(_zone);
    }
    // normalize to include start and exclude end
    if (includeStart == false) {
      startZonedDateTime++;
    }
    if (includeEnd) {
      if (endZonedDateTime != Long.MAX_VALUE) {
        endZonedDateTime++;
      }
    }
    // calculate
    int startPos = Arrays.binarySearch(_times, startZonedDateTime);
    startPos = startPos >= 0 ? startPos : -(startPos + 1);
    int endPos = Arrays.binarySearch(_times, endZonedDateTime);
    endPos = endPos >= 0 ? endPos : -(endPos + 1);
    if (includeEnd && endZonedDateTime == Long.MAX_VALUE) {
      endPos = _times.length;
    }
    long[] timesArray = Arrays.copyOfRange(_times, startPos, endPos);
    V[] valuesArray = Arrays.copyOfRange(_values, startPos, endPos);
    return new ImmutableZonedDateTimeObjectTimeSeries<V>(timesArray, valuesArray, _zone);
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeObjectTimeSeries<V> head(int numItems) {
    if (numItems == size()) {
      return this;
    }
    long[] timesArray = Arrays.copyOfRange(_times, 0, numItems);
    V[] valuesArray = Arrays.copyOfRange(_values, 0, numItems);
    return new ImmutableZonedDateTimeObjectTimeSeries<V>(timesArray, valuesArray, _zone);
  }

  @Override
  public ZonedDateTimeObjectTimeSeries<V> tail(int numItems) {
    int size = size();
    if (numItems == size) {
      return this;
    }
    long[] timesArray = Arrays.copyOfRange(_times, size - numItems, size);
    V[] valuesArray = Arrays.copyOfRange(_values, size - numItems, size);
    return new ImmutableZonedDateTimeObjectTimeSeries<V>(timesArray, valuesArray, _zone);
  }

  @Override
  @SuppressWarnings("unchecked")
  public ZonedDateTimeObjectTimeSeries<V> lag(int days) {
    long[] times = timesArrayFast();
    V[] values = valuesArray();
    if (days == 0) {
      return new ImmutableZonedDateTimeObjectTimeSeries<V>(times, values, _zone);
    } else if (days < 0) {
      if (-days < times.length) {
        long[] resultTimes = new long[times.length + days]; // remember days is -ve
        System.arraycopy(times, 0, resultTimes, 0, times.length + days);
        V[] resultValues = (V[]) new Object[times.length + days];
        System.arraycopy(values, -days, resultValues, 0, times.length + days);
        return new ImmutableZonedDateTimeObjectTimeSeries<V>(resultTimes, resultValues, _zone);
      } else {
        return ImmutableZonedDateTimeObjectTimeSeries.ofEmpty(_zone);
      }
    } else { // if (days > 0) {
      if (days < times.length) {
        long[] resultTimes = new long[times.length - days]; // remember days is +ve
        System.arraycopy(times, days, resultTimes, 0, times.length - days);
        V[] resultValues = (V[]) new Object[times.length - days];
        System.arraycopy(values, 0, resultValues, 0, times.length - days);
        return new ImmutableZonedDateTimeObjectTimeSeries<V>(resultTimes, resultValues, _zone);
      } else {
        return ImmutableZonedDateTimeObjectTimeSeries.ofEmpty(_zone);
      }
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public ImmutableZonedDateTimeObjectTimeSeries<V> newInstance(ZonedDateTime[] dates, V[] values) {
    return of(dates, values, _zone);
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeObjectTimeSeries<V> operate(UnaryOperator<V> operator) {
    V[] valuesArray = valuesArray();
    for (int i = 0; i < valuesArray.length; i++) {
      valuesArray[i] = operator.operate(valuesArray[i]);
    }
    return new ImmutableZonedDateTimeObjectTimeSeries<V>(_times, valuesArray, _zone);  // immutable, so can share times
  }

  @Override
  public ZonedDateTimeObjectTimeSeries<V> operate(V other, BinaryOperator<V> operator) {
    V[] valuesArray = valuesArray();
    for (int i = 0; i < valuesArray.length; i++) {
      valuesArray[i] = operator.operate(valuesArray[i], other);
    }
    return new ImmutableZonedDateTimeObjectTimeSeries<V>(_times, valuesArray, _zone);  // immutable, so can share times
  }

  @Override
  @SuppressWarnings("unchecked")
  public ZonedDateTimeObjectTimeSeries<V> operate(PreciseObjectTimeSeries<?, V> other, BinaryOperator<V> operator) {
    long[] aTimes = timesArrayFast();
    V[] aValues = valuesArray();
    int aCount = 0;
    long[] bTimes = other.timesArrayFast();
    V[] bValues = other.valuesArray();
    int bCount = 0;
    long[] resTimes = new long[aTimes.length + bTimes.length];
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
    long[] trimmedTimes = new long[resCount];
    V[] trimmedValues = (V[]) new Object[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return new ImmutableZonedDateTimeObjectTimeSeries<V>(trimmedTimes, trimmedValues, _zone);
  }

  @SuppressWarnings("unchecked")
  @Override
  public ZonedDateTimeObjectTimeSeries<V> unionOperate(PreciseObjectTimeSeries<?, V> other, BinaryOperator<V> operator) {
    long[] aTimes = timesArrayFast();
    V[] aValues = valuesArray();
    int aCount = 0;
    long[] bTimes = other.timesArrayFast();
    V[] bValues = other.valuesArray();
    int bCount = 0;
    long[] resTimes = new long[aTimes.length + bTimes.length];
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
    long[] trimmedTimes = new long[resCount];
    V[] trimmedValues = (V[]) new Object[resCount];
    System.arraycopy(resTimes, 0, trimmedTimes, 0, resCount);
    System.arraycopy(resValues, 0, trimmedValues, 0, resCount);
    return new ImmutableZonedDateTimeObjectTimeSeries<V>(trimmedTimes, trimmedValues, _zone);
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeObjectTimeSeries<V> intersectionFirstValue(PreciseObjectTimeSeries<?, V> other) {
    return operate(other, ObjectTimeSeriesOperators.<V>firstOperator());
  }

  @Override
  public ZonedDateTimeObjectTimeSeries<V> intersectionSecondValue(PreciseObjectTimeSeries<?, V> other) {
    return operate(other, ObjectTimeSeriesOperators.<V>secondOperator());
  }

  @Override
  public ZonedDateTimeObjectTimeSeries<V> noIntersectionOperation(PreciseObjectTimeSeries<?, V> other) {
    return unionOperate(other, ObjectTimeSeriesOperators.<V>noIntersectionOperator());
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeObjectTimeSeriesBuilder<V> toBuilder() {
    return ImmutableZonedDateTimeObjectTimeSeries.<V>builder(_zone).putAll(this);
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof ImmutableZonedDateTimeObjectTimeSeries) {
      ImmutableZonedDateTimeObjectTimeSeries<?> other = (ImmutableZonedDateTimeObjectTimeSeries<?>) obj;
      return Arrays.equals(_times, other._times) &&
              Arrays.equals(_values, other._values);
    }
    if (obj instanceof PreciseObjectTimeSeries) {
      PreciseObjectTimeSeries<?, ?> other = (PreciseObjectTimeSeries<?, ?>) obj;
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
