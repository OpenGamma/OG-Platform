/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise;

import java.util.NoSuchElementException;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.DoubleTimeSeriesOperators.UnaryOperator;

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

  PreciseDoubleTimeSeries<T> subSeriesFast(long startTime, long endTime);

  PreciseDoubleTimeSeries<T> subSeriesFast(long startTime, boolean includeStart, long endTime, boolean includeEnd);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> head(int numItems);

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> tail(int numItems);

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> lag(int lagCount);

  //-------------------------------------------------------------------------
  /**
   * Applies a unary operator to each value in the time series.
   * 
   * @param operator  the operator, not null
   * @return a copy of this series with the operator applied, not null
   */
  PreciseDoubleTimeSeries<T> operate(UnaryOperator operator);

  /**
   * Applies a binary operator to each value in the time series.
   * 
   * @param other  the single value passed into the binary operator
   * @param operator  the operator, not null
   * @return a copy of this series with the operator applied, not null
   */
  PreciseDoubleTimeSeries<T> operate(double other, BinaryOperator operator);

  /**
   * Applies a binary operator to each value in this time series and
   * another time-series, returning the intersection of times.
   * 
   * @param otherTimeSeries  the other time-series, not null
   * @param operator  the operator, not null
   * @return a copy of this series with the operator applied, not null
   */
  PreciseDoubleTimeSeries<T> operate(PreciseDoubleTimeSeries<?> otherTimeSeries, BinaryOperator operator);

  /**
   * Applies a binary operator to each value in this time series and
   * another time-series, returning the union of times.
   * 
   * @param otherTimeSeries  the other time-series, not null
   * @param operator  the operator, not null
   * @return a copy of this series with the operator applied, not null
   */
  PreciseDoubleTimeSeries<T> unionOperate(PreciseDoubleTimeSeries<?> otherTimeSeries, BinaryOperator operator);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> add(double amountToAdd);

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> add(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> unionAdd(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> subtract(double amountToSubtract);

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> subtract(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> unionSubtract(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> multiply(double amountToMultiplyBy);

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> multiply(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> unionMultiply(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> divide(double amountToDivideBy);

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> divide(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> unionDivide(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> power(double power);

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> power(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> unionPower(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> minimum(double minValue);

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> minimum(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> unionMinimum(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> maximum(double maxValue);

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> maximum(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> unionMaximum(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> average(double value);

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> average(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> unionAverage(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> intersectionFirstValue(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> intersectionSecondValue(DoubleTimeSeries<?> other);

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> noIntersectionOperation(DoubleTimeSeries<?> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> negate();

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> reciprocal();

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> log();

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> log10();

  @Override  // override for covariant return type
  PreciseDoubleTimeSeries<T> abs();

  //-------------------------------------------------------------------------
  /**
   * Returns a builder containing the same data as this time-series.
   * <p>
   * The builder has methods to modify the time-series.
   * Entries can be added, or removed via the iterator.
   * 
   * @return the builder, not null
   */
  PreciseDoubleTimeSeriesBuilder<T> toBuilder();

}
