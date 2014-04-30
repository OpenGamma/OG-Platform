/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.zdt;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.precise.PreciseDoubleTimeSeries;

/**
 * Standard immutable implementation of {@code ZonedDateTimeDoubleTimeSeries}.
 */
public final class ImmutableZonedDateTimeDoubleTimeSeries
    extends AbstractZonedDateTimeDoubleTimeSeries
    implements ZonedDateTimeDoubleTimeSeries, Serializable {

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
  private final double[] _values;

  //-------------------------------------------------------------------------
  /**
   * Creates an empty builder, used to create time-series.
   * <p>
   * The builder has methods to create and modify a time-series.
   * 
   * @param zone  the time-zone, not null
   * @return the time-series builder, not null
   */
  public static ZonedDateTimeDoubleTimeSeriesBuilder builder(ZoneId zone) {
    return new ImmutableZonedDateTimeDoubleTimeSeriesBuilder(zone);
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains an empty time-series.
   * 
   * @param zone  the time-zone, not null
   * @return the time-series, not null
   */
  public static ImmutableZonedDateTimeDoubleTimeSeries ofEmpty(ZoneId zone) {
    Objects.requireNonNull(zone, "zone");
    return new ImmutableZonedDateTimeDoubleTimeSeries(new long[0], new double[0], zone);
  }

  /**
   * Obtains an empty time-series using a UTC zone.
   * 
   * @return the time-series, not null
   */
  public static ImmutableZonedDateTimeDoubleTimeSeries ofEmptyUTC() {
    return ofEmpty(ZoneOffset.UTC);
  }

  /**
   * Obtains a time-series from a single instant and value.
   * 
   * @param instant  the singleton instant, not null
   * @param value  the singleton value
   * @return the time-series, not null
   */
  public static ImmutableZonedDateTimeDoubleTimeSeries of(ZonedDateTime instant, double value) {
    Objects.requireNonNull(instant, "instant");
    long[] timesArray = new long[] {ZonedDateTimeToLongConverter.convertToLong(instant)};
    double[] valuesArray = new double[] {value};
    return new ImmutableZonedDateTimeDoubleTimeSeries(timesArray, valuesArray, instant.getZone());
  }

  /**
   * Obtains a time-series from matching arrays of instants and values.
   * 
   * @param instants  the date array, not null
   * @param values  the value array, not null
   * @param zone  the time-zone, may be null if the arrays are non-empty
   * @return the time-series, not null
   * @throws IllegalArgumentException if the arrays are of different lengths
   */
  public static ImmutableZonedDateTimeDoubleTimeSeries of(ZonedDateTime[] instants, Double[] values, ZoneId zone) {
    long[] timesArray = convertToLongArray(instants);
    double[] valuesArray = convertToDoubleArray(values);
    validate(timesArray, valuesArray);
    zone = (zone != null ? zone : instants[0].getZone());
    return new ImmutableZonedDateTimeDoubleTimeSeries(timesArray, valuesArray, zone);
  }

  /**
   * Obtains a time-series from matching arrays of instants and values, using a UTC zone.
   * 
   * @param instants  the instant array, not null
   * @param values  the value array, not null
   * @return the time-series, not null
   * @throws IllegalArgumentException if the arrays are of different lengths
   */
  public static ImmutableZonedDateTimeDoubleTimeSeries ofUTC(ZonedDateTime[] instants, Double[] values) {
    return of(instants, values, ZoneOffset.UTC);
  }

  /**
   * Obtains a time-series from matching arrays of instants and values.
   * 
   * @param instants  the instant array, not null
   * @param values  the value array, not null
   * @param zone  the time-zone, may be null if the arrays are non-empty
   * @return the time-series, not null
   * @throws IllegalArgumentException if the arrays are of different lengths
   */
  public static ImmutableZonedDateTimeDoubleTimeSeries of(ZonedDateTime[] instants, double[] values, ZoneId zone) {
    long[] timesArray = convertToLongArray(instants);
    validate(timesArray, values);
    double[] valuesArray = values.clone();
    zone = (zone != null ? zone : instants[0].getZone());
    return new ImmutableZonedDateTimeDoubleTimeSeries(timesArray, valuesArray, zone);
  }

  /**
   * Obtains a time-series from matching arrays of instants and values, using a UTC zone.
   * 
   * @param instants  the instant array, not null
   * @param values  the value array, not null
   * @return the time-series, not null
   * @throws IllegalArgumentException if the arrays are of different lengths
   */
  public static ImmutableZonedDateTimeDoubleTimeSeries ofUTC(ZonedDateTime[] instants, double[] values) {
    return of(instants, values, ZoneOffset.UTC);
  }

  /**
   * Obtains a time-series from matching arrays of instants and values.
   * 
   * @param instants  the instant array, not null
   * @param values  the value array, not null
   * @param zone  the time-zone, not null
   * @return the time-series, not null
   * @throws IllegalArgumentException if the arrays are of different lengths
   */
  public static ImmutableZonedDateTimeDoubleTimeSeries of(long[] instants, double[] values, ZoneId zone) {
    validate(instants, values);
    Objects.requireNonNull(zone);
    long[] timesArray = instants.clone();
    double[] valuesArray = values.clone();
    return new ImmutableZonedDateTimeDoubleTimeSeries(timesArray, valuesArray, zone);
  }

  /**
   * Obtains a time-series from matching arrays of instants and values, using a UTC zone.
   * 
   * @param instants  the instant array, not null
   * @param values  the value array, not null
   * @return the time-series, not null
   * @throws IllegalArgumentException if the arrays are of different lengths
   */
  public static ImmutableZonedDateTimeDoubleTimeSeries ofUTC(long[] instants, double[] values) {
    return of(instants, values, ZoneOffset.UTC);
  }

  /**
   * Obtains a time-series from matching arrays of instants and values.
   * 
   * @param instants  the instant list, not null
   * @param values  the value list, not null
   * @param zone  the time-zone, may be null if the collections are non-empty
   * @return the time-series, not null
   * @throws IllegalArgumentException if the collections are of different lengths
   */
  public static ImmutableZonedDateTimeDoubleTimeSeries of(Collection<ZonedDateTime> instants, Collection<Double> values, ZoneId zone) {
    long[] timesArray = convertToLongArray(instants);
    double[] valuesArray = convertToDoubleArray(values);
    zone = (zone != null ? zone : instants.iterator().next().getZone());
    validate(timesArray, valuesArray);
    return new ImmutableZonedDateTimeDoubleTimeSeries(timesArray, valuesArray, zone);
  }

  /**
   * Obtains a time-series from matching arrays of instants and values, using a UTC zone.
   * 
   * @param instants  the instant list, not null
   * @param values  the value list, not null
   * @return the time-series, not null
   * @throws IllegalArgumentException if the arrays are of different lengths
   */
  public static ImmutableZonedDateTimeDoubleTimeSeries ofUTC(Collection<ZonedDateTime> instants, Collection<Double> values) {
    return of(instants, values, ZoneOffset.UTC);
  }

  /**
   * Obtains a time-series from another time-series.
   * 
   * @param timeSeries  the time-series, not null
   * @param zone  the time-zone, not null
   * @return the time-series, not null
   */
  public static ImmutableZonedDateTimeDoubleTimeSeries of(PreciseDoubleTimeSeries<?> timeSeries, ZoneId zone) {
    Objects.requireNonNull(zone, "zone");
    if (timeSeries instanceof ImmutableZonedDateTimeDoubleTimeSeries &&
        ((ImmutableZonedDateTimeDoubleTimeSeries) timeSeries).getZone().equals(zone)) {
      return (ImmutableZonedDateTimeDoubleTimeSeries) timeSeries;
    }
    PreciseDoubleTimeSeries<?> other = (PreciseDoubleTimeSeries<?>) timeSeries;
    long[] timesArray = other.timesArrayFast();
    double[] valuesArray = other.valuesArrayFast();
    return new ImmutableZonedDateTimeDoubleTimeSeries(timesArray, valuesArray, zone);
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains a time-series from another time-series.
   * 
   * @param timeSeries  the time-series, not null
   * @param zone  the time-zone, not null
   * @return the time-series, not null
   */
  public static ImmutableZonedDateTimeDoubleTimeSeries from(DoubleTimeSeries<ZonedDateTime> timeSeries, ZoneId zone) {
    Objects.requireNonNull(zone, "zone");
    if (timeSeries instanceof PreciseDoubleTimeSeries) {
      return of((PreciseDoubleTimeSeries<?>) timeSeries, zone);
    }
    long[] timesArray = convertToLongArray(timeSeries.timesArray());
    double[] valuesArray = timeSeries.valuesArrayFast();
    return new ImmutableZonedDateTimeDoubleTimeSeries(timesArray, valuesArray, zone);
  }

  //-------------------------------------------------------------------------
  /**
   * Validates the data before creation.
   * 
   * @param instants  the times, not null
   * @param values  the values, not null
   */
  private static void validate(long[] instants, double[] values) {
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
  ImmutableZonedDateTimeDoubleTimeSeries(long[] instants, double[] values, ZoneId zone) {
    _times = instants;
    _values = values;
    _zone = zone;
  }

  //-------------------------------------------------------------------------
  @Override
  long[] timesArrayFast0() {
    return _times;
  }

  @Override
  double[] valuesArrayFast0() {
    return _values;
  }

  @Override
  ZonedDateTimeDoubleTimeSeries newInstanceFast(long[] instant, double[] values) {
    return new ImmutableZonedDateTimeDoubleTimeSeries(instant, values, _zone);
  }

  //-------------------------------------------------------------------------
  @Override
  public ZoneId getZone() {
    return _zone;
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries withZone(ZoneId zone) {
    Objects.requireNonNull(zone, "zone");
    if (zone.equals(_zone)) {
      return this;
    }
    // immutable, so can share arrays
    return new ImmutableZonedDateTimeDoubleTimeSeries(_times, _values, zone);
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
  public Double getValue(long instant) {
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
  public double getValueAtIndexFast(int index) {
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
  public double getEarliestValueFast() {
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
  public double getLatestValueFast() {
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
  public double[] valuesArrayFast() {
    return _values.clone();
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeDoubleTimeSeries subSeriesFast(long startTime, boolean includeStart, long endTime, boolean includeEnd) {
    if (endTime < startTime) {
      throw new IllegalArgumentException("Invalid subSeries: endTime < startTime");
    }
    // special case for start equals end
    if (startTime == endTime) {
      if (includeStart && includeEnd) {
        int pos = Arrays.binarySearch(_times, startTime);
        if (pos >= 0) {
          return new ImmutableZonedDateTimeDoubleTimeSeries(new long[] {startTime}, new double[] {_values[pos]}, _zone);
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
      startTime++;
    }
    if (includeEnd) {
      if (endTime != Long.MAX_VALUE) {
        endTime++;
      }
    }
    // calculate
    int startPos = Arrays.binarySearch(_times, startTime);
    startPos = startPos >= 0 ? startPos : -(startPos + 1);
    int endPos = Arrays.binarySearch(_times, endTime);
    endPos = endPos >= 0 ? endPos : -(endPos + 1);
    if (includeEnd && endTime == Long.MAX_VALUE) {
      endPos = _times.length;
    }
    long[] timesArray = Arrays.copyOfRange(_times, startPos, endPos);
    double[] valuesArray = Arrays.copyOfRange(_values, startPos, endPos);
    return new ImmutableZonedDateTimeDoubleTimeSeries(timesArray, valuesArray, _zone);
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeDoubleTimeSeries head(int numItems) {
    if (numItems == size()) {
      return this;
    }
    long[] timesArray = Arrays.copyOfRange(_times, 0, numItems);
    double[] valuesArray = Arrays.copyOfRange(_values, 0, numItems);
    return new ImmutableZonedDateTimeDoubleTimeSeries(timesArray, valuesArray, _zone);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries tail(int numItems) {
    int size = size();
    if (numItems == size) {
      return this;
    }
    long[] timesArray = Arrays.copyOfRange(_times, size - numItems, size);
    double[] valuesArray = Arrays.copyOfRange(_values, size - numItems, size);
    return new ImmutableZonedDateTimeDoubleTimeSeries(timesArray, valuesArray, _zone);
  }

  //-------------------------------------------------------------------------
  @Override
  public ImmutableZonedDateTimeDoubleTimeSeries newInstance(ZonedDateTime[] instants, Double[] values) {
    return of(instants, values, _zone);
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeDoubleTimeSeries operate(UnaryOperator operator) {
    double[] valuesArray = valuesArrayFast();
    for (int i = 0; i < valuesArray.length; i++) {
      valuesArray[i] = operator.operate(valuesArray[i]);
    }
    return new ImmutableZonedDateTimeDoubleTimeSeries(_times, valuesArray, _zone);  // immutable, so can share times
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries operate(double other, BinaryOperator operator) {
    double[] valuesArray = valuesArrayFast();
    for (int i = 0; i < valuesArray.length; i++) {
      valuesArray[i] = operator.operate(valuesArray[i], other);
    }
    return new ImmutableZonedDateTimeDoubleTimeSeries(_times, valuesArray, _zone);  // immutable, so can share times
  }

  //-------------------------------------------------------------------------
  @Override
  public ZonedDateTimeDoubleTimeSeriesBuilder toBuilder() {
    return builder(_zone).putAll(this);
  }

}
