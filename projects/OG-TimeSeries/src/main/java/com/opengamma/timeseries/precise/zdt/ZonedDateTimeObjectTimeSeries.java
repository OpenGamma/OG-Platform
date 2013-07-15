/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.zdt;

import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.timeseries.ObjectTimeSeriesOperators.BinaryOperator;
import com.opengamma.timeseries.ObjectTimeSeriesOperators.UnaryOperator;
import com.opengamma.timeseries.precise.PreciseObjectTimeSeries;
import com.opengamma.timeseries.precise.PreciseTimeSeries;

/**
 * A time series that stores {@code Object} data values against {@code ZonedDateTime} times.
 * <p>
 * The "time" key to the time-series is an {@code ZonedDateTime}.
 * See {@link PreciseTimeSeries} for details about the "time" represented as a {@code long}.
 * 
 * @param <V>  the value being viewed over time
 */
public interface ZonedDateTimeObjectTimeSeries<V>
    extends PreciseObjectTimeSeries<ZonedDateTime, V> {

  /**
   * Gets the applicable time-zone.
   * 
   * @return the time-zone, not null
   */
  ZoneId getZone();

  /**
   * Returns this time-series with a different time-zone.
   * 
   * @param zone  the time-zone, not null
   * @return the same time-series with the specified zone, not null
   */
  ZonedDateTimeObjectTimeSeries<V> withZone(ZoneId zone);

  //-------------------------------------------------------------------------
  /**
   * Gets an iterator over the instant-value pairs.
   * <p>
   * Although the pairs are expressed as instances of {@code Map.Entry},
   * it is recommended to use the primitive methods on {@code ZonedDateTimeObjectIterator}.
   * 
   * @return the iterator, not null
   */
  @Override
  ZonedDateTimeObjectEntryIterator<V> iterator();

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  ZonedDateTimeObjectTimeSeries<V> subSeries(ZonedDateTime startTime, ZonedDateTime endTime);

  @Override  // override for covariant return type
  ZonedDateTimeObjectTimeSeries<V> subSeries(ZonedDateTime startTime, boolean includeStart, ZonedDateTime endTime, boolean includeEnd);

  @Override  // override for covariant return type
  ZonedDateTimeObjectTimeSeries<V> subSeriesFast(long startTime, long endTime);

  @Override  // override for covariant return type
  ZonedDateTimeObjectTimeSeries<V> subSeriesFast(long startTime, boolean includeStart, long endTime, boolean includeEnd);

  @Override  // override for covariant return type
  ZonedDateTimeObjectTimeSeries<V> head(int numItems);

  @Override  // override for covariant return type
  ZonedDateTimeObjectTimeSeries<V> tail(int numItems);

  @Override  // override for covariant return type
  ZonedDateTimeObjectTimeSeries<V> lag(int lagCount);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  ZonedDateTimeObjectTimeSeries<V> operate(UnaryOperator<V> operator);

  @Override  // override for covariant return type
  ZonedDateTimeObjectTimeSeries<V> operate(V other, BinaryOperator<V> operator);

  @Override  // override for covariant return type
  ZonedDateTimeObjectTimeSeries<V> operate(PreciseObjectTimeSeries<?, V> otherTimeSeries, BinaryOperator<V> operator);

  @Override  // override for covariant return type
  ZonedDateTimeObjectTimeSeries<V> unionOperate(PreciseObjectTimeSeries<?, V> otherTimeSeries, BinaryOperator<V> operator);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  ZonedDateTimeObjectTimeSeries<V> intersectionFirstValue(PreciseObjectTimeSeries<?, V> other);

  @Override  // override for covariant return type
  ZonedDateTimeObjectTimeSeries<V> intersectionSecondValue(PreciseObjectTimeSeries<?, V> other);

  @Override  // override for covariant return type
  ZonedDateTimeObjectTimeSeries<V> noIntersectionOperation(PreciseObjectTimeSeries<?, V> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  ZonedDateTimeObjectTimeSeriesBuilder<V> toBuilder();

}
