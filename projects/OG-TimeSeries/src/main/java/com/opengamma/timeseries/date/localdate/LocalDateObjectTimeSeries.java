/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date.localdate;

import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.ObjectTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.ObjectTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.date.DateObjectTimeSeries;
import com.opengamma.timeseries.date.DateTimeSeries;

/**
 * A time series that stores {@code Object} data values against {@code LocalDate} dates.
 * <p>
 * The "time" key to the time-series is a {@code LocalDate}.
 * See {@link DateTimeSeries} for details about the "time" represented as an {@code int}.
 * 
 * @param <V>  the value being viewed over time
 */
public interface LocalDateObjectTimeSeries<V>
    extends DateObjectTimeSeries<LocalDate, V> {

  /**
   * Gets an iterator over the date-value pairs.
   * <p>
   * Although the pairs are expressed as instances of {@code Map.Entry},
   * it is recommended to use the primitive methods on {@code LocalDateObjectIterator}.
   * 
   * @return the iterator, not null
   */
  @Override
  LocalDateObjectEntryIterator<V> iterator();

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  LocalDateObjectTimeSeries<V> subSeries(LocalDate startTime, LocalDate endTime);

  @Override  // override for covariant return type
  LocalDateObjectTimeSeries<V> subSeries(LocalDate startTime, boolean includeStart, LocalDate endTime, boolean includeEnd);

  LocalDateObjectTimeSeries<V> subSeriesFast(int startTime, int endTime);

  LocalDateObjectTimeSeries<V> subSeriesFast(int startTime, final boolean includeStart, int endTime, final boolean includeEnd);

  @Override  // override for covariant return type
  LocalDateObjectTimeSeries<V> head(int numItems);

  @Override  // override for covariant return type
  LocalDateObjectTimeSeries<V> tail(int numItems);

  @Override  // override for covariant return type
  LocalDateObjectTimeSeries<V> lag(final int lagCount);

  //-------------------------------------------------------------------------
  /**
   * Applies a unary operator to each value in the time series.
   * 
   * @param operator  the operator, not null
   * @return a copy of this series with the operator applied, not null
   */
  LocalDateObjectTimeSeries<V> operate(UnaryOperator<V> operator);

  /**
   * Applies a binary operator to each value in the time series.
   * 
   * @param other  the single value passed into the binary operator
   * @param operator  the operator, not null
   * @return a copy of this series with the operator applied, not null
   */
  LocalDateObjectTimeSeries<V> operate(V other, BinaryOperator<V> operator);

  /**
   * Applies a binary operator to each value in this time series and
   * another time-series, returning the intersection of times.
   * 
   * @param otherTimeSeries  the other time-series, not null
   * @param operator  the operator, not null
   * @return a copy of this series with the operator applied, not null
   */
  LocalDateObjectTimeSeries<V> operate(DateObjectTimeSeries<?, V> otherTimeSeries, BinaryOperator<V> operator);

  /**
   * Applies a binary operator to each value in this time series and
   * another time-series, returning the union of times.
   * 
   * @param otherTimeSeries  the other time-series, not null
   * @param operator  the operator, not null
   * @return a copy of this series with the operator applied, not null
   */
  LocalDateObjectTimeSeries<V> unionOperate(DateObjectTimeSeries<?, V> otherTimeSeries, BinaryOperator<V> operator);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values from this series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  LocalDateObjectTimeSeries<V> intersectionFirstValue(DateObjectTimeSeries<?, V> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values from the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  LocalDateObjectTimeSeries<V> intersectionSecondValue(DateObjectTimeSeries<?, V> other);

  /**
   * Creates a new time-series combining both series where there are no
   * overlapping date-times.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   * @throws RuntimeException if there are overlapping date-times
   */
  LocalDateObjectTimeSeries<V> noIntersectionOperation(DateObjectTimeSeries<?, V> other);

}
