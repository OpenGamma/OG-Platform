/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

/**
 * A time-series, which represents the changes in a value over time.
 * <p>
 * This interface is similar to both a {@code SortedMap} of value keyed by date-time
 * and a {@code List} of date-time to value pairs.
 * As such, the date/times do not have to be evenly spread over time within the series.
 * 
 * @param <T> the date-time type, such as {@code Instant} or {@code LocalDate}
 * @param <V> the value being viewed over time, such as {@code Double}
 */
public interface ObjectTimeSeries<T, V> extends TimeSeries<T, V> {

  @Override  // override for covariant return type
  ObjectTimeSeries<T, V> subSeries(T startTimeInclusive, T endTimeExclusive);

  @Override  // override for covariant return type
  ObjectTimeSeries<T, V> subSeries(T startTime, boolean includeStart, T endTime, boolean includeEnd);

  @Override  // override for covariant return type
  ObjectTimeSeries<T, V> head(int numItems);

  @Override  // override for covariant return type
  ObjectTimeSeries<T, V> tail(int numItems);

  @Override  // override for covariant return type
  ObjectTimeSeries<T, V> lag(int lagCount);

  @Override  // override for covariant return type
  ObjectTimeSeries<T, V> newInstance(T[] dateTimes, V[] values);

}
