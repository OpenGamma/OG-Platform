/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date;

import com.opengamma.timeseries.ObjectTimeSeries;
import com.opengamma.timeseries.ObjectTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.ObjectTimeSeriesOperators.UnaryOperator;

/**
 * A time series that stores {@code Object} data values against dates.
 * <p>
 * The "time" key to the time-series is a date.
 * See {@link DateTimeSeries} for details about the "time" represented as an {@code int}.
 * 
 * @param <T>  the date type
 * @param <V>  the value being viewed over time
 */
public interface DateObjectTimeSeries<T, V>
    extends ObjectTimeSeries<T, V>, DateTimeSeries<T, V> {

  @Override  // override for covariant return type
  DateObjectTimeSeries<T, V> subSeries(T startTime, T endTime);

  @Override  // override for covariant return type
  DateObjectTimeSeries<T, V> subSeries(T startTime, boolean includeStart, T endTime, boolean includeEnd);

  DateObjectTimeSeries<T, V> subSeriesFast(int startTime, int endTime);

  DateObjectTimeSeries<T, V> subSeriesFast(int startTime, boolean includeStart, int endTime, boolean includeEnd);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  DateObjectTimeSeries<T, V> head(int numItems);

  @Override  // override for covariant return type
  DateObjectTimeSeries<T, V> tail(int numItems);

  @Override  // override for covariant return type
  DateObjectTimeSeries<T, V> lag(int lagCount);

  //-------------------------------------------------------------------------
  /**
   * Applies a unary operator to each value in the time series.
   * 
   * @param operator  the operator, not null
   * @return a copy of this series with the operator applied, not null
   */
  DateObjectTimeSeries<T, V> operate(UnaryOperator<V> operator);

  /**
   * Applies a binary operator to each value in the time series.
   * 
   * @param other  the single value passed into the binary operator
   * @param operator  the operator, not null
   * @return a copy of this series with the operator applied, not null
   */
  DateObjectTimeSeries<T, V> operate(V other, BinaryOperator<V> operator);

  /**
   * Applies a binary operator to each value in this time series and
   * another time-series, returning the intersection of times.
   * 
   * @param otherTimeSeries  the other time-series, not null
   * @param operator  the operator, not null
   * @return a copy of this series with the operator applied, not null
   */
  DateObjectTimeSeries<T, V> operate(DateObjectTimeSeries<?, V> otherTimeSeries, BinaryOperator<V> operator);

  /**
   * Applies a binary operator to each value in this time series and
   * another time-series, returning the union of times.
   * 
   * @param otherTimeSeries  the other time-series, not null
   * @param operator  the operator, not null
   * @return a copy of this series with the operator applied, not null
   */
  DateObjectTimeSeries<T, V> unionOperate(DateObjectTimeSeries<?, V> otherTimeSeries, BinaryOperator<V> operator);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values from this series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DateObjectTimeSeries<T, V> intersectionFirstValue(DateObjectTimeSeries<?, V> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values from the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  DateObjectTimeSeries<T, V> intersectionSecondValue(DateObjectTimeSeries<?, V> other);

  /**
   * Creates a new time-series combining both series where there are no
   * overlapping date-times.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   * @throws RuntimeException if there are overlapping date-times
   */
  DateObjectTimeSeries<T, V> noIntersectionOperation(DateObjectTimeSeries<?, V> other);

  //-------------------------------------------------------------------------
  /**
   * Returns a builder containing the same data as this time-series.
   * <p>
   * The builder has methods to modify the time-series.
   * Entries can be added, or removed via the iterator.
   * 
   * @return the builder, not null
   */
  DateObjectTimeSeriesBuilder<T, V> toBuilder();

}
