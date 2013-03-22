/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

/**
 * A mutable time-series, which represents the changes in a value over time.
 * <p>
 * This interface is similar to both a {@code SortedMap} of value keyed by date-time
 * and a {@code List} of date-time to value pairs.
 * As such, the date/times do not have to be evenly spread over time within the series.
 * 
 * @param <T> the date-time type, such as {@code Instant} or {@code LocalDate}
 * @param <V> the value being viewed over time, such as {@code Double}
 */
public interface MutableTimeSeries<T, V> extends TimeSeries<T, V> {

  /**
   * Puts a data point into the time-series as though adding to a {@code Map}.
   * <p>
   * If the date-time already exists in the time-series then the value is updated.
   * Otherwise a new data point for that date-time and value is added.
   * 
   * @param dateTime  the date-time to set, not null
   * @param value  the value to set, not null
   */
  void putDataPoint(T dateTime, V value);

  /**
   * Removes a data point from the time-series as though removing from a {@code Map}.
   * <p>
   * If the time-series does not contain the date-time, there is no change and no error.
   * 
   * @param dateTime  the date-time to remove, not null
   */
  void removeDataPoint(T dateTime);

  /**
   * Clears all the data points in the time-series.
   */
  void clear();

}
