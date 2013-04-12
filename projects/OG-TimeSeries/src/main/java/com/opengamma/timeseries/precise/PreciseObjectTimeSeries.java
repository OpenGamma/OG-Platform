/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise;

import com.opengamma.timeseries.ObjectTimeSeries;
import com.opengamma.timeseries.ObjectTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.ObjectTimeSeriesOperators.UnaryOperator;

/**
 * A time series that stores {@code Object} data values against instants.
 * <p>
 * The "time" key to the time-series is an instant.
 * See {@link PreciseTimeSeries} for details about the "time" represented as a {@code long}.
 * 
 * @param <T>  the instant type
 * @param <V>  the value being viewed over time
 */
public interface PreciseObjectTimeSeries<T, V>
    extends ObjectTimeSeries<T, V>, PreciseTimeSeries<T, V> {

  @Override  // override for covariant return type
  PreciseObjectTimeSeries<T, V> subSeries(T startTime, boolean includeStart, T endTime, boolean includeEnd);

  @Override  // override for covariant return type
  PreciseObjectTimeSeries<T, V> subSeries(T startTime, T endTime);

  PreciseObjectTimeSeries<T, V> subSeriesFast(long startTime, long endTime);

  PreciseObjectTimeSeries<T, V> subSeriesFast(long startTime, boolean includeStart, long endTime, boolean includeEnd);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  PreciseObjectTimeSeries<T, V> head(int numItems);

  @Override  // override for covariant return type
  PreciseObjectTimeSeries<T, V> tail(int numItems);

  @Override  // override for covariant return type
  PreciseObjectTimeSeries<T, V> lag(int lagCount);

  //-------------------------------------------------------------------------
  /**
   * Applies a unary operator to each value in the time series.
   * 
   * @param operator  the operator, not null
   * @return a copy of this series with the operator applied, not null
   */
  PreciseObjectTimeSeries<T, V> operate(UnaryOperator<V> operator);

  /**
   * Applies a binary operator to each value in the time series.
   * 
   * @param other  the single value passed into the binary operator
   * @param operator  the operator, not null
   * @return a copy of this series with the operator applied, not null
   */
  PreciseObjectTimeSeries<T, V> operate(V other, BinaryOperator<V> operator);

  /**
   * Applies a binary operator to each value in this time series and
   * another time-series, returning the intersection of times.
   * 
   * @param otherTimeSeries  the other time-series, not null
   * @param operator  the operator, not null
   * @return a copy of this series with the operator applied, not null
   */
  PreciseObjectTimeSeries<T, V> operate(PreciseObjectTimeSeries<?, V> otherTimeSeries, BinaryOperator<V> operator);

  /**
   * Applies a binary operator to each value in this time series and
   * another time-series, returning the union of times.
   * 
   * @param otherTimeSeries  the other time-series, not null
   * @param operator  the operator, not null
   * @return a copy of this series with the operator applied, not null
   */
  PreciseObjectTimeSeries<T, V> unionOperate(PreciseObjectTimeSeries<?, V> otherTimeSeries, BinaryOperator<V> operator);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values from this series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  PreciseObjectTimeSeries<T, V> intersectionFirstValue(PreciseObjectTimeSeries<?, V> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values from the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  PreciseObjectTimeSeries<T, V> intersectionSecondValue(PreciseObjectTimeSeries<?, V> other);

  /**
   * Creates a new time-series combining both series where there are no
   * overlapping date-times.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   * @throws RuntimeException if there are overlapping date-times
   */
  PreciseObjectTimeSeries<T, V> noIntersectionOperation(PreciseObjectTimeSeries<?, V> other);

  //-------------------------------------------------------------------------
  /**
   * Returns a builder containing the same data as this time-series.
   * <p>
   * The builder has methods to modify the time-series.
   * Entries can be added, or removed via the iterator.
   * 
   * @return the builder, not null
   */
  PreciseObjectTimeSeriesBuilder<T, V> toBuilder();

}
