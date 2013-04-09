/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date.localdate;

import java.util.Map;

import org.threeten.bp.LocalDate;

import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.DateObjectTimeSeries;
import com.opengamma.timeseries.date.DateObjectTimeSeriesBuilder;

/**
 * A builder of time-series that stores {@code Object} data values against {@code LocalDate} dates.
 * <p>
 * The "time" key to the time-series is a {@code LocalDate}.
 * See {@link DateDoubleTimeSeries} for details about the "time" represented as an {@code int}.
 * 
 * @param <V>  the value being viewed over time
 */
public interface LocalDateObjectTimeSeriesBuilder<V> extends DateObjectTimeSeriesBuilder<LocalDate, V> {

  /**
   * Puts a time-value pair into the builder.
   * 
   * @param time  the time to add, not null
   * @param value  the value to add, not null
   * @return {@code this} for method chaining, not null
   */
  @Override  // override for covariant return type
  LocalDateObjectTimeSeriesBuilder<V> put(LocalDate time, V value);

  /**
   * Puts a time-value pair into the builder.
   * 
   * @param time  the time to add, not null
   * @param value  the value to add, not null
   * @return {@code this} for method chaining, not null
   */
  @Override  // override for covariant return type
  LocalDateObjectTimeSeriesBuilder<V> put(int time, V value);

  /**
   * Puts time-value pairs into the builder.
   * 
   * @param times  the times array to add, not null
   * @param values  the values array to add, not null
   * @return {@code this} for method chaining, not null
   */
  @Override  // override for covariant return type
  LocalDateObjectTimeSeriesBuilder<V> putAll(LocalDate[] times, V[] values);

  /**
   * Puts time-value pairs into the builder.
   * 
   * @param times  the times array to add, not null
   * @param values  the values array to add, not null
   * @return {@code this} for method chaining, not null
   */
  @Override  // override for covariant return type
  LocalDateObjectTimeSeriesBuilder<V> putAll(int[] times, V[] values);

  //-------------------------------------------------------------------------
  /**
   * Puts time-value pairs into the builder from another series.
   * <p>
   * This adds the whole of the specified series.
   * 
   * @param timeSeries  the timeSeries to copy from, not null
   * @return {@code this} for method chaining, not null
   */
  @Override  // override for covariant return type
  LocalDateObjectTimeSeriesBuilder<V> putAll(DateObjectTimeSeries<?, V> timeSeries);

  /**
   * Puts time-value pairs into the builder from another series.
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
  LocalDateObjectTimeSeriesBuilder<V> putAll(DateObjectTimeSeries<?, V> timeSeries, int startPos, int endPos);

  /**
   * Puts time-value pairs into the builder from another series.
   * <p>
   * This adds the the specified range of the specified series.
   * 
   * @param timeSeriesMap  the map representing a time-series to copy from, not null
   * @return {@code this} for method chaining, not null
   */
  @Override  // override for covariant return type
  LocalDateObjectTimeSeriesBuilder<V> putAll(Map<LocalDate, V> timeSeriesMap);

  //-------------------------------------------------------------------------
  /**
   * Clears the builder.
   * 
   * @return {@code this} for method chaining, not null
   */
  @Override  // override for covariant return type
  LocalDateObjectTimeSeriesBuilder<V> clear();

  //-------------------------------------------------------------------------
  /**
   * Builds the immutable time-series from the builder.
   * <p>
   * Continuing to use the builder after calling this method is a programming error.
   * 
   * @return the immutable resulting time-series, not null
   */
  @Override  // override for covariant return type
  LocalDateObjectTimeSeries<V> build();

}
