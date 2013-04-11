/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.precise.instant;

import java.util.Map;

import org.threeten.bp.Instant;

import com.opengamma.timeseries.precise.PreciseObjectTimeSeries;
import com.opengamma.timeseries.precise.PreciseObjectTimeSeriesBuilder;
import com.opengamma.timeseries.precise.PreciseTimeSeries;

/**
 * A builder of time-series that stores {@code Object} data values against {@code Instant} times.
 * <p>
 * The "time" key to the time-series is an {@code Instant}.
 * See {@link PreciseTimeSeries} for details about the "time" represented as a {@code long}.
 * 
 * @param <V>  the value being viewed over time
 */
public interface InstantObjectTimeSeriesBuilder<V> extends PreciseObjectTimeSeriesBuilder<Instant, V> {

  /**
   * Puts an instant-value pair into the builder.
   * 
   * @param time  the time to add, not null
   * @param value  the value to add, not null
   * @return {@code this} for method chaining, not null
   */
  @Override  // override for covariant return type
  InstantObjectTimeSeriesBuilder<V> put(Instant time, V value);

  /**
   * Puts an instant-value pair into the builder.
   * 
   * @param time  the time to add, not null
   * @param value  the value to add, not null
   * @return {@code this} for method chaining, not null
   */
  @Override  // override for covariant return type
  InstantObjectTimeSeriesBuilder<V> put(long time, V value);

  /**
   * Puts instant-value pairs into the builder.
   * 
   * @param times  the times array to add, not null
   * @param values  the values array to add, not null
   * @return {@code this} for method chaining, not null
   */
  @Override  // override for covariant return type
  InstantObjectTimeSeriesBuilder<V> putAll(Instant[] times, V[] values);

  /**
   * Puts instant-value pairs into the builder.
   * 
   * @param times  the times array to add, not null
   * @param values  the values array to add, not null
   * @return {@code this} for method chaining, not null
   */
  @Override  // override for covariant return type
  InstantObjectTimeSeriesBuilder<V> putAll(long[] times, V[] values);

  //-------------------------------------------------------------------------
  /**
   * Puts instant-value pairs into the builder from another series.
   * <p>
   * This adds the whole of the specified series.
   * 
   * @param timeSeries  the timeSeries to copy from, not null
   * @return {@code this} for method chaining, not null
   */
  @Override  // override for covariant return type
  InstantObjectTimeSeriesBuilder<V> putAll(PreciseObjectTimeSeries<?, V> timeSeries);

  /**
   * Puts instant-value pairs into the builder from another series.
   * <p>
   * This adds the the specified range of the specified series.
   * 
   * @param timeSeries  the timeSeries to copy from, not null
   * @param startPos  the start position, must be valid
   * @param endPos  the end position, must be valid
   * @return {@code this} for method chaining, not null
   * @throws IndexOutOfBoundsException if the indexes are invalid
   */
  @Override  // override for covariant return type
  InstantObjectTimeSeriesBuilder<V> putAll(PreciseObjectTimeSeries<?, V> timeSeries, int startPos, int endPos);

  /**
   * Puts instant-value pairs into the builder from another series.
   * <p>
   * This adds the the specified range of the specified series.
   * 
   * @param timeSeriesMap  the map representing a time-series to copy from, not null
   * @return {@code this} for method chaining, not null
   */
  @Override  // override for covariant return type
  InstantObjectTimeSeriesBuilder<V> putAll(Map<Instant, V> timeSeriesMap);

  //-------------------------------------------------------------------------
  /**
   * Clears the builder.
   * 
   * @return {@code this} for method chaining, not null
   */
  @Override  // override for covariant return type
  InstantObjectTimeSeriesBuilder<V> clear();

  //-------------------------------------------------------------------------
  /**
   * Builds the immutable time-series from the builder.
   * <p>
   * Continuing to use the builder after calling this method is a programming error.
   * 
   * @return the immutable resulting time-series, not null
   */
  @Override  // override for covariant return type
  InstantObjectTimeSeries<V> build();

}
