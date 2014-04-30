/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.instant;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.threeten.bp.Instant;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.precise.PreciseDoubleTimeSeries;

/**
 * Standard immutable implementation of {@code InstantDoubleTimeSeries}.
 */
public final class ImmutableInstantDoubleTimeSeries
    extends AbstractInstantDoubleTimeSeries
    implements InstantDoubleTimeSeries, Serializable {

  /** Empty instance. */
  public static final ImmutableInstantDoubleTimeSeries EMPTY_SERIES = new ImmutableInstantDoubleTimeSeries(new long[0], new double[0]);

  /** Serialization version. */
  private static final long serialVersionUID = -43654613865187568L;

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
   * @return the time-series builder, not null
   */
  public static InstantDoubleTimeSeriesBuilder builder() {
    return new ImmutableInstantDoubleTimeSeriesBuilder();
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains a time-series from a single instant and value.
   * 
   * @param instant  the singleton instant, not null
   * @param value  the singleton value
   * @return the time-series, not null
   */
  public static ImmutableInstantDoubleTimeSeries of(Instant instant, double value) {
    Objects.requireNonNull(instant, "instant");
    long[] timesArray = new long[] {InstantToLongConverter.convertToLong(instant)};
    double[] valuesArray = new double[] {value};
    return new ImmutableInstantDoubleTimeSeries(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from matching arrays of instants and values.
   * 
   * @param instants  the date array, not null
   * @param values  the value array, not null
   * @return the time-series, not null
   */
  public static ImmutableInstantDoubleTimeSeries of(Instant[] instants, Double[] values) {
    long[] timesArray = convertToLongArray(instants);
    double[] valuesArray = convertToDoubleArray(values);
    validate(timesArray, valuesArray);
    return new ImmutableInstantDoubleTimeSeries(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from matching arrays of instants and values.
   * 
   * @param instants  the instant array, not null
   * @param values  the value array, not null
   * @return the time-series, not null
   */
  public static ImmutableInstantDoubleTimeSeries of(Instant[] instants, double[] values) {
    long[] timesArray = convertToLongArray(instants);
    validate(timesArray, values);
    double[] valuesArray = values.clone();
    return new ImmutableInstantDoubleTimeSeries(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from matching arrays of instants and values.
   * 
   * @param instants  the instant array, not null
   * @param values  the value array, not null
   * @return the time-series, not null
   */
  public static ImmutableInstantDoubleTimeSeries of(long[] instants, double[] values) {
    validate(instants, values);
    long[] timesArray = instants.clone();
    double[] valuesArray = values.clone();
    return new ImmutableInstantDoubleTimeSeries(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from matching arrays of instants and values.
   * 
   * @param instants  the instant list, not null
   * @param values  the value list, not null
   * @return the time-series, not null
   */
  public static ImmutableInstantDoubleTimeSeries of(Collection<Instant> instants, Collection<Double> values) {
    long[] timesArray = convertToLongArray(instants);
    double[] valuesArray = convertToDoubleArray(values);
    validate(timesArray, valuesArray);
    return new ImmutableInstantDoubleTimeSeries(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from another time-series.
   * 
   * @param timeSeries  the time-series, not null
   * @return the time-series, not null
   */
  public static ImmutableInstantDoubleTimeSeries of(PreciseDoubleTimeSeries<?> timeSeries) {
    if (timeSeries instanceof ImmutableInstantDoubleTimeSeries) {
      return (ImmutableInstantDoubleTimeSeries) timeSeries;
    }
    PreciseDoubleTimeSeries<?> other = (PreciseDoubleTimeSeries<?>) timeSeries;
    long[] timesArray = other.timesArrayFast();
    double[] valuesArray = other.valuesArrayFast();
    return new ImmutableInstantDoubleTimeSeries(timesArray, valuesArray);
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains a time-series from another time-series.
   * 
   * @param timeSeries  the time-series, not null
   * @return the time-series, not null
   */
  public static ImmutableInstantDoubleTimeSeries from(DoubleTimeSeries<Instant> timeSeries) {
    if (timeSeries instanceof PreciseDoubleTimeSeries) {
      return of((PreciseDoubleTimeSeries<?>) timeSeries);
    }
    long[] timesArray = convertToLongArray(timeSeries.timesArray());
    double[] valuesArray = timeSeries.valuesArrayFast();
    return new ImmutableInstantDoubleTimeSeries(timesArray, valuesArray);
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
        throw new IllegalArgumentException("Instants must be ordered");
      }
      maxTime = time;
    }
  }

  /**
   * Creates an instance.
   * 
   * @param nanos  the times, not null
   * @param values  the values, not null
   */
  ImmutableInstantDoubleTimeSeries(long[] nanos, double[] values) {
    _times = nanos;
    _values = values;
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
  InstantDoubleTimeSeries newInstanceFast(long[] instant, double[] values) {
    return new ImmutableInstantDoubleTimeSeries(instant, values);
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
  public InstantDoubleTimeSeries subSeriesFast(long startTime, boolean includeStart, long endTime, boolean includeEnd) {
    if (endTime < startTime) {
      throw new IllegalArgumentException("Invalid subSeries: endTime < startTime");
    }
    // special case for start equals end
    if (startTime == endTime) {
      if (includeStart && includeEnd) {
        int pos = Arrays.binarySearch(_times, startTime);
        if (pos >= 0) {
          return new ImmutableInstantDoubleTimeSeries(new long[] {startTime}, new double[] {_values[pos]});
        }
      }
      return EMPTY_SERIES;
    }
    // special case when this is empty
    if (isEmpty()) {
      return EMPTY_SERIES;
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
    return new ImmutableInstantDoubleTimeSeries(timesArray, valuesArray);
  }

  //-------------------------------------------------------------------------
  @Override
  public InstantDoubleTimeSeries head(int numItems) {
    if (numItems == size()) {
      return this;
    }
    long[] timesArray = Arrays.copyOfRange(_times, 0, numItems);
    double[] valuesArray = Arrays.copyOfRange(_values, 0, numItems);
    return new ImmutableInstantDoubleTimeSeries(timesArray, valuesArray);
  }

  @Override
  public InstantDoubleTimeSeries tail(int numItems) {
    int size = size();
    if (numItems == size) {
      return this;
    }
    long[] timesArray = Arrays.copyOfRange(_times, size - numItems, size);
    double[] valuesArray = Arrays.copyOfRange(_values, size - numItems, size);
    return new ImmutableInstantDoubleTimeSeries(timesArray, valuesArray);
  }

  //-------------------------------------------------------------------------
  @Override
  public ImmutableInstantDoubleTimeSeries newInstance(Instant[] instants, Double[] values) {
    return of(instants, values);
  }

  //-------------------------------------------------------------------------
  @Override
  public InstantDoubleTimeSeries operate(UnaryOperator operator) {
    double[] valuesArray = valuesArrayFast();
    for (int i = 0; i < valuesArray.length; i++) {
      valuesArray[i] = operator.operate(valuesArray[i]);
    }
    return new ImmutableInstantDoubleTimeSeries(_times, valuesArray);  // immutable, so can share times
  }

  @Override
  public InstantDoubleTimeSeries operate(double other, BinaryOperator operator) {
    double[] valuesArray = valuesArrayFast();
    for (int i = 0; i < valuesArray.length; i++) {
      valuesArray[i] = operator.operate(valuesArray[i], other);
    }
    return new ImmutableInstantDoubleTimeSeries(_times, valuesArray);  // immutable, so can share times
  }

  //-------------------------------------------------------------------------
  @Override
  public InstantDoubleTimeSeriesBuilder toBuilder() {
    return builder().putAll(this);
  }

}
