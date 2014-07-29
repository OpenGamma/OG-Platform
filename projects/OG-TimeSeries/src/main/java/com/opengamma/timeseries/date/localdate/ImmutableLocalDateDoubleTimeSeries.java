/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date.localdate;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;

/**
 * Standard immutable implementation of {@code LocalDateDoubleTimeSeries}.
 */
public final class ImmutableLocalDateDoubleTimeSeries
    extends AbstractLocalDateDoubleTimeSeries
    implements LocalDateDoubleTimeSeries, Serializable {

  /** Empty instance. */
  public static final ImmutableLocalDateDoubleTimeSeries EMPTY_SERIES = new ImmutableLocalDateDoubleTimeSeries(new int[0], new double[0]);

  /** Serialization version. */
  private static final long serialVersionUID = -43654613865187568L;

  /**
   * The times in the series.
   */
  private final int[] _times;
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
  public static LocalDateDoubleTimeSeriesBuilder builder() {
    return new ImmutableLocalDateDoubleTimeSeriesBuilder();
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains a time-series from a single date and value.
   * 
   * @param date  the singleton date, not null
   * @param value  the singleton value
   * @return the time-series, not null
   */
  public static ImmutableLocalDateDoubleTimeSeries of(LocalDate date, double value) {
    Objects.requireNonNull(date, "date");
    int[] timesArray = new int[] {LocalDateToIntConverter.convertToInt(date)};
    double[] valuesArray = new double[] {value};
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from matching arrays of dates and values.
   * 
   * @param dates  the date array, not null
   * @param values  the value array, not null
   * @return the time-series, not null
   */
  public static ImmutableLocalDateDoubleTimeSeries of(LocalDate[] dates, Double[] values) {
    int[] timesArray = convertToIntArray(dates);
    double[] valuesArray = convertToDoubleArray(values);
    validate(timesArray, valuesArray);
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from matching arrays of dates and values.
   * 
   * @param dates  the date array, not null
   * @param values  the value array, not null
   * @return the time-series, not null
   */
  public static ImmutableLocalDateDoubleTimeSeries of(LocalDate[] dates, double[] values) {
    int[] timesArray = convertToIntArray(dates);
    validate(timesArray, values);
    double[] valuesArray = values.clone();
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from matching arrays of dates and values.
   * 
   * @param dates  the date array, not null
   * @param values  the value array, not null
   * @return the time-series, not null
   */
  public static ImmutableLocalDateDoubleTimeSeries of(int[] dates, double[] values) {
    validate(dates, values);
    int[] timesArray = dates.clone();
    double[] valuesArray = values.clone();
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from matching arrays of dates and values.
   * 
   * @param dates  the date list, not null
   * @param values  the value list, not null
   * @return the time-series, not null
   */
  public static ImmutableLocalDateDoubleTimeSeries of(Collection<LocalDate> dates, Collection<Double> values) {
    int[] timesArray = convertToIntArray(dates);
    double[] valuesArray = convertToDoubleArray(values);
    validate(timesArray, valuesArray);
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  /**
   * Obtains a time-series from another time-series.
   * 
   * @param timeSeries  the time-series, not null
   * @return the time-series, not null
   */
  public static ImmutableLocalDateDoubleTimeSeries of(DateDoubleTimeSeries<?> timeSeries) {
    if (timeSeries instanceof ImmutableLocalDateDoubleTimeSeries) {
      return (ImmutableLocalDateDoubleTimeSeries) timeSeries;
    }
    DateDoubleTimeSeries<?> other = (DateDoubleTimeSeries<?>) timeSeries;
    int[] timesArray = other.timesArrayFast();
    double[] valuesArray = other.valuesArrayFast();
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains a time-series from another time-series.
   * 
   * @param timeSeries  the time-series, not null
   * @return the time-series, not null
   */
  public static ImmutableLocalDateDoubleTimeSeries from(DoubleTimeSeries<LocalDate> timeSeries) {
    if (timeSeries instanceof DateDoubleTimeSeries) {
      return of((DateDoubleTimeSeries<?>) timeSeries);
    }
    int[] timesArray = convertToIntArray(timeSeries.timesArray());
    double[] valuesArray = timeSeries.valuesArrayFast();
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  //-------------------------------------------------------------------------
  /**
   * Validates the data before creation.
   * 
   * @param times  the times, not null
   * @param values  the values, not null
   */
  private static void validate(int[] times, double[] values) {
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
      LocalDateToIntConverter.checkValid(time);
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
  ImmutableLocalDateDoubleTimeSeries(int[] times, double[] values) {
    _times = times;
    _values = values;
  }

  //-------------------------------------------------------------------------
  @Override
  int[] timesArrayFast0() {
    return _times;
  }

  @Override
  double[] valuesArrayFast0() {
    return _values;
  }

  @Override
  LocalDateDoubleTimeSeries newInstanceFast(int[] times, double[] values) {
    return new ImmutableLocalDateDoubleTimeSeries(times, values);
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
  public Double getValue(int date) {
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
  public double getValueAtIndexFast(int index) {
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
  public double getEarliestValueFast() {
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
  public double getLatestValueFast() {
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
  public double[] valuesArrayFast() {
    return _values.clone();
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateDoubleTimeSeries subSeriesFast(int startTime, boolean includeStart, int endTime, boolean includeEnd) {
    if (endTime < startTime) {
      throw new IllegalArgumentException("Invalid subSeries: endTime < startTime");
    }
    // special case for start equals end
    if (startTime == endTime) {
      if (includeStart && includeEnd) {
        int pos = Arrays.binarySearch(_times, startTime);
        if (pos >= 0) {
          return new ImmutableLocalDateDoubleTimeSeries(new int[] {startTime}, new double[] {_values[pos]});
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
    double[] valuesArray = Arrays.copyOfRange(_values, startPos, endPos);
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateDoubleTimeSeries head(int numItems) {
    if (numItems == size()) {
      return this;
    }
    int[] timesArray = Arrays.copyOfRange(_times, 0, numItems);
    double[] valuesArray = Arrays.copyOfRange(_values, 0, numItems);
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  @Override
  public LocalDateDoubleTimeSeries tail(int numItems) {
    int size = size();
    if (numItems == size) {
      return this;
    }
    int[] timesArray = Arrays.copyOfRange(_times, size - numItems, size);
    double[] valuesArray = Arrays.copyOfRange(_values, size - numItems, size);
    return new ImmutableLocalDateDoubleTimeSeries(timesArray, valuesArray);
  }

  //-------------------------------------------------------------------------
  @Override
  public ImmutableLocalDateDoubleTimeSeries newInstance(LocalDate[] dates, Double[] values) {
    return of(dates, values);
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateDoubleTimeSeries operate(UnaryOperator operator) {
    double[] valuesArray = valuesArrayFast();
    for (int i = 0; i < valuesArray.length; i++) {
      valuesArray[i] = operator.operate(valuesArray[i]);
    }
    return new ImmutableLocalDateDoubleTimeSeries(_times, valuesArray);  // immutable, so can share times
  }

  @Override
  public LocalDateDoubleTimeSeries operate(double other, BinaryOperator operator) {
    double[] valuesArray = valuesArrayFast();
    for (int i = 0; i < valuesArray.length; i++) {
      valuesArray[i] = operator.operate(valuesArray[i], other);
    }
    return new ImmutableLocalDateDoubleTimeSeries(_times, valuesArray);  // immutable, so can share times
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDateDoubleTimeSeriesBuilder toBuilder() {
    return builder().putAll(this);
  }

}
