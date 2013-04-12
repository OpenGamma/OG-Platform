/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.timeseries.date;

import java.util.Map;

/**
 * A builder of time-series that stores {@code double} data values against {@code LocalDate} dates.
 * <p>
 * The "time" key to the time-series is a date.
 * See {@link DateTimeSeries} for details about the "time" represented as an {@code int}.
 * 
 * @param <T>  the date type
 */
public interface DateDoubleTimeSeriesBuilder<T> {

  /**
   * Gets the size of the builder.
   * 
   * @return the number of pairs in the builder
   */
  int size();

  /**
   * Gets an iterator over the date-value pairs.
   * <p>
   * Although the pairs are expressed as instances of {@code Map.Entry},
   * it is recommended to use the primitive methods on {@code DateDoubleEntryIterator}.
   * 
   * @return the iterator, not null
   */
  DateDoubleEntryIterator<T> iterator();

  //-------------------------------------------------------------------------
  /**
   * Puts a date-value pair into the builder.
   * 
   * @param time  the time to add, not null
   * @param value  the value to add, not null
   * @return {@code this} for method chaining, not null
   */
  DateDoubleTimeSeriesBuilder<T> put(T time, double value);

  /**
   * Puts a date-value pair into the builder.
   * 
   * @param time  the time to add, not null
   * @param value  the value to add, not null
   * @return {@code this} for method chaining, not null
   */
  DateDoubleTimeSeriesBuilder<T> put(int time, double value);

  /**
   * Puts date-value pairs into the builder.
   * 
   * @param times  the times array to add, not null
   * @param values  the values array to add, not null
   * @return {@code this} for method chaining, not null
   */
  DateDoubleTimeSeriesBuilder<T> putAll(T[] times, double[] values);

  /**
   * Puts date-value pairs into the builder.
   * 
   * @param times  the times array to add, not null
   * @param values  the values array to add, not null
   * @return {@code this} for method chaining, not null
   */
  DateDoubleTimeSeriesBuilder<T> putAll(int[] times, double[] values);

  //-------------------------------------------------------------------------
  /**
   * Puts date-value pairs into the builder from another series.
   * <p>
   * This adds the whole of the specified series.
   * 
   * @param timeSeries  the timeSeries to copy from, not null
   * @return {@code this} for method chaining, not null
   */
  DateDoubleTimeSeriesBuilder<T> putAll(DateDoubleTimeSeries<?> timeSeries);

  /**
   * Puts date-value pairs into the builder from another series.
   * <p>
   * This adds the the specified range of the specified series.
   * 
   * @param timeSeries  the timeSeries to copy from, not null
   * @param startPos  the start position, must be valid
   * @param endPos  the end position, must be valid
   * @return {@code this} for method chaining, not null
   * @throws IndexOutOfBoundsException if the indexes are invalid
   */
  DateDoubleTimeSeriesBuilder<T> putAll(DateDoubleTimeSeries<?> timeSeries, int startPos, int endPos);

  /**
   * Puts date-value pairs into the builder from another series.
   * <p>
   * This adds the the specified range of the specified series.
   * 
   * @param timeSeriesMap  the map representing a time-series to copy from, not null
   * @return {@code this} for method chaining, not null
   */
  DateDoubleTimeSeriesBuilder<T> putAll(Map<T, Double> timeSeriesMap);

  //-------------------------------------------------------------------------
  /**
   * Clears the builder.
   * 
   * @return {@code this} for method chaining, not null
   */
  DateDoubleTimeSeriesBuilder<T> clear();

  //-------------------------------------------------------------------------
  /**
   * Builds the immutable time-series from the builder.
   * <p>
   * Continuing to use the builder after calling this method is a programming error.
   * 
   * @return the immutable resulting time-series, not null
   */
  DateDoubleTimeSeries<T> build();

}
