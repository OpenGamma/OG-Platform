/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import java.util.Date;
import java.util.TimeZone;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.util.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.util.timeseries.date.MutableDateDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.DateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.date.time.MutableDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.integer.FastMutableIntDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastMutableLongDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MutableLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.MutableSQLDateDoubleTimeSeries;
import com.opengamma.util.timeseries.sqldate.SQLDateDoubleTimeSeries;
import com.opengamma.util.timeseries.yearoffset.MutableYearOffsetDoubleTimeSeries;
import com.opengamma.util.timeseries.yearoffset.YearOffsetDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.MutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ZonedDateTimeDoubleTimeSeries;

/**
 * A time-series, which represents the changes in a {@code double} value over time.
 * <p>
 * This interface is similar to both a {@code SortedMap} of value keyed by date-time
 * and a {@code List} of date-time to {@code double} value pairs.
 * As such, the date/times do not have to be evenly spread over time within the series.
 * 
 * @param <T> the date-time type, such as {@code Instant} or {@code LocalDate}
 */
public interface DoubleTimeSeries<T> extends TimeSeries<T, Double> {

  @Override
  DoubleTimeSeries<T> subSeries(T startTime, boolean includeStart, T endTime, boolean includeEnd);

  @Override
  DoubleTimeSeries<T> subSeries(T startTime, T endTime);

  @Override
  DoubleTimeSeries<T> head(int numItems);

  @Override
  DoubleTimeSeries<T> tail(int numItems);

  @Override
  DoubleTimeSeries<T> lag(final int lagCount);

//  //-------------------------------------------------------------------------
//  /**
//   * Gets the {@code double} value at the date-time specified.
//   * <p>
//   * This method provides {@code Map} style lookup of values.
//   * The date/time is matched exactly, thus care must be taken with precision in times.
//   * If there is no entry at the date-time, then null is returned.
//   * 
//   * @param dateTime  the date-time to retrieve, not null
//   * @return the value at the date-time, null if date-time not present or
//   *  if the implementation permits nulls
//   */
//  double getValueDouble(T dateTime);
//
//  /**
//   * Gets the {@code double} value at the index specified.
//   * <p>
//   * This method provides {@code List} style lookup of values.
//   * It is not guaranteed that the lookup is O(1), thus it should be avoided in loops.
//   * 
//   * @param index  the zero-based index to retrieve
//   * @return the value at the index, null if the implementation permits nulls
//   * @throws IndexOutOfBoundsException if the index is invalid
//   */
//  double getValueDoubleAt(int index);
//
//  /**
//   * Gets the {@code double} value at the latest date-time in the series.
//   * 
//   * @return the value at the latest date-time
//   * @throws NoSuchElementException if empty
//   */
//  double getLatestValueDouble();
//
//  /**
//   * Gets the {@code double} value at the earliest date-time in the series.
//   * 
//   * @return the value at the earliest date-time
//   * @throws NoSuchElementException if empty
//   */
//  double getEarliestValueDouble();

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with each value in this time-series
   * increased by the specified amount.
   * <p>
   * The result will have the same set of date-times, but each value will be
   * increased by the specified amount using simple {@code double} addition.
   * 
   * @param amountToAdd  the amount to add to each value
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> add(double amountToAdd);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values added.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * sum of the two values by simple {@code double} addition.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> add(DoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values added.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * sum of the two values by simple {@code double} addition.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> add(FastBackedDoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values added.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * sum of the two values by simple {@code double} addition.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> add(FastIntDoubleTimeSeries other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values added.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * sum of the two values by simple {@code double} addition.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> add(FastLongDoubleTimeSeries other);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the values added.
   * <p>
   * For the union of date-times, the result will be the sum of the two
   * values by simple {@code double} addition, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionAdd(DoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the values added.
   * <p>
   * For the union of date-times, the result will be the sum of the two
   * values by simple {@code double} addition, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionAdd(FastBackedDoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the values added.
   * <p>
   * For the union of date-times, the result will be the sum of the two
   * values by simple {@code double} addition, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionAdd(FastIntDoubleTimeSeries other);

  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the values added.
   * <p>
   * For the union of date-times, the result will be the sum of the two
   * values by simple {@code double} addition, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionAdd(FastLongDoubleTimeSeries other);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with each value in this time-series
   * decreased by the specified amount.
   * <p>
   * The result will have the same set of date-times, but each value will be
   * decreased by the specified amount using simple {@code double} subtraction.
   * 
   * @param amountToSubtract  the amount to subtract from each value
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> subtract(double amountToSubtract);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values subtracted.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * value of this series minus the value of the other series by simple
   * {@code double} subtraction.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> subtract(DoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values subtracted.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * value of this series minus the value of the other series by simple
   * {@code double} subtraction.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> subtract(FastBackedDoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values subtracted.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * value of this series minus the value of the other series by simple
   * {@code double} subtraction.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> subtract(FastLongDoubleTimeSeries other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values subtracted.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * value of this series minus the value of the other series by simple
   * {@code double} subtraction.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> subtract(FastIntDoubleTimeSeries other);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the values subtracted.
   * <p>
   * For the union of date-times, the result will be the value of this
   * series minus the value of the other series by simple {@code double}
   * subtraction, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionSubtract(DoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the values subtracted.
   * <p>
   * For the union of date-times, the result will be the value of this
   * series minus the value of the other series by simple {@code double}
   * subtraction, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionSubtract(FastBackedDoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the values subtracted.
   * <p>
   * For the union of date-times, the result will be the value of this
   * series minus the value of the other series by simple {@code double}
   * subtraction, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionSubtract(FastIntDoubleTimeSeries other);

  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the values subtracted.
   * <p>
   * For the union of date-times, the result will be the value of this
   * series minus the value of the other series by simple {@code double}
   * subtraction, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionSubtract(FastLongDoubleTimeSeries other);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with each value in this time-series
   * multiplied by the specified amount.
   * <p>
   * The result will have the same set of date-times, but each value will be
   * multiplied by the specified amount using simple {@code double} multiplication.
   * 
   * @param amountToMultiplyBy  the amount to multiply each value by
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> multiply(double amountToMultiplyBy);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values multiplied.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * value of this series multiplied by the value of the other series by simple
   * {@code double} multiplication.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> multiply(DoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values multiplied.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * value of this series multiplied by the value of the other series by simple
   * {@code double} multiplication.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> multiply(FastBackedDoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values multiplied.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * value of this series multiplied by the value of the other series by simple
   * {@code double} multiplication.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> multiply(FastIntDoubleTimeSeries other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values multiplied.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * value of this series multiplied by the value of the other series by simple
   * {@code double} multiplication.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> multiply(FastLongDoubleTimeSeries other);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the values multiplied.
   * <p>
   * For the union of date-times, the result will be the value of this
   * series multiplied by the value of the other series by simple {@code double}
   * multiplication, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionMultiply(DoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the values multiplied.
   * <p>
   * For the union of date-times, the result will be the value of this
   * series multiplied by the value of the other series by simple {@code double}
   * multiplication, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionMultiply(FastBackedDoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the values multiplied.
   * <p>
   * For the union of date-times, the result will be the value of this
   * series multiplied by the value of the other series by simple {@code double}
   * multiplication, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionMultiply(FastIntDoubleTimeSeries other);

  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the values multiplied.
   * <p>
   * For the union of date-times, the result will be the value of this
   * series multiplied by the value of the other series by simple {@code double}
   * multiplication, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionMultiply(FastLongDoubleTimeSeries other);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with each value in this time-series
   * divided by the specified amount.
   * <p>
   * The result will have the same set of date-times, but each value will be
   * divided by the specified amount using simple {@code double} division.
   * 
   * @param amountToDivideBy  the amount to divide each value by
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> divide(double amountToDivideBy);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values divided.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * value of this series divided by the value of the other series by simple
   * {@code double} division.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> divide(DoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values divided.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * value of this series divided by the value of the other series by simple
   * {@code double} division.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> divide(FastBackedDoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values divided.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * value of this series divided by the value of the other series by simple
   * {@code double} division.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> divide(FastIntDoubleTimeSeries other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values divided.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * value of this series divided by the value of the other series by simple
   * {@code double} division.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> divide(FastLongDoubleTimeSeries other);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the values divided.
   * <p>
   * For the union of date-times, the result will be the value of this
   * series divided by the value of the other series by simple {@code double}
   * division, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionDivide(DoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the values divided.
   * <p>
   * For the union of date-times, the result will be the value of this
   * series divided by the value of the other series by simple {@code double}
   * division, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionDivide(FastBackedDoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the values divided.
   * <p>
   * For the union of date-times, the result will be the value of this
   * series divided by the value of the other series by simple {@code double}
   * division, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionDivide(FastIntDoubleTimeSeries other);

  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the values divided.
   * <p>
   * For the union of date-times, the result will be the value of this
   * series divided by the value of the other series by simple {@code double}
   * division, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionDivide(FastLongDoubleTimeSeries other);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with each value in this time-series
   * set to the specified power.
   * <p>
   * The result will have the same set of date-times, but each value will be
   * to the power of the specified amount.
   * 
   * @param power  the power to apply to each value
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> power(double power);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values powered.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * value of this series to the power of the value of the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> power(DoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values powered.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * value of this series to the power of the value of the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> power(FastBackedDoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values powered.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * value of this series to the power of the value of the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> power(FastIntDoubleTimeSeries other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values powered.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * value of this series to the power of the value of the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> power(FastLongDoubleTimeSeries other);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the values powered.
   * <p>
   * For the union of date-times, the result will be the value of this
   * series to the power of the value of the other series, with zero as the
   * default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionPower(DoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the values powered.
   * <p>
   * For the union of date-times, the result will be the value of this
   * series to the power of the value of the other series, with zero as the
   * default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionPower(FastBackedDoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the values powered.
   * <p>
   * For the union of date-times, the result will be the value of this
   * series to the power of the value of the other series, with zero as the
   * default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionPower(FastIntDoubleTimeSeries other);

  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the values powered.
   * <p>
   * For the union of date-times, the result will be the value of this
   * series to the power of the value of the other series, with zero as the
   * default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionPower(FastLongDoubleTimeSeries other);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with each value in this time-series
   * set to the minimum of its current value and the specified value.
   * <p>
   * The result will have the same set of date-times, but each value will be
   * be no less than the specified minimum value.
   * 
   * @param minValue  the minimum value to apply to each value
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> minimum(double minValue);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the minimum value selected.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * minimum of the value of this series and the value of the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> minimum(DoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the minimum value selected.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * minimum of the value of this series and the value of the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> minimum(FastBackedDoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the minimum value selected.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * minimum of the value of this series and the value of the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> minimum(FastIntDoubleTimeSeries other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the minimum value selected.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * minimum of the value of this series and the value of the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> minimum(FastLongDoubleTimeSeries other);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the minimum value selected.
   * <p>
   * For the union of date-times, the result will be the minimum of the value
   * of this series and the value of the other series, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionMinimum(DoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the minimum value selected.
   * <p>
   * For the union of date-times, the result will be the minimum of the value
   * of this series and the value of the other series, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionMinimum(FastBackedDoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the minimum value selected.
   * <p>
   * For the union of date-times, the result will be the minimum of the value
   * of this series and the value of the other series, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionMinimum(FastIntDoubleTimeSeries other);

  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the minimum value selected.
   * <p>
   * For the union of date-times, the result will be the minimum of the value
   * of this series and the value of the other series, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionMinimum(FastLongDoubleTimeSeries other);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with each value in this time-series
   * set to the maximum of its current value and the specified value.
   * <p>
   * The result will have the same set of date-times, but each value will be
   * be no more than the specified maximum value.
   * 
   * @param maxValue  the maximum value to apply to each value
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> maximum(double maxValue);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the maximum value selected.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * maximum of the value of this series and the value of the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> maximum(DoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the maximum value selected.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * maximum of the value of this series and the value of the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> maximum(FastBackedDoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the maximum value selected.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * maximum of the value of this series and the value of the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> maximum(FastIntDoubleTimeSeries other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the maximum value selected.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * maximum of the value of this series and the value of the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> maximum(FastLongDoubleTimeSeries other);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the maximum value selected.
   * <p>
   * For the union of date-times, the result will be the maximum of the value
   * of this series and the value of the other series, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionMaximum(DoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the maximum value selected.
   * <p>
   * For the union of date-times, the result will be the maximum of the value
   * of this series and the value of the other series, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionMaximum(FastBackedDoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the maximum value selected.
   * <p>
   * For the union of date-times, the result will be the maximum of the value
   * of this series and the value of the other series, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionMaximum(FastIntDoubleTimeSeries other);

  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the maximum value selected.
   * <p>
   * For the union of date-times, the result will be the maximum of the value
   * of this series and the value of the other series, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionMaximum(FastLongDoubleTimeSeries other);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with each value in this time-series
   * set to the average of its current value and the specified value.
   * <p>
   * The result will have the same set of date-times, but each value will be
   * be the average of it and the specified value.
   * 
   * @param value  the value to calculate the average against
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> average(double value);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the average value.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * average of the value of this series and the value of the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> average(DoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the average value.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * average of the value of this series and the value of the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> average(FastBackedDoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the average value.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * average of the value of this series and the value of the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> average(FastIntDoubleTimeSeries other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the average value.
   * <p>
   * For each date-time that the series have in common, the result will be the
   * average of the value of this series and the value of the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> average(FastLongDoubleTimeSeries other);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the average value.
   * <p>
   * For the union of date-times, the result will be the average of the value
   * of this series and the value of the other series, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionAverage(DoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the average value.
   * <p>
   * For the union of date-times, the result will be the average of the value
   * of this series and the value of the other series, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionAverage(FastBackedDoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the average value.
   * <p>
   * For the union of date-times, the result will be the average of the value
   * of this series and the value of the other series, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionAverage(FastIntDoubleTimeSeries other);

  /**
   * Creates a new time-series with the union of the date-times from
   * this time-series and another time-series, with the average value.
   * <p>
   * For the union of date-times, the result will be the average of the value
   * of this series and the value of the other series, with zero as the default value.
   * 
   * @param other  the other series to union with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> unionAverage(FastLongDoubleTimeSeries other);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values from this series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> intersectionFirstValue(DoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values from this series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> intersectionFirstValue(FastBackedDoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values from this series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> intersectionFirstValue(FastIntDoubleTimeSeries other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values from this series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> intersectionFirstValue(FastLongDoubleTimeSeries other);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values from the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> intersectionSecondValue(DoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values from the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> intersectionSecondValue(FastBackedDoubleTimeSeries<?> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values from the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> intersectionSecondValue(FastIntDoubleTimeSeries other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values from the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> intersectionSecondValue(FastLongDoubleTimeSeries other);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series combining both series where there are no
   * overlapping date-times.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   * @throws RuntimeException if there are overlapping date-times
   */
  DoubleTimeSeries<T> noIntersectionOperation(DoubleTimeSeries<?> other);

  /**
   * Creates a new time-series combining both series where there are no
   * overlapping date-times.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   * @throws RuntimeException if there are overlapping date-times
   */
  DoubleTimeSeries<T> noIntersectionOperation(FastBackedDoubleTimeSeries<?> other);

  /**
   * Creates a new time-series combining both series where there are no
   * overlapping date-times.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   * @throws RuntimeException if there are overlapping date-times
   */
  DoubleTimeSeries<T> noIntersectionOperation(FastIntDoubleTimeSeries other);

  /**
   * Creates a new time-series combining both series where there are no
   * overlapping date-times.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   * @throws RuntimeException if there are overlapping date-times
   */
  DoubleTimeSeries<T> noIntersectionOperation(FastLongDoubleTimeSeries other);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with each value negated.
   * 
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> negate();

  /**
   * Creates a new time-series with each value set to the reciprocal.
   * 
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> reciprocal();

  /**
   * Creates a new time-series with each value set to the log.
   * 
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> log();

  /**
   * Creates a new time-series with each value set to the log base-10.
   * 
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> log10();

  /**
   * Creates a new time-series with each value set to the absolute positive value.
   * 
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> abs();

  //-------------------------------------------------------------------------
  /**
   * Calculates the minimum value across the whole time-series.
   * 
   * @return the minimum value
   */
  double maxValue();

  /**
   * Calculates the maximum value across the whole time-series.
   * 
   * @return the maximum value
   */
  double minValue();

  //-------------------------------------------------------------------------
  /**
   * Gets an array of all {@code double} values in this series.
   * <p>
   * The index of each entry will match that used by the index lookup methods.
   * As such, the values will be in date-time order.
   * 
   * @return an array of all the values in order from earliest to latest, not null
   */
  double[] valuesArrayFast();

  //-------------------------------------------------------------------------
  /**
   * Creates a new instance with a new set of date-times and values.
   * 
   * @param dateTimes  the date-times, not null
   * @param values  the values, not null
   * @return the new time-series, not null
   * @throws RuntimeException if the array sizes differ or the instance cannot be created
   */
  DoubleTimeSeries<T> newInstance(T[] dateTimes, Double[] values);

  //-------------------------------------------------------------------------
  /**
   * Converts this time-series to a {@code FastIntDoubleTimeSeries}.
   * 
   * @return the time-series, not null
   */
  FastIntDoubleTimeSeries toFastIntDoubleTimeSeries();

  /**
   * Converts this time-series to a {@code FastIntDoubleTimeSeries} using
   * a specific date-time encoding.
   * 
   * @param encoding  the date-time encoding, not null
   * @return the time-series, not null
   */
  FastIntDoubleTimeSeries toFastIntDoubleTimeSeries(DateTimeNumericEncoding encoding);

  //-------------------------------------------------------------------------
  /**
   * Converts this time-series to a {@code FastMutableIntDoubleTimeSeries}.
   * 
   * @return the time-series, not null
   */
  FastMutableIntDoubleTimeSeries toFastMutableIntDoubleTimeSeries();

  /**
   * Converts this time-series to a {@code FastMutableIntDoubleTimeSeries} using
   * a specific date-time encoding.
   * 
   * @param encoding  the date-time encoding, not null
   * @return the time-series, not null
   */
  FastMutableIntDoubleTimeSeries toFastMutableIntDoubleTimeSeries(DateTimeNumericEncoding encoding);

  //-------------------------------------------------------------------------
  /**
   * Converts this time-series to a {@code FastLongDoubleTimeSeries}.
   * 
   * @return the time-series, not null
   */
  FastLongDoubleTimeSeries toFastLongDoubleTimeSeries();

  /**
   * Converts this time-series to a {@code FastLongDoubleTimeSeries} using
   * a specific date-time encoding.
   * 
   * @param encoding  the date-time encoding, not null
   * @return the time-series, not null
   */
  FastLongDoubleTimeSeries toFastLongDoubleTimeSeries(DateTimeNumericEncoding encoding);

  //-------------------------------------------------------------------------
  /**
   * Converts this time-series to a {@code FastMutableLongDoubleTimeSeries}.
   * 
   * @return the time-series, not null
   */
  FastMutableLongDoubleTimeSeries toFastMutableLongDoubleTimeSeries();

  /**
   * Converts this time-series to a {@code FastMutableLongDoubleTimeSeries} using
   * a specific date-time encoding.
   * 
   * @param encoding  the date-time encoding, not null
   * @return the time-series, not null
   */
  FastMutableLongDoubleTimeSeries toFastMutableLongDoubleTimeSeries(DateTimeNumericEncoding encoding);

  //-------------------------------------------------------------------------
  /**
   * Converts this time-series to a {@code DateDoubleTimeSeries}.
   * 
   * @return the time-series, not null
   */
  DateDoubleTimeSeries toDateDoubleTimeSeries();

  /**
   * Converts this time-series to a {@code DateDoubleTimeSeries}.
   * 
   * @param zone  the time-zone to use
   * @return the time-series, not null
   */
  DateDoubleTimeSeries toDateDoubleTimeSeries(TimeZone zone);

  //-------------------------------------------------------------------------
  /**
   * Converts this time-series to a {@code MutableDateDoubleTimeSeries}.
   * 
   * @return the time-series, not null
   */
  MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries();

  /**
   * Converts this time-series to a {@code MutableDateDoubleTimeSeries}.
   * 
   * @param zone  the time-zone to use
   * @return the time-series, not null
   */
  MutableDateDoubleTimeSeries toMutableDateDoubleTimeSeries(TimeZone zone);

  //-------------------------------------------------------------------------
  /**
   * Converts this time-series to a {@code MutableSQLDateDoubleTimeSeries}.
   * 
   * @return the time-series, not null
   */
  MutableSQLDateDoubleTimeSeries toMutableSQLDateDoubleTimeSeries();

  /**
   * Converts this time-series to a {@code MutableSQLDateDoubleTimeSeries}.
   * 
   * @param zone  the time-zone to use
   * @return the time-series, not null
   */
  MutableSQLDateDoubleTimeSeries toMutableSQLDateDoubleTimeSeries(TimeZone zone);

  //-------------------------------------------------------------------------
  /**
   * Converts this time-series to a {@code SQLDateDoubleTimeSeries}.
   * 
   * @return the time-series, not null
   */
  SQLDateDoubleTimeSeries toSQLDateDoubleTimeSeries();

  /**
   * Converts this time-series to a {@code SQLDateDoubleTimeSeries}.
   * 
   * @param zone  the time-zone to use
   * @return the time-series, not null
   */
  SQLDateDoubleTimeSeries toSQLDateDoubleTimeSeries(TimeZone zone);

  //-------------------------------------------------------------------------
  /**
   * Converts this time-series to a {@code MutableDateTimeDoubleTimeSeries}.
   * 
   * @return the time-series, not null
   */
  MutableDateTimeDoubleTimeSeries toMutableDateTimeDoubleTimeSeries();

  /**
   * Converts this time-series to a {@code MutableDateTimeDoubleTimeSeries}.
   * 
   * @param zone  the time-zone to use
   * @return the time-series, not null
   */
  MutableDateTimeDoubleTimeSeries toMutableDateTimeDoubleTimeSeries(TimeZone zone);

  //-------------------------------------------------------------------------
  /**
   * Converts this time-series to a {@code DateTimeDoubleTimeSeries}.
   * 
   * @return the time-series, not null
   */
  DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries();

  /**
   * Converts this time-series to a {@code DateTimeDoubleTimeSeries}.
   * 
   * @param zone  the time-zone to use
   * @return the time-series, not null
   */
  DateTimeDoubleTimeSeries toDateTimeDoubleTimeSeries(TimeZone zone);

  //-------------------------------------------------------------------------
  /**
   * Converts this time-series to a {@code ZonedDateTimeDoubleTimeSeries}.
   * 
   * @return the time-series, not null
   */
  ZonedDateTimeDoubleTimeSeries toZonedDateTimeDoubleTimeSeries();

  /**
   * Converts this time-series to a {@code ZonedDateTimeDoubleTimeSeries}.
   * 
   * @param zone  the time-zone to use
   * @return the time-series, not null
   */
  ZonedDateTimeDoubleTimeSeries toZonedDateTimeDoubleTimeSeries(javax.time.calendar.TimeZone zone);

  //-------------------------------------------------------------------------
  /**
   * Converts this time-series to a {@code MutableZonedDateTimeDoubleTimeSeries}.
   * 
   * @return the time-series, not null
   */
  MutableZonedDateTimeDoubleTimeSeries toMutableZonedDateTimeDoubleTimeSeries();

  /**
   * Converts this time-series to a {@code MutableZonedDateTimeDoubleTimeSeries}.
   * 
   * @param zone  the time-zone to use
   * @return the time-series, not null
   */
  MutableZonedDateTimeDoubleTimeSeries toMutableZonedDateTimeDoubleTimeSeries(javax.time.calendar.TimeZone zone);

  //-------------------------------------------------------------------------
  /**
   * Converts this time-series to a {@code LocalDateDoubleTimeSeries}.
   * 
   * @return the time-series, not null
   */
  LocalDateDoubleTimeSeries toLocalDateDoubleTimeSeries();

  /**
   * Converts this time-series to a {@code LocalDateDoubleTimeSeries}.
   * 
   * @param zone  the time-zone to use
   * @return the time-series, not null
   */
  LocalDateDoubleTimeSeries toLocalDateDoubleTimeSeries(javax.time.calendar.TimeZone zone);

  //-------------------------------------------------------------------------
  /**
   * Converts this time-series to a {@code MutableLocalDateDoubleTimeSeries}.
   * 
   * @return the time-series, not null
   */
  MutableLocalDateDoubleTimeSeries toMutableLocalDateDoubleTimeSeries();

  /**
   * Converts this time-series to a {@code MutableLocalDateDoubleTimeSeries}.
   * 
   * @param zone  the time-zone to use
   * @return the time-series, not null
   */
  MutableLocalDateDoubleTimeSeries toMutableLocalDateDoubleTimeSeries(javax.time.calendar.TimeZone zone);

  //-------------------------------------------------------------------------
  /**
   * Converts this time-series to a {@code YearOffsetDoubleTimeSeries}.
   * 
   * @param zeroDate  the zero date-time
   * @return the time-series, not null
   */
  YearOffsetDoubleTimeSeries toYearOffsetDoubleTimeSeries(ZonedDateTime zeroDate);

  /**
   * Converts this time-series to a {@code YearOffsetDoubleTimeSeries}.
   * 
   * @param zone  the time-zone to use
   * @param zeroDate  the zero date-time
   * @return the time-series, not null
   */
  YearOffsetDoubleTimeSeries toYearOffsetDoubleTimeSeries(TimeZone zone, Date zeroDate);

  //-------------------------------------------------------------------------
  /**
   * Converts this time-series to a {@code MutableYearOffsetDoubleTimeSeries}.
   * 
   * @param zeroDate  the zero date-time
   * @return the time-series, not null
   */
  MutableYearOffsetDoubleTimeSeries toMutableYearOffsetDoubleTimeSeries(ZonedDateTime zeroDate);

  /**
   * Converts this time-series to a {@code MutableYearOffsetDoubleTimeSeries}.
   * 
   * @param zone  the time-zone to use
   * @param zeroDate  the zero date-time
   * @return the time-series, not null
   */
  MutableYearOffsetDoubleTimeSeries toMutableYearOffsetDoubleTimeSeries(TimeZone zone, Date zeroDate);

}
