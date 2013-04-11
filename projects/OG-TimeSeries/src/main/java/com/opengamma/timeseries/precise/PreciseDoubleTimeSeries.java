/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise;

import java.util.NoSuchElementException;

import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * A time series that stores {@code double} data values against instants.
 * <p>
 * The "time" key to the time-series is an instant.
 * See {@link PreciseTimeSeries} for details about the "time" represented as a {@code long}.
 * 
 * @param <T>  the instant type
 */
public interface PreciseDoubleTimeSeries<T>
    extends DoubleTimeSeries<T>, PreciseTimeSeries<T, Double> {

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
   * Gets the value at the earliest instant in the series.
   * 
   * @return the value at the earliest instant
   * @throws NoSuchElementException if empty
   */
  double getEarliestValueFast();

  /**
   * Gets the value at the latest instant in the series.
   * 
   * @return the value at the latest instant
   * @throws NoSuchElementException if empty
   */
  double getLatestValueFast();

  //-------------------------------------------------------------------------
  /**
   * Gets an iterator over the instant-value pairs.
   * <p>
   * Although the pairs are expressed as instances of {@code Map.Entry},
   * it is recommended to use the primitive methods on {@code PreciseDoubleEntryIterator}.
   * 
   * @return the iterator, not null
   */
  PreciseDoubleEntryIterator<T> iterator();

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> subSeries(T startTime, boolean includeStart, T endTime, boolean includeEnd);

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> subSeries(T startTime, T endTime);

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> head(int numItems);

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> tail(int numItems);

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> lag(final int lagCount);

//  //-------------------------------------------------------------------------
//  /**
//   * Returns a time-series where the specified operator has been applied to
//   * each element in this time-series.
//   * 
//   * @param operator  the operator, not null
//   * @return the time-series, not null
//   */
//  InstantDoubleTimeSeries<T> operate(UnaryOperator operator);
//
//  /**
//   * Returns a time-series where the specified operator has been applied to
//   * each element in this time-series.
//   * 
//   * @param other  the other value to use when applying the operator, not null
//   * @param operator  the operator, not null
//   * @return the time-series, not null
//   */
//  InstantDoubleTimeSeries<T> operate(double other, BinaryOperator operator);
//
//  //-------------------------------------------------------------------------
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> add(double amountToAdd);
//
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> add(DoubleTimeSeries<?> other);
//
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> unionAdd(DoubleTimeSeries<?> other);
//
//  //-------------------------------------------------------------------------
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> subtract(double amountToSubtract);
//
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> subtract(DoubleTimeSeries<?> other);
//
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> unionSubtract(DoubleTimeSeries<?> other);
//
//  //-------------------------------------------------------------------------
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> multiply(double amountToMultiplyBy);
//
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> multiply(DoubleTimeSeries<?> other);
//
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> unionMultiply(DoubleTimeSeries<?> other);
//
//  //-------------------------------------------------------------------------
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> divide(double amountToDivideBy);
//
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> divide(DoubleTimeSeries<?> other);
//
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> unionDivide(DoubleTimeSeries<?> other);
//
//  //-------------------------------------------------------------------------
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> power(double power);
//
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> power(DoubleTimeSeries<?> other);
//
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> unionPower(DoubleTimeSeries<?> other);
//
//  //-------------------------------------------------------------------------
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> minimum(double minValue);
//
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> minimum(DoubleTimeSeries<?> other);
//
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> unionMinimum(DoubleTimeSeries<?> other);
//
//  //-------------------------------------------------------------------------
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> maximum(double maxValue);
//
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> maximum(DoubleTimeSeries<?> other);
//
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> unionMaximum(DoubleTimeSeries<?> other);
//
//  //-------------------------------------------------------------------------
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> average(double value);
//
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> average(DoubleTimeSeries<?> other);
//
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> unionAverage(DoubleTimeSeries<?> other);
//
//  //-------------------------------------------------------------------------
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> intersectionFirstValue(DoubleTimeSeries<?> other);
//
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> intersectionSecondValue(DoubleTimeSeries<?> other);
//
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> noIntersectionOperation(DoubleTimeSeries<?> other);
//
//  //-------------------------------------------------------------------------
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> negate();
//
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> reciprocal();
//
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> log();
//
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> log10();
//
//  @Override  // override for covariant return type
//  InstantDoubleTimeSeries<T> abs();

}
