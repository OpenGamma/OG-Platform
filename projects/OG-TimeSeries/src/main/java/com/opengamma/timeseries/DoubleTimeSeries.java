/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import java.util.NoSuchElementException;

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

  @Override  // override for covariant return type
  DoubleTimeSeries<T> subSeries(T startTime, boolean includeStart, T endTime, boolean includeEnd);

  @Override  // override for covariant return type
  DoubleTimeSeries<T> subSeries(T startTime, T endTime);

  @Override  // override for covariant return type
  DoubleTimeSeries<T> head(int numItems);

  @Override  // override for covariant return type
  DoubleTimeSeries<T> tail(int numItems);

  @Override  // override for covariant return type
  DoubleTimeSeries<T> lag(int lagCount);

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
//  double getValueDoubleAtIndex(int index);
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
   * this time-series and another time-series, with the values from the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DoubleTimeSeries<T> intersectionSecondValue(DoubleTimeSeries<?> other);

  /**
   * Creates a new time-series combining both series where there are no
   * overlapping date-times.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   * @throws RuntimeException if there are overlapping date-times
   */
  DoubleTimeSeries<T> noIntersectionOperation(DoubleTimeSeries<?> other);

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
   * @throws NoSuchElementException if the series is empty
   */
  double maxValue() throws NoSuchElementException;

  /**
   * Calculates the maximum value across the whole time-series.
   * 
   * @return the maximum value
   * @throws NoSuchElementException if the series is empty
   */
  double minValue() throws NoSuchElementException;

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
  @Override  // override for covariant return type
  DoubleTimeSeries<T> newInstance(T[] dateTimes, Double[] values);

}
