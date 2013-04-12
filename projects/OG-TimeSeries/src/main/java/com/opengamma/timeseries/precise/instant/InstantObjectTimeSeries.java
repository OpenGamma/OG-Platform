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

  @Override  // override for covariant return type
  InstantObjectTimeSeries<V> subSeriesFast(long startTime, long endTime);

  @Override  // override for covariant return type
  InstantObjectTimeSeries<V> subSeriesFast(long startTime, boolean includeStart, long endTime, boolean includeEnd);

  @Override  // override for covariant return type
  InstantObjectTimeSeries<V> head(int numItems);

  @Override  // override for covariant return type
  InstantObjectTimeSeries<V> tail(int numItems);

  @Override  // override for covariant return type
  InstantObjectTimeSeries<V> lag(int lagCount);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  InstantObjectTimeSeries<V> operate(UnaryOperator<V> operator);

  @Override  // override for covariant return type
  InstantObjectTimeSeries<V> operate(V other, BinaryOperator<V> operator);

  @Override  // override for covariant return type
  InstantObjectTimeSeries<V> operate(PreciseObjectTimeSeries<?, V> otherTimeSeries, BinaryOperator<V> operator);

  @Override  // override for covariant return type
  InstantObjectTimeSeries<V> unionOperate(PreciseObjectTimeSeries<?, V> otherTimeSeries, BinaryOperator<V> operator);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  InstantObjectTimeSeries<V> intersectionFirstValue(PreciseObjectTimeSeries<?, V> other);

  @Override  // override for covariant return type
  InstantObjectTimeSeries<V> intersectionSecondValue(PreciseObjectTimeSeries<?, V> other);

  @Override  // override for covariant return type
  InstantObjectTimeSeries<V> noIntersectionOperation(PreciseObjectTimeSeries<?, V> other);

  //-------------------------------------------------------------------------
  @Override  // override for covariant return type
  InstantObjectTimeSeriesBuilder<V> toBuilder();

}
