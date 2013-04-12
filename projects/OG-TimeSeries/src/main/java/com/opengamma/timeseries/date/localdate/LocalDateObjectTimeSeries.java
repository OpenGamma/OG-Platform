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
  @Override  // override for covariant return type
  LocalDateObjectEntryIterator<V> iterator();

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  LocalDateObjectTimeSeries<V> subSeries(LocalDate startTime, LocalDate endTime);

  @Override  // override for covariant return type
  LocalDateObjectTimeSeries<V> subSeries(LocalDate startTime, boolean includeStart, LocalDate endTime, boolean includeEnd);

  @Override  // override for covariant return type
  LocalDateObjectTimeSeries<V> subSeriesFast(int startTime, int endTime);

  @Override  // override for covariant return type
  LocalDateObjectTimeSeries<V> subSeriesFast(int startTime, boolean includeStart, int endTime, boolean includeEnd);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  LocalDateObjectTimeSeries<V> head(int numItems);

  @Override  // override for covariant return type
  LocalDateObjectTimeSeries<V> tail(int numItems);

  @Override  // override for covariant return type
  LocalDateObjectTimeSeries<V> lag(int lagCount);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  LocalDateObjectTimeSeries<V> operate(UnaryOperator<V> operator);

  @Override  // override for covariant return type
  LocalDateObjectTimeSeries<V> operate(V other, BinaryOperator<V> operator);

  @Override  // override for covariant return type
  LocalDateObjectTimeSeries<V> operate(DateObjectTimeSeries<?, V> otherTimeSeries, BinaryOperator<V> operator);

  @Override  // override for covariant return type
  LocalDateObjectTimeSeries<V> unionOperate(DateObjectTimeSeries<?, V> otherTimeSeries, BinaryOperator<V> operator);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  LocalDateObjectTimeSeries<V> intersectionFirstValue(DateObjectTimeSeries<?, V> other);

  @Override  // override for covariant return type
  LocalDateObjectTimeSeries<V> intersectionSecondValue(DateObjectTimeSeries<?, V> other);

  @Override  // override for covariant return type
  LocalDateObjectTimeSeries<V> noIntersectionOperation(DateObjectTimeSeries<?, V> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  LocalDateObjectTimeSeriesBuilder<V> toBuilder();

}
