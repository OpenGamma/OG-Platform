/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date;

import java.util.NoSuchElementException;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.localdate.LocalDateToIntConverter;

/**
 * A time series that stores {@code double} data values against dates.
 * <p>
 * The "time" key to the time-series is a date.
 * The date class is defined by the implementation, allowing flexibility of date library.
 * <p>
 * This interface also allows the "time" to be viewed and manipulate as an {@code int}.
 * An implementation may store either the date object or an {@code int}.
 * The mapping between the two is available using {@link LocalDateToIntConverter}.
 * <p>
 * The {@code int} must use this encoding:
 * Any far future or maximum date must be converted to {@code Integer.MAX_VALUE}.
 * Any far past or minimum date must be converted to {@code Integer.MIN_VALUE}.
 * Other values are encoded by multiplying the year by 10,000 and the month by 100.
 * Thus the date 2012-06-30 will be converted to the number 20,120,630.
 * Any date with a year outside the range 0000 to 9999 throws an exception.
 * 
 * @param <T>  the date type
 */
public interface DateDoubleTimeSeries<T>
    extends DoubleTimeSeries<T> {

  /**
   * Checks if the series contains a value at the {@code int} date specified.
   * <p>
   * This method provides {@code Map} style {@code containsKey()} behavior.
   * 
   * @param date  the date to retrieve, not null
   * @return true if the series contains the specified date, false if not
   */
  boolean containsTime(int date);

  /**
   * Gets the value associated with the date, specifying the primitive {@code int} date.
   * 
   * @param date  the {@code int} date
   * @return the matching value, null if there is no value for the date
   */
  Double getValue(int date);

  //-------------------------------------------------------------------------
  /**
   * Gets the value at the specified index.
   * 
   * @param index  the index to retrieve
   * @return the date at the index
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  int getTimeAtIndexFast(int index);

  /**
   * Gets the value at the specified index.
   * 
   * @param index  the index to retrieve
   * @return the value at the index
   * @throws IndexOutOfBoundsException if the index is invalid
   */
  double getValueAtIndexFast(int index);

  //-------------------------------------------------------------------------
  /**
   * Gets the earliest date for which there is a data point.
   * 
   * @return the earliest date
   * @throws NoSuchElementException if empty
   */
  int getEarliestTimeFast();

  /**
   * Gets the value at the earliest date in the series.
   * 
   * @return the value at the earliest date
   * @throws NoSuchElementException if empty
   */
  double getEarliestValueFast();

  /**
   * Gets the latest date for which there is a data point.
   * 
   * @return the latest date
   * @throws NoSuchElementException if empty
   */
  int getLatestTimeFast();

  /**
   * Gets the value at the latest date in the series.
   * 
   * @return the value at the latest date
   * @throws NoSuchElementException if empty
   */
  double getLatestValueFast();

  //-------------------------------------------------------------------------
  /**
   * Gets an iterator over the date-value pairs.
   * <p>
   * Although the pairs are expressed as instances of {@code Map.Entry},
   * it is recommended to use the primitive methods on {@code DateDoubleEntryIterator}.
   * 
   * @return the iterator, not null
   */
  DateDoubleEntryIterator<T> iterator();

  //-------------------------------------------------------------------------
  /**
   * Gets an array of all {@code int} times in this series.
   * <p>
   * The index of each entry will match that used by the index lookup methods.
   * As such, the values will be in date order.
   * 
   * @return an array of all the values in order from earliest to latest, not null
   */
  int[] timesArrayFast();

//  //-------------------------------------------------------------------------
//  DateDoubleTimeSeries<T> operate(FastTimeSeries<?> other, BinaryOperator operator);
//
//  DateDoubleTimeSeries<T> operate(FastBackedDoubleTimeSeries<?> other, BinaryOperator operator);
//
//  DateDoubleTimeSeries<T> operate(double other, BinaryOperator operator);
//
//  DateDoubleTimeSeries<T> operate(UnaryOperator operator);
//
//  DateDoubleTimeSeries<T> unionOperate(FastTimeSeries<?> other, BinaryOperator operator);
//
//  DateDoubleTimeSeries<T> unionOperate(FastBackedDoubleTimeSeries<?> other, BinaryOperator operator);
//
//  //-------------------------------------------------------------------------
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> subSeries(T startTime, boolean includeStart, T endTime, boolean includeEnd);
//
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> subSeries(T startTime, T endTime);
//
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> head(int numItems);
//
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> tail(int numItems);
//
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> lag(final int lagCount);
//
//  //-------------------------------------------------------------------------
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> add(double amountToAdd);
//
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> add(DoubleTimeSeries<?> other);
//
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> unionAdd(DoubleTimeSeries<?> other);
//
//  //-------------------------------------------------------------------------
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> subtract(double amountToSubtract);
//
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> subtract(DoubleTimeSeries<?> other);
//
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> unionSubtract(DoubleTimeSeries<?> other);
//
//  //-------------------------------------------------------------------------
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> multiply(double amountToMultiplyBy);
//
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> multiply(DoubleTimeSeries<?> other);
//
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> unionMultiply(DoubleTimeSeries<?> other);
//
//  //-------------------------------------------------------------------------
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> divide(double amountToDivideBy);
//
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> divide(DoubleTimeSeries<?> other);
//
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> unionDivide(DoubleTimeSeries<?> other);
//
//  //-------------------------------------------------------------------------
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> power(double power);
//
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> power(DoubleTimeSeries<?> other);
//
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> unionPower(DoubleTimeSeries<?> other);
//
//  //-------------------------------------------------------------------------
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> minimum(double minValue);
//
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> minimum(DoubleTimeSeries<?> other);
//
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> unionMinimum(DoubleTimeSeries<?> other);
//
//  //-------------------------------------------------------------------------
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> maximum(double maxValue);
//
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> maximum(DoubleTimeSeries<?> other);
//
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> unionMaximum(DoubleTimeSeries<?> other);
//
//  //-------------------------------------------------------------------------
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> average(double value);
//
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> average(DoubleTimeSeries<?> other);
//
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> unionAverage(DoubleTimeSeries<?> other);
//
//  //-------------------------------------------------------------------------
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> intersectionFirstValue(DoubleTimeSeries<?> other);
//
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> intersectionSecondValue(DoubleTimeSeries<?> other);
//
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> noIntersectionOperation(DoubleTimeSeries<?> other);
//
//  //-------------------------------------------------------------------------
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> negate();
//
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> reciprocal();
//
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> log();
//
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> log10();
//
//  @Override  // override for covariant return type
//  DateDoubleTimeSeries<T> abs();

}
