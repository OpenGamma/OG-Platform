/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.instant;

import org.threeten.bp.Instant;

import com.opengamma.timeseries.ObjectTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.ObjectTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.precise.PreciseObjectTimeSeries;
import com.opengamma.timeseries.precise.PreciseTimeSeries;

/**
 * A time series that stores {@code Object} data values against {@code Instant} times.
 * <p>
 * The "time" key to the time-series is an {@code Instant}.
 * See {@link PreciseTimeSeries} for details about the "time" represented as a {@code long}.
 * 
 * @param <V>  the value being viewed over time
 */
public interface InstantObjectTimeSeries<V>
    extends PreciseObjectTimeSeries<Instant, V> {

  /**
   * Gets an iterator over the instant-value pairs.
   * <p>
   * Although the pairs are expressed as instances of {@code Map.Entry},
   * it is recommended to use the primitive methods on {@code InstantObjectIterator}.
   * 
   * @return the iterator, not null
   */
  @Override
  InstantObjectEntryIterator<V> iterator();

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  InstantObjectTimeSeries<V> subSeries(Instant startTime, Instant endTime);

  @Override  // override for covariant return type
  InstantObjectTimeSeries<V> subSeries(Instant startTime, boolean includeStart, Instant endTime, boolean includeEnd);

  InstantObjectTimeSeries<V> subSeriesFast(long startTime, long endTime);

  InstantObjectTimeSeries<V> subSeriesFast(long startTime, boolean includeStart, long endTime, boolean includeEnd);

  @Override  // override for covariant return type
  InstantObjectTimeSeries<V> head(int numItems);

  @Override  // override for covariant return type
  InstantObjectTimeSeries<V> tail(int numItems);

  @Override  // override for covariant return type
  InstantObjectTimeSeries<V> lag(int lagCount);

  //-------------------------------------------------------------------------
  /**
   * Applies a unary operator to each value in the time series.
   * 
   * @param operator  the operator, not null
   * @return a copy of this series with the operator applied, not null
   */
  InstantObjectTimeSeries<V> operate(UnaryOperator<V> operator);

  /**
   * Applies a binary operator to each value in the time series.
   * 
   * @param other  the single value passed into the binary operator
   * @param operator  the operator, not null
   * @return a copy of this series with the operator applied, not null
   */
  InstantObjectTimeSeries<V> operate(V other, BinaryOperator<V> operator);

  /**
   * Applies a binary operator to each value in this time series and
   * another time-series, returning the intersection of times.
   * 
   * @param otherTimeSeries  the other time-series, not null
   * @param operator  the operator, not null
   * @return a copy of this series with the operator applied, not null
   */
  InstantObjectTimeSeries<V> operate(PreciseObjectTimeSeries<?, V> otherTimeSeries, BinaryOperator<V> operator);

  /**
   * Applies a binary operator to each value in this time series and
   * another time-series, returning the union of times.
   * 
   * @param otherTimeSeries  the other time-series, not null
   * @param operator  the operator, not null
   * @return a copy of this series with the operator applied, not null
   */
  InstantObjectTimeSeries<V> unionOperate(PreciseObjectTimeSeries<?, V> otherTimeSeries, BinaryOperator<V> operator);

  //-------------------------------------------------------------------------
  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values from this series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  InstantObjectTimeSeries<V> intersectionFirstValue(PreciseObjectTimeSeries<?, V> other);

  /**
   * Creates a new time-series with the intersection of the date-times from
   * this time-series and another time-series, with the values from the other series.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   */
  InstantObjectTimeSeries<V> intersectionSecondValue(PreciseObjectTimeSeries<?, V> other);

  /**
   * Creates a new time-series combining both series where there are no
   * overlapping date-times.
   * 
   * @param other  the other series to intersect with, not null
   * @return the new time-series, not null
   * @throws RuntimeException if there are overlapping date-times
   */
  InstantObjectTimeSeries<V> noIntersectionOperation(PreciseObjectTimeSeries<?, V> other);

}
